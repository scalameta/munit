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
import scala.util.Try

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
        val escapedName = test.name.replaceAllLiterally("\n", "\\n")
        val testName = munit.internal.Compat.LazyList
          .from(0)
          .map {
            case 0 => escapedName
            case n => s"${escapedName}-${n}"
          }
          .find(candidate => !testNames.contains(candidate))
          .head
        testNames += testName
        Description.createTestDescription(
          cls,
          testName,
          test.annotations: _*
        )
      }
    )
  }

  override def getDescription(): Description = {
    try {
      val suiteTests = StackTraces.dropOutside(suite.munitTests())
      suiteTests.foreach { test =>
        val testDescription = createTestDescription(test)
        if (filter.shouldRun(testDescription)) {
          suiteDescription.addChild(testDescription)
        }
      }
    } catch {
      case ex: Throwable =>
        StackTraces.trimStackTrace(ex)
        // Print to stdout because we don't have access to a RunNotifier
        ex.printStackTrace()
        Nil
    }
    suiteDescription
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

  private def runBeforeAll(notifier: RunNotifier): Boolean = {
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
  private def runAfterAll(notifier: RunNotifier): Unit = {
    suite.munitFixtures.foreach { fixture =>
      runHiddenTest(
        notifier,
        s"afterAllFixture(${fixture.fixtureName})",
        fixture.afterAll()
      )
    }
    runHiddenTest(notifier, "afterAll", suite.afterAll())
  }

  class BeforeEachResult(
      val error: Option[Throwable],
      val loadedFixtures: List[suite.Fixture[_]]
  )
  private def runBeforeEach(
      test: suite.Test
  ): BeforeEachResult = {
    val beforeEach = new GenericBeforeEach(test)
    val fixtures = mutable.ListBuffer.empty[suite.Fixture[_]]
    val error = foreachUnsafe(
      List(() => suite.beforeEach(beforeEach)) ++
        suite.munitFixtures.map(fixture =>
          () => {
            fixture.beforeEach(beforeEach)
            fixtures += fixture
            ()
          }
        )
    )
    new BeforeEachResult(error.failed.toOption, fixtures.toList)
  }

  private def runAfterEach(
      test: suite.Test,
      fixtures: List[suite.Fixture[_]]
  ): Unit = {
    val afterEach = new GenericAfterEach(test)
    val error = foreachUnsafe(
      fixtures.map(fixture => () => fixture.afterEach(afterEach)) ++
        List(() => suite.afterEach(afterEach))
    )
    error.get // throw exception if it exists.
  }

  private def runAll(notifier: RunNotifier): Unit = {
    if (PlatformCompat.isIgnoreSuite(cls)) {
      notifier.fireTestIgnored(suiteDescription)
      return
    }
    var isSingleTestRun = false
    try {
      val isContinue = runBeforeAll(notifier)
      if (isContinue) {
        suite.munitTests().foreach { test =>
          val isTestRun = runTest(notifier, test)
          isSingleTestRun ||= isTestRun
        }
      }
    } finally {
      if (isSingleTestRun) {
        runAfterAll(notifier)
      }
    }
  }

  private def runTest(
      notifier: RunNotifier,
      test: suite.Test
  ): Boolean = {
    val description = createTestDescription(test)
    if (!filter.shouldRun(description)) {
      return false
    }
    notifier.fireTestStarted(description)
    try {
      val result = StackTraces.dropOutside {
        val beforeEachResult = runBeforeEach(test)
        beforeEachResult.error match {
          case None =>
            try test.body()
            finally runAfterEach(test, beforeEachResult.loadedFixtures)
          case Some(error) =>
            try runAfterEach(test, beforeEachResult.loadedFixtures)
            finally throw error
        }
      }
      result match {
        case f: TestValues.FlakyFailure =>
          StackTraces.trimStackTrace(f)
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
    }
    true
  }

  private def foreachUnsafe(thunks: Iterable[() => Unit]): Try[Unit] = {
    var errors = mutable.ListBuffer.empty[Throwable]
    thunks.foreach { thunk =>
      try {
        thunk()
      } catch {
        case ex if NonFatal(ex) =>
          errors += ex
      }
    }
    errors.toList match {
      case head :: tail =>
        tail.foreach { e =>
          if (e ne head) {
            head.addSuppressed(e)
          }
        }
        scala.util.Failure(head)
      case _ =>
        scala.util.Success(())
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
