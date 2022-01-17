package munit.internal.junitinterface

import org.junit.runner.{Description, notification}
import org.junit.runner.notification.RunNotifier

import scala.collection.mutable

class MUnitRunNotifier(reporter: JUnitReporter) extends RunNotifier {
  var ignored = 0
  var total = 0
  var startedTimestamp = 0L
  val isReported: mutable.Set[Description] = mutable.Set.empty[Description]
  override def fireTestSuiteStarted(description: Description): Unit = {
    reporter.reportTestSuiteStarted()
  }
  override def fireTestStarted(description: Description): Unit = {
    startedTimestamp = System.nanoTime()
    reporter.reportTestStarted(description.getMethodName)
  }
  def elapsedMillis(): Double = {
    val elapsedNanos = System.nanoTime() - startedTimestamp
    elapsedNanos / 1000000.0
  }
  override def fireTestIgnored(description: Description): Unit = {
    ignored += 1
    isReported += description
    reporter.reportTestIgnored(description.getMethodName)
  }
  override def fireTestAssumptionFailed(
      failure: notification.Failure
  ): Unit = {
    isReported += failure.description
    reporter.reportAssumptionViolation(
      failure.description.getMethodName,
      elapsedMillis(),
      failure.ex
    )
  }
  override def fireTestFailure(failure: notification.Failure): Unit = {
    val methodName = failure.description.getMethodName
    isReported += failure.description
    reporter.reportTestFailed(
      methodName,
      failure.ex,
      elapsedMillis()
    )
  }
  override def fireTestFinished(description: Description): Unit = {
    val methodName = description.getMethodName
    total += 1
    if (!isReported(description)) {
      reporter.reportTestPassed(
        methodName,
        elapsedMillis()
      )
    }
  }
  override def fireTestSuiteFinished(description: Description): Unit = {}
}
