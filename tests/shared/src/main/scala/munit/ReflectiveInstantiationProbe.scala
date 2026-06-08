package munit

import sbt.testing.{Event, EventHandler, Logger, Status, TaskDef}

class GuardSuite extends FunSuite {
  test("runs")(assert(true))
}

object NullLogger extends Logger {
  def ansiCodesSupported(): Boolean = false
  def error(x: String): Unit = ()
  def warn(x: String): Unit = ()
  def info(x: String): Unit = ()
  def debug(x: String): Unit = ()
  def trace(x: Throwable): Unit = ()
}

object ReflectiveInstantiationProbe {

  /**
   * Discovers and runs `fqcn` via the MUnit framework, returning the
   * number of successful test events (0 when discovery fails).
   */
  def countSuccesses(fqcn: String, classLoader: ClassLoader): Int = {
    val framework = new Framework
    val runner = framework.runner(Array.empty, Array.empty, classLoader)
    val taskDef =
      new TaskDef(fqcn, framework.munitFingerprint, false, Array.empty)
    val events = List.newBuilder[Event]
    val handler: EventHandler = event => events += event
    runner.tasks(Array(taskDef)).foreach(_.execute(handler, Array(NullLogger)))
    events.result().count(_.status() == Status.Success)
  }
}
