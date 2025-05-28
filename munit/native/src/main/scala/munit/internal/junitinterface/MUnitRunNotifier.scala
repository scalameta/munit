package munit.internal.junitinterface

import java.util.concurrent._

import org.junit.runner.Description
import org.junit.runner.notification

class MUnitRunNotifier(reporter: JUnitReporter) extends notification.RunNotifier {
  private val status = new ConcurrentHashMap[String, MUnitRunNotifier.TestStatus]

  override def fireTestSuiteStarted(description: Description): Unit =
    reporter.reportTestSuiteStarted()

  override def fireTestStarted(description: Description): Unit = status
    .computeIfAbsent(
      description.getMethodName,
      methodName => {
        reporter.reportTestStarted(methodName)
        new MUnitRunNotifier.TestStatus()
      },
    )

  override def fireTestIgnored(description: Description): Unit = status.compute(
    description.getMethodName,
    (methodName, existingStatus) => {
      val status = existingStatus.orCreate
      if (status.ignore()) {
        val sb = new StringBuilder()
        val annotations = description.getAnnotations
        if (annotations.contains(munit.Pending)) sb.append("PENDING")
        annotations.foreach {
          case tag: munit.PendingComment =>
            if (sb.nonEmpty) sb.append(' ')
            sb.append(tag.value)
          case _ =>
        }
        reporter.reportTestIgnored(methodName, sb.toString())
      }
      status
    },
  )

  override def fireTestAssumptionFailed(failure: notification.Failure): Unit =
    status.compute(
      failure.description.getMethodName,
      (methodName, existingStatus) => {
        val status = existingStatus.orCreate
        if (status.fail(failure)) reporter
          .reportAssumptionViolation(methodName, failure.ex)
        status
      },
    )

  override def fireTestFailure(failure: notification.Failure): Unit = status
    .compute(
      failure.description.getMethodName,
      (methodName, existingStatus) => {
        val status = existingStatus.orCreate
        if (status.fail(failure)) reporter
          .reportTestFailed(methodName, failure.ex, status.elapsedNanos)
        status
      },
    )

  override def fireTestFinished(description: Description): Unit = status
    .computeIfPresent(
      description.getMethodName,
      (methodName, status) => {
        if (status.ok()) reporter
          .reportTestPassed(methodName, status.elapsedNanos)
        status
      },
    )

  override def fireTestSuiteFinished(description: Description): Unit = {}
}

object MUnitRunNotifier {

  private val none = new notification.Failure(null, null)
  private val ignored = new notification.Failure(null, null)

  class TestStatus() {
    private val startNanos: Long = System.nanoTime()
    private val completed: atomic.AtomicReference[notification.Failure] =
      new atomic.AtomicReference(null)

    def elapsedNanos: Long = System.nanoTime() - startNanos

    def ok(): Boolean = fail(none)
    def ignore(): Boolean = fail(ignored)
    def fail(failure: notification.Failure): Boolean = completed
      .compareAndSet(null, failure)
  }

  implicit class ImplicitTestStatus(private val status: TestStatus)
      extends AnyVal {
    def orCreate: TestStatus = if (status eq null) new TestStatus() else status
  }

}
