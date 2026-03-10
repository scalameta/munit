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
  private var failedCount = 0
  private var skippedCount = 0
  private var ignoredCount = 0
  private var totalCount = 0
  private val buffer =
    if (settings.useBufferedLogger) new PlatformCompat.LogBuffer else null

  private def logEvent(
      method: String = "",
      color: String = null,
      fq: Boolean = false,
  )(
      prefix: String = "",
      suffix: String = "",
      nanos: Long = -1,
      cause: Throwable = null,
      extra: StringBuilder => Unit = null,
  ): Unit = {
    implicit val sb = new StringBuilder()
    if (prefix.nonEmpty) sb.append(prefix).append(' ')
    AnsiColors.c(color, flag = true) { sb =>
      if (fq) {
        sb.append(taskDef.fullyQualifiedName())
        if (method.nonEmpty) sb.append('.')
      }
      sb.append(method)
    }
    sb.append(suffix)
    if (nanos >= 0) {
      sb.append(' ')
      AnsiColors
        .c(AnsiColors.DarkGrey, flag = true)(_.append("%.3fs".format(nanos / 1e9)))
    }
    if (cause ne null) sb.append(' ').append(cause.getClass().getName())
      .append(": ").append(cause.getMessage())
    if (extra ne null) extra(sb.append(' '))
    log(Info, sb.toString())
  }

  def reportTestSuiteStarted(): Unit = {
    suiteStartNanos = System.nanoTime
    if (settings.shouldLogInfo)
      logEvent(color = AnsiColors.CYAN, fq = true)("Test run", " started")
    if (settings.shouldLogDebug)
      logEvent(color = AnsiColors.GREEN, fq = true)(suffix = ":")
  }

  def reportTestSuiteFinished(): Unit = if (settings.shouldLogInfo) {
    val nanos = System.nanoTime - suiteStartNanos
    logEvent(color = AnsiColors.GREEN, fq = true)(
      suffix = ": finished",
      nanos = nanos,
    )
    val sb = new StringBuilder()
    def appendCount(cnt: Int, label: String, color: String): Unit = {
      val useColor = cnt != 0 && color != null && !AnsiColors.noColor
      if (useColor) sb.append(color)
      sb.append(cnt).append(label)
      if (useColor) sb.append(AnsiColors.Reset)
    }
    sb.append(" finished: ")
    appendCount(failedCount, " failed", AnsiColors.RED)
    sb.append(", ")
    appendCount(ignoredCount, " ignored", AnsiColors.YELLOW)
    sb.append(", ").append(totalCount).append(" total")
    logEvent(color = AnsiColors.CYAN, fq = true)(
      prefix = "Test run",
      suffix = sb.toString,
      nanos = nanos,
    )
    flush()
  }

  def reportTestSuiteError(ex: Throwable): Unit = {
    logEvent(color = AnsiColors.LightRed, fq = true)(s"==> X", cause = ex)
    emitEvent("", Status.Error, Option(ex), 0)
    flush()
  }

  def reportTestStarted(method: String): Unit = {
    totalCount += 1
    if (settings.shouldLogTrace) logEvent(method)(suffix = " started")
  }

  def reportTestIgnored(method: String, suffix: String): Unit = {
    ignoredCount += 1
    val suffixed = if (suffix.isEmpty) "" else s" $suffix"
    if (settings.shouldLogWarn) logEvent(method, AnsiColors.YELLOW)(
      "==> i",
      suffixed + " ignored",
      nanos = System.nanoTime - suiteStartNanos,
    )
    emitEvent(method, Status.Ignored, None, 0)
  }

  def reportAssumptionViolation(method: String, e: Throwable): Unit = {
    skippedCount += 1
    if (settings.shouldLogWarn)
      logEvent(method, AnsiColors.YELLOW)("==> s", " skipped")
    emitEvent(method, Status.Skipped, Option(e), 0)
  }

  def reportTestPassed(method: String, elapsedNanos: Long): Unit = {
    if (settings.shouldLogDebug)
      logEvent(method, AnsiColors.GREEN)("  +", nanos = elapsedNanos)
    emitEvent(method, Status.Success, None, elapsedNanos)
  }

  def reportTestFailed(
      method: String,
      ex: Throwable,
      elapsedNanos: Long,
  ): Unit = {
    failedCount += 1
    logEvent(method, AnsiColors.LightRed, fq = true)(
      s"==> X",
      nanos = elapsedNanos,
      cause = ex,
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
    val suite = taskDef.fullyQualifiedName()
    val (testName, selector) =
      if (method.isEmpty) (suite, new SuiteSelector)
      else {
        val name = suite + "." + settings.decodeName(method)
        (name, new TestSelector(name))
      }
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
      val msg = filterAnsi(s, l)
      level match {
        case Debug | Trace => l.debug(msg)
        case Warn => l.warn(msg)
        case Error => l.error(msg)
        case _ => l.info(msg)
      }
    }
    else logNonSbt(filterAnsi(s, loggers: _*))

  private def logNonSbt(message: String): Unit =
    if (buffer eq null) println(message) else buffer.append(message)

  private[internal] def flush(): Unit = if (buffer ne null) {
    val out = buffer.flush()
    if (out.nonEmpty) println(out)
  }

  private def filterAnsi(s: String, loggers: Logger*): String =
    if (settings.color && loggers.forall(_.ansiCodesSupported())) s
    else AnsiColors.filterAnsi(s)

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
