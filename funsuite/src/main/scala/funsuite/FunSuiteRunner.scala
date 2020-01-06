package funsuite

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import java.lang.reflect.Modifier
import org.junit.runner.notification.Failure
import scala.util.control.NonFatal
import java.{util => ju}
import fansi.ErrorMode.Throw
import funsuite.internal.StackMarker
import org.junit.AssumptionViolatedException
import fansi.Color

final class FunSuiteRunner(cls: Class[_ <: FunSuite])
    extends org.junit.runner.Runner {
  require(
    hasEligibleConstructor(),
    s"Class '${cls.getCanonicalName()}' is missing a public empty argument constructor"
  )
  lazy val suite = cls.newInstance()
  private val suiteDescription = Description.createSuiteDescription(cls)

  def createTestDescription(test: Test): Description = {
    Description.createTestDescription(cls, test.name, test.location)
  }

  override def getDescription(): Description = {
    val description = Description.createSuiteDescription(cls)
    try {
      val suiteTests = StackMarker.dropOutside(suite.tests)
      suiteTests.foreach { test =>
        description.addChild(createTestDescription(test))
      }
    } catch {
      case ex: Throwable =>
        StackMarker.trimStackTrace(ex)
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
      StackMarker.dropOutside(thunk)
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
    val description = Description.createTestDescription(cls, name)
    notifier.fireTestStarted(description)
    StackMarker.trimStackTrace(ex)
    notifier.fireTestFailure(new Failure(suiteDescription, ex))
    notifier.fireTestFinished(description)
  }

  def runBeforeAll(notifier: RunNotifier): Boolean = {
    runHiddenTest(notifier, "beforeAll", suite.beforeAll(new BeforeAll))
  }
  def runAfterAll(notifier: RunNotifier): Boolean = {
    runHiddenTest(notifier, "afterAll", suite.afterAll(new AfterAll))
  }

  def runBeforeEach(notifier: RunNotifier, test: Test): Boolean = {
    runHiddenTest(
      notifier,
      s"beforeEach.${test.name}",
      suite.beforeEach(new BeforeEach(test))
    )
  }
  def runAfterEach(notifier: RunNotifier, test: Test): Boolean = {
    runHiddenTest(
      notifier,
      s"afterEach.${test.name}",
      suite.afterEach(new AfterEach(test))
    )
  }

  def runAll(notifier: RunNotifier): Unit = {
    try {
      val isContinue = runBeforeAll(notifier)
      if (isContinue) {
        suite.tests.foreach { test =>
          val description = createTestDescription(test)
          var isContinue = runBeforeEach(notifier, test)
          if (isContinue) {
            notifier.fireTestStarted(description)
            try {
              StackMarker.dropOutside(test.body()) match {
                case f: FlakyFailure =>
                  notifier.fireTestAssumptionFailed(new Failure(description, f))
                case Ignore =>
                  notifier.fireTestIgnored(description)
                case _ =>
              }
            } catch {
              case ex: AssumptionViolatedException =>
                StackMarker.trimStackTrace(ex)
              case NonFatal(ex) =>
                StackMarker.trimStackTrace(ex)
                val failure = new Failure(description, ex)
                ex match {
                  case _: AssumptionViolatedException =>
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
      }
    } finally {
      runAfterAll(notifier)
    }
  }

  private def hasEligibleConstructor(): Boolean = {
    try {
      val constructor = cls.getConstructor(
        new Array[java.lang.Class[T] forSome { type T }](0): _*
      )
      Modifier.isPublic(constructor.getModifiers)
    } catch {
      case nsme: NoSuchMethodException => false
    }
  }
}
