package munit

import sbt.testing.Event
import sbt.testing.EventHandler
import sbt.testing.Runner
import sbt.testing.Selector
import sbt.testing.Status
import sbt.testing.SuiteSelector
import sbt.testing.TaskDef
import sbt.testing.TestSelector

import scala.collection.mutable

/**
 * Dummy test suite which is needed by TestSelectorSuite
 */
class MyTestSuite extends FunSuite {

  test("testFoo")(assert(true, "Test should pass"))

  test("testBar")(assert(true, "Test should pass"))

  test("testBar(")(assert(true, "Test should pass"))
}

/**
 * Check if TestSelector's are correctly handled by Munit.
 * Execute prepared TaskDef's using manually created instances of sbt.testing.{Framework and Runner}.
 */
class TestSelectorSuite extends FunSuite {
  val framework = new Framework();
  val runner: Runner = framework
    .runner(Array.empty, Array.empty, this.getClass().getClassLoader());

  val fingerprint = framework.munitFingerprint

  /**
   * Reference to the collection which will contain fully qualified names of executed tests.
   * Only after finished execution it's safe to convert this mutable collection to immutable one.
   */
  private def getEventHandler(): (mutable.ListBuffer[String], EventHandler) = {
    val executedItems = new mutable.ListBuffer[String]
    val eventHandler = new EventHandler {
      override def handle(event: Event) = if (event.status() == Status.Success)
        executedItems += event.fullyQualifiedName()
    }
    (executedItems, eventHandler)
  }

  private def getTaskDefs(selectors: Array[Selector]): Array[TaskDef] =
    Array(new TaskDef("munit.MyTestSuite", fingerprint, false, selectors))

  test("runAllViaSuiteSelector") {
    val selectors = Array[Selector](new SuiteSelector)
    val taskDefs =
      Array(new TaskDef("munit.MyTestSuite", fingerprint, false, selectors))

    val tasks = runner.tasks(taskDefs)
    assertEquals(tasks.size, 1)
    val task = tasks(0)

    val (executedItems, eventHandler) = getEventHandler()

    task.execute(eventHandler, Nil.toArray)
    assertEquals(
      executedItems.toSet,
      Set("munit.MyTestSuite.testBar", "munit.MyTestSuite.testFoo", "munit.MyTestSuite.testBar("),
    )
  }

  test("runAllViaTestSelectors") {
    val selectors =
      Array[Selector](new TestSelector("testFoo"), new TestSelector("testBar"))
    val taskDefs = getTaskDefs(selectors)

    val tasks = runner.tasks(taskDefs)
    assertEquals(tasks.size, 1)
    val task = tasks(0)

    val (executedItems, eventHandler) = getEventHandler()

    task.execute(eventHandler, Nil.toArray)
    assertEquals(
      executedItems.toSet,
      Set("munit.MyTestSuite.testBar", "munit.MyTestSuite.testFoo"),
    )
  }

  test("runOnlyOne") {
    val selectors = Array[Selector](new TestSelector("testFoo"))
    val taskDefs = getTaskDefs(selectors)

    val tasks = runner.tasks(taskDefs)
    assertEquals(tasks.size, 1)
    val task = tasks(0)

    val (executedItems, eventHandler) = getEventHandler()

    task.execute(eventHandler, Nil.toArray)
    assertEquals(executedItems.toSet, Set("munit.MyTestSuite.testFoo"))
  }

  test("runWithParentheses") {
    val selectors = Array[Selector](new TestSelector(raw"testBar\("))
    val taskDefs = getTaskDefs(selectors)

    val tasks = runner.tasks(taskDefs)
    assertEquals(tasks.size, 1)
    val task = tasks(0)

    val (executedItems, eventHandler) = getEventHandler()

    task.execute(eventHandler, Nil.toArray)
    assertEquals(executedItems.toSet, Set("munit.MyTestSuite.testBar("))
  }
}
