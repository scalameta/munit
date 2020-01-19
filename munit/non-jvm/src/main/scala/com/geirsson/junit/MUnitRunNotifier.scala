package com.geirsson.junit

import org.junit.runner.{Description, notification}
import org.junit.runner.notification.RunNotifier

import scala.collection.mutable

class MUnitRunNotifier(reporter: JUnitReporter) extends RunNotifier {
  var ignored = 0
  var total = 0
  var startedTimestamp = 0L
  val isFailed: mutable.Set[String] = mutable.Set.empty[String]
  override def fireTestSuiteStarted(description: Description): Unit = {
    reporter.reportTestSuiteStarted()
  }
  override def fireTestStarted(description: Description): Unit = {
    startedTimestamp = System.nanoTime()
    reporter.reportTestStarted(description.getMethodName)
  }
  def elapsedSeconds(): Double = {
    val elapsedNanos = System.nanoTime() - startedTimestamp
    elapsedNanos / 1000000000.0
  }
  override def fireTestIgnored(description: Description): Unit = {
    ignored += 1
    reporter.reportTestIgnored(description.getMethodName)
  }
  override def fireTestAssumptionFailed(
      failure: notification.Failure
  ): Unit = {
    reporter.reportAssumptionViolation(
      failure.description.getMethodName,
      elapsedSeconds(),
      failure.ex
    )
  }
  override def fireTestFailure(failure: notification.Failure): Unit = {
    val methodName = failure.description.getMethodName
    isFailed += methodName
    reporter.reportTestFailed(
      methodName,
      failure.ex,
      elapsedSeconds()
    )
  }
  override def fireTestFinished(description: Description): Unit = {
    val methodName = description.getMethodName
    total += 1
    if (!isFailed(methodName)) {
      reporter.reportTestPassed(
        methodName,
        elapsedSeconds()
      )
    }
  }
  override def fireTestSuiteFinished(description: Description): Unit = {}
}
