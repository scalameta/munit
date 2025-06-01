/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import munit.internal.PlatformCompat

import sbt.testing._

/* Implementation note: In JUnitTask we use Future[Try[Unit]] instead of simply
 * Future[Unit]. This is to prevent Scala's Future implementation to box/wrap
 * fatal errors (most importantly AssertionError) in ExecutionExceptions. We
 * need to prevent the wrapping in order to hide the fact that we use async
 * under the hood and stay consistent with JVM JUnit.
 */
final class JUnitTask(
    _taskDef: TaskDef,
    runSettings: RunSettings,
    classLoader: ClassLoader,
) extends Task {

  override def taskDef(): TaskDef = _taskDef
  override def tags(): Array[String] = Array.empty

  def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    def reporter =
      new JUnitReporter(eventHandler, loggers, runSettings, _taskDef)
    try PlatformCompat.newRunner(_taskDef, classLoader).foreach { runner =>
        runner.filter(runSettings.tags)
        runner.run(new MUnitRunNotifier(reporter))
      }
    catch { case ex: Throwable => reporter.reportTestSuiteError(ex) }
    Array()
  }

}
