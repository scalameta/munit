package munit.sbtmunit

import sbt._
import scala.collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import sbt.testing.Status
import sbt.testing.Event
import java.{util => ju}
import java.text.SimpleDateFormat

class MUnitTestsListener(
    listener: MUnitReportListener,
    repository: String,
    reportName: String,
    ref: String,
    sha: String,
    scalaVersion: String,
    projectName: String
) extends TestsListener {
  private val groups =
    new ConcurrentHashMap[String, ConcurrentLinkedQueue[TestEvent]]
  @volatile
  private var currentGroup: String = "unknown"

  def startGroup(name: String): Unit = {
    currentGroup = name
  }
  def testEvent(event: TestEvent): Unit = {
    val group = groups.computeIfAbsent(
      currentGroup,
      (_: String) => new ConcurrentLinkedQueue[TestEvent]()
    )
    group.add(event)
  }
  def endGroup(name: String, t: Throwable): Unit = {}
  def endGroup(name: String, result: TestResult): Unit = {}
  def doInit(): Unit = {
    groups.clear()
  }
  def doComplete(finalResult: TestResult): Unit = {
    listener.onReport(newReport(finalResult))
  }

  val ISO_8601 =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", ju.Locale.US);
  private def newReport(testResult: TestResult): MUnitTestReport.Summary = {
    MUnitTestReport.Summary(
      repository = repository,
      ref = ref,
      sha = sha,
      timestamp = ISO_8601.format(new ju.Date()),
      scalaVersion = scalaVersion,
      projectName = projectName,
      javaVersion = System.getProperty("java.version"),
      os = System.getProperty("os.name"),
      groups = groups.asScala.iterator.map {
        case (group, events) =>
          MUnitTestReport.Group(
            name = group,
            result = overallResult(events.asScala).toString,
            events = events.asScala.iterator
              .flatMap(_.detail)
              .map(newTestEvent)
              .toArray
          )
      }.toArray
    )
  }

  private def newTestEvent(event: Event): MUnitTestReport.TestEvent = {
    MUnitTestReport.TestEvent(
      status = event.status().toString(),
      name = event.fullyQualifiedName(),
      duration = event.duration(),
      exception =
        if (event.throwable().isEmpty()) null
        else newTestException(event.throwable().get())
    )
  }

  private def newTestException(ex: Throwable): MUnitTestReport.TestException = {
    if (ex == null) null
    else {
      val plainMessage = Option(ex.getMessage()).map(filterAnsi).getOrElse("")
      val plainClassName = ex.getClass().getName()
      val (className, message) =
        if (plainClassName == "sbt.ForkMain$ForkError") {
          // When `fork := true`, the exception has class name
          // `sbt.ForkMain$ForkError` and the underlying exception class name is
          // formatted in the message.
          val colon = plainMessage.indexOf(": ")
          val space = plainMessage.indexOf(' ')
          val customClassNameHasSpace = space >= 0 && space < colon
          if (colon < 0 || customClassNameHasSpace) {
            (plainClassName, plainMessage)
          } else {
            (
              plainMessage.substring(0, colon),
              plainMessage.substring(colon + 2)
            )
          }
        } else {
          (plainClassName, plainMessage)
        }
      MUnitTestReport.TestException(
        className = className,
        message = message,
        stack = Option(ex.getStackTrace()).getOrElse(Array()).map(_.toString),
        cause = newTestException(ex.getCause())
      )
    }
  }
  private def filterAnsi(s: String): String = {
    if (s == null) {
      null
    } else {
      var r: String = ""
      val len = s.length
      var i = 0
      while (i < len) {
        val c = s.charAt(i)
        if (c == '\u001B') {
          i += 1
          while (i < len && s.charAt(i) != 'm') i += 1
        } else {
          r += c
        }
        i += 1
      }
      r
    }
  }
  private def overallResult(events: Iterable[TestEvent]): TestResult = {
    events.iterator
      .flatMap(_.detail)
      .foldLeft(TestResult.Passed: TestResult) { (sum, event) =>
        (sum, event.status) match {
          case (TestResult.Error, _)  => TestResult.Error
          case (_, Status.Error)      => TestResult.Error
          case (TestResult.Failed, _) => TestResult.Failed
          case (_, Status.Failure)    => TestResult.Failed
          case _                      => TestResult.Passed
        }
      }
  }
}
