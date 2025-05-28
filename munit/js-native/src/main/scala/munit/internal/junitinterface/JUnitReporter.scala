/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import munit.diff.console.AnsiColors
import munit.internal.PlatformCompat

import sbt.testing._

final class JUnitReporter(
    eventHandler: EventHandler,
    loggers: Array[Logger],
    settings: RunSettings,
    taskDef: TaskDef,
) {
  import JUnitReporter._

  private var suiteStartNanos: Long = Long.MaxValue

  private val isAnsiSupported = loggers.forall(_.ansiCodesSupported()) &&
    settings.color

  private def logEvent(method: String, color: String = null, fq: Boolean = false)(
      prefix: String = "",
      suffix: String = "",
      nanos: Long = -1,
      extra: StringBuilder => Unit = null,
  ): Unit = {
    implicit val sb = new StringBuilder()
    AnsiColors.c(color, flag = true) { sb =>
      if (prefix.nonEmpty) sb.append(prefix).append(' ')
      if (fq) {
        sb.append(taskDef.fullyQualifiedName())
        if (method.nonEmpty) sb.append('.')
      }
      sb.append(method).append(suffix)
    }
    if (nanos >= 0) {
      sb.append(' ')
      AnsiColors.c(AnsiColors.DarkGrey, flag = true)(_.append(
        "%.3fs".format(nanos / 1000000000.0)
      ))
    }
    if (extra ne null) extra(sb.append(' '))
    log(Info, sb.toString())
  }

  def reportTestSuiteStarted(): Unit = {
    suiteStartNanos = System.nanoTime
    logEvent("", AnsiColors.GREEN, fq = true)(suffix = ":")
  }

  def reportTestStarted(method: String): Unit =
    if (settings.verbose) logEvent(method)(suffix = " started")

  def reportTestIgnored(method: String, suffix: String): Unit = {
    val suffixed = if (suffix.isEmpty) "" else s" $suffix"
    logEvent(method, AnsiColors.YELLOW)(
      "==> i",
      suffixed + " ignored",
      nanos = System.nanoTime - suiteStartNanos,
    )
    emitEvent(method, Status.Ignored, None, 0)
  }

  def reportAssumptionViolation(method: String, e: Throwable): Unit = {
    logEvent(method, AnsiColors.YELLOW)("==> s", " skipped")
    emitEvent(method, Status.Skipped, Option(e), 0)
  }

  def reportTestPassed(method: String, elapsedNanos: Long): Unit = {
    logEvent(method, AnsiColors.GREEN)("  +", nanos = elapsedNanos)
    emitEvent(method, Status.Success, None, elapsedNanos)
  }

  def reportTestFailed(
      method: String,
      ex: Throwable,
      elapsedNanos: Long,
  ): Unit = {
    logEvent(method, AnsiColors.LightRed, fq = true)(
      s"==> X",
      nanos = elapsedNanos,
      extra =
        _.append(ex.getClass().getName()).append(": ").append(ex.getMessage()),
    )
    emitEvent(method, Status.Failure, Option(ex), elapsedNanos)
  }

  private def trace(t: Throwable): Unit =
    if (!t.isInstanceOf[AssertionError] || settings.logAssert) logTrace(t)

  private def emitEvent(
      method: String,
      status: Status,
      throwable: Option[Throwable],
      elapsedNanos: Long,
  ): Unit = {
    val testName = taskDef.fullyQualifiedName() + "." +
      settings.decodeName(method)
    val selector = new TestSelector(testName)
    eventHandler.handle(new JUnitEvent(
      taskDef,
      testName,
      status,
      selector,
      new OptionalThrowable(throwable.orNull),
      elapsedNanos / 1000000L,
    ))
  }

  private def log(level: Level, s: String): Unit =
    if (settings.useSbtLoggers) for (l <- loggers) {
      val msg = filterAnsiIfNeeded(l, s)
      level match {
        case Debug => l.debug(msg)
        case Info => l.info(msg)
        case Warn => l.warn(msg)
        case Error => l.error(msg)
        case _ => l.error(msg)
      }
    }
    else level match {
      case Debug | Trace if !settings.verbose =>
      case _ => println(filterAnsiIfNeeded(isAnsiSupported, s))
    }

  private def filterAnsiIfNeeded(l: Logger, s: String): String =
    filterAnsiIfNeeded(l.ansiCodesSupported(), s)

  private def filterAnsiIfNeeded(isColorSupported: Boolean, s: String): String =
    if (isColorSupported && settings.color) s else AnsiColors.filterAnsi(s)

  private def logTrace(t: Throwable): Unit = {
    val trace = t.getStackTrace.dropWhile(p =>
      p.getFileName != null && {
        p.getFileName.contains("StackTrace.scala") ||
        p.getFileName.contains("Throwables.scala")
      }
    )
    val testFileName = if (settings.color) findTestFileName(trace) else null
    val i = trace.indexWhere(p =>
      p.getFileName != null && p.getFileName.contains("JUnitExecuteTest.scala")
    ) - 1
    val m = if (i > 0) i else trace.length - 1
    logStackTracePart(trace, m, trace.length - m - 1, t, testFileName)
  }

  private def logStackTracePart(
      trace: Array[StackTraceElement],
      m: Int,
      framesInCommon: Int,
      t: Throwable,
      testFileName: String,
  ): Unit = {
    val m0 = m
    var m2 = m
    var top = 0
    var i = top
    while (i <= m2) {
      if (
        trace(i).toString.startsWith("org.junit.") ||
        trace(i).toString.startsWith("org.hamcrest.")
      )
        if (i == top) top += 1
        else {
          m2 = i - 1
          var break = false
          while (m2 > top && !break) {
            val s = trace(m2).toString
            if (
              !s.startsWith("java.lang.reflect.") &&
              !s.startsWith("sun.reflect.")
            ) break = true
            else m2 -= 1
          }
          i = m2 // break
        }
      i += 1
    }

    for (i <- top to m2)
      log(Error, stackTraceElementToString(trace(i), testFileName))
    if (m0 != m2)
      // skip junit-related frames
      log(Error, "    ...")
    else if (framesInCommon != 0)
      // skip frames that were in the previous trace too
      log(Error, "    ... " + framesInCommon + " more")
    logStackTraceAsCause(trace, t.getCause, testFileName)
  }

  private def logStackTraceAsCause(
      causedTrace: Array[StackTraceElement],
      t: Throwable,
      testFileName: String,
  ): Unit = if (t != null) {
    val trace = t.getStackTrace
    var m = trace.length - 1
    var n = causedTrace.length - 1
    while (m >= 0 && n >= 0 && trace(m) == causedTrace(n)) {
      m -= 1
      n -= 1
    }
    log(Error, "Caused by: " + t)
    logStackTracePart(trace, m, trace.length - 1 - m, t, testFileName)
  }

  private def findTestFileName(trace: Array[StackTraceElement]): String = trace
    .find(_.getClassName == taskDef.fullyQualifiedName()).map(_.getFileName)
    .orNull

  private def stackTraceElementToString(
      e: StackTraceElement,
      testFileName: String,
  ): String = {
    val highlight = settings.color && {
      // This logic assumes that users have configured Scala.js sourcemaps.
      e.getFileName() != null && e.getFileName().contains("file:/")
    }
    val canHighlight = !PlatformCompat.isNative
    new StringBuilder().append(AnsiColors.use(AnsiColors.Reset)).append(
      if (!canHighlight) ""
      else if (highlight) AnsiColors.use(AnsiColors.Bold)
      else AnsiColors.use(AnsiColors.DarkGrey)
    ).append("    at ")
      .append(settings.decodeName(e.getClassName + '.' + e.getMethodName))
      .append('(').append(
        if (e.isNativeMethod()) "Native Method"
        else if (e.getFileName() == null) "Unknown Source"
        else {
          val file = e.getFileName().indexOf("file:/")
          val https = if (file >= 0) file else e.getFileName().indexOf("https:/")
          val filename =
            if (https >= 0) e.getFileName().substring(https) else e.getFileName()
          if (e.getLineNumber() >= 0) s"$filename:${e.getLineNumber()}"
          else filename
        }
      ).append(')').append(AnsiColors.use(AnsiColors.Reset)).toString()
  }
}

object JUnitReporter {
  private val Trace = 0
  private val Debug = 1
  private val Info = 2
  private val Warn = 3
  private val Error = 4
  private type Level = Int
}
