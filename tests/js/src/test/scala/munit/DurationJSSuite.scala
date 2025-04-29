package munit

import munit.internal.PlatformCompat

import sbt.testing.Event
import sbt.testing.EventHandler
import sbt.testing.Runner
import sbt.testing.SuiteSelector
import sbt.testing.Task
import sbt.testing.TaskDef

import scala.collection.mutable

class DurationJSTestSuite extends FunSuite {
  test("success")(assert(true, "Test should pass"))
  test("ignore".ignore)(assert(false, "Test should be ignored"))
}

class DurationJSSuite extends FunSuite {
  private def run(fullyQualifiedName: String): List[Event] = {
    val framework = new Framework()
    val runner: Runner = framework
      .runner(Array(), Array(), PlatformCompat.getThisClassLoader)
    val events = new mutable.ListBuffer[Event]
    val eventHandler: EventHandler = new EventHandler {
      override def handle(event: Event) = events += event
    }
    val taskDef: TaskDef = new TaskDef(
      fullyQualifiedName,
      framework.munitFingerprint,
      false,
      Array(new SuiteSelector()),
    )
    val tasks: Array[Task] = runner.tasks(Array(taskDef))
    assertEquals(tasks.size, 1)
    tasks(0).execute(eventHandler, Nil.toArray)
    events.toList
  }

  test("duration-defined") {
    val events: List[Event] = run("munit.DurationJSTestSuite")
    events.foreach(event =>
      assert(event.duration != -1L, "duration should be defined")
    )
  }
}
