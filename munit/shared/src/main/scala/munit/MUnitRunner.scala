package munit

import munit.internal.PlatformCompat
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import java.lang.reflect.Modifier

import munit.internal.console.StackTraces
import org.junit.runner.notification.Failure

import scala.util.control.NonFatal
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.Filter
import org.junit.runner.Runner

import scala.collection.mutable

class MUnitRunner(val cls: Class[_ <: Suite], newInstance: () => Suite)
    extends Runner
    with Filterable {
  def this(cls: Class[_ <: Suite]) =
    this(MUnitRunner.ensureEligibleConstructor(cls), () => cls.newInstance())
  val suite: Suite = newInstance()
  private val suiteDescription = Description.createSuiteDescription(cls)
  @volatile private var filter: Filter = Filter.ALL
  val descriptions: mutable.Map[suite.Test, Description] =
    mutable.Map.empty[suite.Test, Description]
  val testNames: mutable.Set[String] = mutable.Set.empty[String]

  def filter(filter: Filter): Unit = {
    this.filter = filter
  }

  def createTestDescription(test: suite.Test): Description = {
    descriptions.getOrElseUpdate(
      test, {
        val testName = munit.internal.Compat.LazyList
          .from(0)
          .map {
            case 0 => test.name
            case n => s"${test.name}-${n}"
          }
          .find(candidate => !testNames.contains(candidate))
          .head
        testNames += testName
        Description.createTestDescription(cls, testName, test.location)
      }
    )
  }

  override def getDescription(): Description = {
    val description = Description.createSuiteDescription(cls)
    try {
      val suiteTests = StackTraces.dropOutside(suite.munitTests())
      suiteTests.foreach { test =>
        val testDescription = createTestDescription(test)
        if (filter.shouldRun(testDescription)) {
          description.addChild(testDescription)
        }
      }
    } catch {
      case ex: Throwable =>
        StackTraces.trimStackTrace(ex)
        // Print to stdout because we don't have access to a RunNotifier
        ex.printStackTrace()
        Nil
    }
    description
  }

  override def run(notifier: RunNotifier): Unit = {
    notifier.fireTestSuiteStarted(suiteDescription)
    try {
      runAll(notifier)
    } catch {
      case ex: Throwable =>
        fireHiddenTest(notifier, "expected error running tests", ex)
    } finally {
      notifier.fireTestSuiteFinished(suiteDescription)
    }
  }

  private def runHiddenTest(
      notifier: RunNotifier,
      name: String,
      thunk: => Unit
  ): Boolean = {
    try {
      StackTraces.dropOutside(thunk)
      true
    } catch {
      case ex: Throwable =>
        fireHiddenTest(notifier, name, ex)
        false
    }
  }

  private def fireHiddenTest(
      notifier: RunNotifier,
      name: String,
      ex: Throwable
  ): Unit = {
    val test = new suite.Test(name, () => ???, Set.empty, Location.empty)
    val description = createTestDescription(test)
    notifier.fireTestStarted(description)
    StackTraces.trimStackTrace(ex)
    notifier.fireTestFailure(new Failure(description, ex))
    notifier.fireTestFinished(description)
  }

  def runBeforeAll(notifier: RunNotifier): Boolean = {
    var isContinue = runHiddenTest(notifier, "beforeAll", suite.beforeAll())
    suite.munitFixtures.foreach { fixture =>
      isContinue &= runHiddenTest(
        notifier,
        s"beforeAllFixture(${fixture.fixtureName})",
        fixture.beforeAll()
      )
    }
    isContinue
  }
  def runAfterAll(notifier: RunNotifier): Boolean = {
    var isContinue = true
    suite.munitFixtures.foreach { fixture =>
      isContinue &= runHiddenTest(
        notifier,
        s"afterAllFixture(${fixture.fixtureName})",
        fixture.afterAll()
      )
    }
    isContinue &= runHiddenTest(notifier, "afterAll", suite.afterAll())
    isContinue
  }

  def runBeforeEach(
      notifier: RunNotifier,
      test: suite.Test
  ): Boolean = {
    val beforeEach = new GenericBeforeEach(test)
    var isContinue = runHiddenTest(
      notifier,
      s"beforeEach(${test.name})",
      suite.beforeEach(beforeEach)
    )
    suite.munitFixtures.foreach { fixture =>
      isContinue &= runHiddenTest(
        notifier,
        s"beforeEachFixture(${test.name}, ${fixture.fixtureName})",
        fixture.beforeEach(beforeEach)
      )
    }
    isContinue
  }
  def runAfterEach(
      notifier: RunNotifier,
      test: suite.Test
  ): Boolean = {
    var isContinue = true
    val afterEach = new GenericAfterEach(test)
    suite.munitFixtures.foreach { fixture =>
      isContinue &= runHiddenTest(
        notifier,
        s"afterEachFixture(${test.name}, ${fixture.fixtureName})",
        fixture.afterEach(afterEach)
      )
    }
    isContinue &= runHiddenTest(
      notifier,
      s"afterEach(${test.name})",
      suite.afterEach(afterEach)
    )
    isContinue
  }

  def runTest(
      notifier: RunNotifier,
      test: suite.Test
  ): Unit = {
    val description = createTestDescription(test)
    if (!filter.shouldRun(description)) {
      return
    }
    var isContinue = runBeforeEach(notifier, test)
    if (isContinue) {
      notifier.fireTestStarted(description)
      try {
        StackTraces.dropOutside(test.body()) match {
          case f: TestValues.FlakyFailure =>
            notifier.fireTestAssumptionFailed(new Failure(description, f))
          case TestValues.Ignore =>
            notifier.fireTestIgnored(description)
          case _ =>
        }
      } catch {
        case ex: DottyBugAssumptionViolatedException =>
          StackTraces.trimStackTrace(ex)
        case NonFatal(ex) =>
          StackTraces.trimStackTrace(ex)
          val failure = new Failure(description, ex)
          ex match {
            case _: DottyBugAssumptionViolatedException =>
              notifier.fireTestAssumptionFailed(failure)
            case _ =>
              notifier.fireTestFailure(failure)
          }
      } finally {
        notifier.fireTestFinished(description)
        runAfterEach(notifier, test)
      }
    }
  }

  def runAll(notifier: RunNotifier): Unit = {
    if (PlatformCompat.isIgnoreSuite(cls)) {
      notifier.fireTestIgnored(suiteDescription)
      return
    }
    try {
      val isContinue = runBeforeAll(notifier)
      if (isContinue) {
        suite.munitTests().foreach { test =>
          runTest(notifier, test)
        }
      }
    } finally {
      runAfterAll(notifier)
    }
  }

}
object MUnitRunner {
  def run(suite: Suite, notifier: RunNotifier): Unit = {}
  private def ensureEligibleConstructor(
      cls: Class[_ <: Suite]
  ): Class[_ <: Suite] = {
    require(
      hasEligibleConstructor(cls),
      s"Class '${cls.getName}' is missing a public empty argument constructor"
    )
    cls
  }
  private def hasEligibleConstructor(cls: Class[_ <: Suite]): Boolean = {
    try {
      val constructor = cls.getConstructor(
        new Array[java.lang.Class[_]](0): _*
      )
      Modifier.isPublic(constructor.getModifiers)
    } catch {
      case nsme: NoSuchMethodException => false
    }
  }
}
