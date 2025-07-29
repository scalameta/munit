package munit

import munit.MUnitRunner.TestTeardownException
import munit.internal.PlatformCompat
import munit.internal.console.Printers
import munit.internal.console.StackTraces
import munit.internal.junitinterface.Configurable
import munit.internal.junitinterface.Settings

import java.lang.reflect.Modifier

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

import org.junit.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier

class MUnitRunner(val cls: Class[_ <: Suite], suite: Suite)
    extends Runner with Filterable with Configurable {

  def this(cls: Class[_ <: Suite], newInstance: () => Suite) =
    this(cls, newInstance())

  def this(cls: Class[_ <: Suite]) =
    this(MUnitRunner.ensureEligibleConstructor(cls), cls.newInstance())

  private implicit val ec: ExecutionContext = suite.munitExecutionContext

  @volatile
  private var settings: Settings = Settings.defaults()
  @volatile
  private var suiteAborted: Boolean = false

  private val descriptions: mutable.Map[Test, Description] = mutable.Map
    .empty[Test, Description]
  private val testNames: mutable.Set[String] = mutable.Set.empty[String]

  private lazy val munitTests: mutable.ArrayBuffer[Test] = mutable
    .ArrayBuffer[Test](suite.munitTests(): _*)

  // Suite fixtures are implemented as regular fixtures
  // We split up the before*/after* methods so we can order them explicitly
  private val suiteFixtureBefore = new Fixture[Unit](cls.getName()) {
    def apply(): Unit = ()
    override def beforeAll(): Unit = suite.beforeAll()
    override def beforeEach(context: BeforeEach): Unit = suite
      .beforeEach(context)
  }
  private val suiteFixtureAfter = new Fixture[Unit](cls.getName()) {
    def apply(): Unit = ()
    override def afterEach(context: AfterEach): Unit = suite.afterEach(context)
    override def afterAll(): Unit = suite.afterAll()
  }
  private lazy val munitFixtures: List[AnyFixture[_]] = suiteFixtureBefore ::
    suite.munitFixtures.toList ::: suiteFixtureAfter :: Nil

  override def filter(filter: Filter): Unit = {
    val newTests = munitTests
      .filter(t => filter.shouldRun(createTestDescription(t)))
    munitTests.clear()
    munitTests ++= newTests
  }
  override def configure(settings: Settings): Unit = this.settings = settings

  private def createTestDescription(test: Test): Description = descriptions
    .getOrElseUpdate(
      test, {
        val escapedName = Printers.escapeNonVisible(test.name)
        val testName = Iterator.from(0).map {
          case 0 => escapedName
          case n => s"$escapedName-$n"
        }.find(candidate => !testNames.contains(candidate)).head
        testNames += testName
        val desc = Description
          .createTestDescription(cls, testName, test.annotations: _*)
        desc
      },
    )

  override def getDescription(): Description = {
    val description = Description.createSuiteDescription(cls)

    try {
      val suiteTests = StackTraces.dropOutside(munitTests)
      suiteTests.iterator.map(createTestDescription)
        .foreach(description.addChild)
    } catch {
      case ex: Throwable =>
        trimStackTrace(ex)
        // Print to stdout because we don't have access to a RunNotifier
        ex.printStackTrace()
    }

    description
  }

  override def run(notifier: RunNotifier): Unit = PlatformCompat
    .awaitResult(runAsync(notifier))
  def runAsync(notifier: RunNotifier): Future[Unit] = {
    val description = getDescription()
    if (PlatformCompat.isIgnoreSuite(cls) || munitTests.isEmpty) {
      notifier.fireTestIgnored(description)
      Future.successful(())
    } else {
      notifier.fireTestSuiteStarted(description)
      runBeforeAll(notifier).flatMap { beforeAll =>
        val body =
          if (!beforeAll.isSuccess) Future.successful(Nil)
          else sequenceFutures(munitTests.iterator.map(runTest(notifier, _)))
        body.transform { res =>
          runAfterAll(notifier, beforeAll)
          res.failed.foreach(ex =>
            fireFailedHiddenTest(notifier, "unexpected error running tests", ex)
          )
          notifier.fireTestSuiteFinished(description)
          util.Success(())
        }
      }
    }
  }

  // Similar `Future.sequence` but with cleaner stack traces for non-async code.
  private def sequenceFutures[A](
      futures: Iterator[Future[A]]
  ): Future[List[Try[A]]] = {
    val acc = mutable.ListBuffer.empty[Try[A]]
    def loop(): Future[List[Try[A]]] =
      if (!futures.hasNext) Future.successful(acc.toList)
      else {
        val future = futures.next()
        future.value match {
          // use tail-recursive call if possible to keep stack traces clean.
          case Some(t) => acc += t; loop()
          case None => future.transformWith { t => acc += t; loop() }
        }
      }
    loop()
  }

  private[munit] class BeforeAllResult(
      val isSuccess: Boolean,
      val loadedFixtures: List[AnyFixture[_]],
      val errors: List[Throwable],
  )

  private def runBeforeAll(notifier: RunNotifier): Future[BeforeAllResult] = {
    val result: Future[List[Try[(AnyFixture[_], Boolean)]]] = sequenceFutures(
      munitFixtures.iterator.map(f =>
        runHiddenTest(
          notifier,
          s"beforeAll(${f.fixtureName})",
          () => f.beforeAll(),
        ).map(isSuccess => f -> isSuccess)
      )
    )
    result.map { results =>
      val loadedFixtures = List.newBuilder[AnyFixture[_]]
      val errors = List.newBuilder[Throwable]
      var isSuccess = true
      results.foreach {
        case util.Failure(ex) => errors += ex; isSuccess = false
        case util.Success((fixture, success)) =>
          if (success) loadedFixtures += fixture else isSuccess = false
      }
      new BeforeAllResult(isSuccess, loadedFixtures.result(), errors.result())
    }
  }

  private def runAfterAll(
      notifier: RunNotifier,
      beforeAll: BeforeAllResult,
  ): Future[List[Try[Boolean]]] =
    sequenceFutures[Boolean](beforeAll.loadedFixtures.iterator.map(f =>
      runHiddenTest(notifier, s"afterAll(${f.fixtureName})", () => f.afterAll())
    ))

  private def runBeforeEach(test: Test): Future[BeforeAllResult] = {
    val context = new BeforeEach(test)
    sequenceFutures(
      munitFixtures.iterator
        .map(f => valueTransform(() => f.beforeEach(context)).map(_ => f))
    ).map { results =>
      val loadedFixtures = List.newBuilder[AnyFixture[_]]
      val errors = List.newBuilder[Throwable]
      results.foreach {
        case util.Failure(ex) => errors += ex
        case util.Success(fixture) => loadedFixtures += fixture
      }
      val errorList = errors.result()
      new BeforeAllResult(errorList.isEmpty, loadedFixtures.result(), errorList)
    }
  }

  private def runAfterEach(
      test: Test,
      fixtures: List[AnyFixture[_]],
  ): Future[List[Try[_]]] = {
    val context = new AfterEach(test)
    sequenceFutures(
      fixtures.iterator.map(f => valueTransform(() => f.afterEach(context)))
    )
  }

  private def runTest(notifier: RunNotifier, test: Test): Future[Boolean] = {
    val description = createTestDescription(test)

    if (suiteAborted) {
      notifier.fireTestAssumptionFailed(new Failure(
        description,
        new FailSuiteException("Suite has been aborted", test.location),
      ))
      return Future.successful(false)
    }

    if (test.tags(Ignore)) {
      notifier.fireTestIgnored(description)
      return Future.successful(false)
    }

    notifier.fireTestStarted(description)
    def handleNonFatalOrStackOverflow(ex: Throwable): Unit = {
      trimStackTrace(ex)
      val cause = Exceptions.rootCause(ex)
      val failure = new Failure(description, cause)
      cause match {
        case _: AssumptionViolatedException => notifier
            .fireTestAssumptionFailed(failure)
        case _: FailSuiteException =>
          suiteAborted = true
          notifier.fireTestFailure(failure)
        case _ => notifier.fireTestFailure(failure)
      }
    }

    def onError(ex: Throwable): Unit = ex match {
      case ex: AssumptionViolatedException =>
        trimStackTrace(ex)
        notifier.fireTestAssumptionFailed(new Failure(description, ex))
      case ex: TestTeardownException =>
        suiteAborted = true
        trimStackTrace(ex)
        notifier.fireTestFailure(new Failure(description, ex))
      case NonFatal(ex) => handleNonFatalOrStackOverflow(ex)
      case ex: StackOverflowError => handleNonFatalOrStackOverflow(ex)
      case ex =>
        suiteAborted = true
        notifier.fireTestFailure(new Failure(description, ex))
    }

    def onResult(res: Try[Unit]): Future[Boolean] = {
      res.failed.foreach(onError)
      notifier.fireTestFinished(description)
      Future.successful(!test.tags(Pending))
    }

    try runTestBody(notifier, description, test).transformWith(onResult)
    catch { case ex: Throwable => onResult(util.Failure(ex)) }
  }

  private def futureFromAny(any: Any): Future[Any] = any match {
    case f: Future[_] => f
    case _ => Future.successful(any)
  }

  private def runTestBody(
      notifier: RunNotifier,
      description: Description,
      test: Test,
  ): Future[Unit] = {
    import scala.util.{Failure => TryFailure}
    import scala.util.{Success => TrySuccess}
    import org.junit.runner.notification.{Failure => JunitFailure}

    def addSuppressed(firstError: Throwable, otherErrors: Seq[Throwable]) = {
      otherErrors.foreach(err => firstError.addSuppressed(err))
      firstError
    }

    runBeforeEach(test).flatMap[Any] { beforeEach =>
      sealed trait Outcome
      case class BeforeEachFailure(exceptions: List[Throwable]) extends Outcome
      case class TestFailure(exception: Throwable) extends Outcome
      case class TestSuccess(testResult: Any) extends Outcome

      (beforeEach.errors match {
        case Nil => StackTraces.dropOutside(test.body()).transform {
            case TrySuccess(testResult) => TrySuccess(TestSuccess(testResult))
            case TryFailure(testException) =>
              TrySuccess(TestFailure(testException))
          }
        case errors => Future.successful(BeforeEachFailure(errors))
      }).flatMap { outcome =>
        runAfterEach(test, beforeEach.loadedFixtures).transform {
          case TryFailure(afterEachException) =>
            TrySuccess(List(afterEachException))
          case TrySuccess(afterEachExceptions) =>
            TrySuccess(afterEachExceptions.collect { case TryFailure(e) => e })
        }.flatMap { afterEachErrors =>
          outcome match {
            case BeforeEachFailure(beforeEachErrors) => Future
                .failed(addSuppressed(
                  beforeEachErrors.head,
                  beforeEachErrors.tail ++ afterEachErrors,
                ))
            case TestFailure(testException) => Future
                .failed(addSuppressed(testException, afterEachErrors))
            case TestSuccess(testResult) =>
              if (afterEachErrors.isEmpty) Future.successful(testResult)
              else Future.failed(addSuppressed(
                new TestTeardownException(afterEachErrors.head),
                afterEachErrors.tail,
              ))
          }
        }
      }
    }.map {
      case f: TestValues.FlakyFailure =>
        trimStackTrace(f)
        notifier.fireTestAssumptionFailed(new JunitFailure(description, f))
      case TestValues.Ignore => notifier.fireTestIgnored(description)
      case _ if test.tags(Pending) => notifier.fireTestIgnored(description)
      case _ => ()
    }
  }

  private def runHiddenTest(
      notifier: RunNotifier,
      name: String,
      thunk: () => Any,
  ): Future[Boolean] =
    try StackTraces.dropOutside(valueTransform(thunk).transform {
        case util.Failure(exception) =>
          fireFailedHiddenTest(notifier, name, exception)
          util.Success(false)
        case _ => util.Success(true)
      })
    catch {
      case ex: Throwable =>
        fireFailedHiddenTest(notifier, name, ex)
        Future.successful(false)
    }

  private def fireFailedHiddenTest(
      notifier: RunNotifier,
      name: String,
      ex: Throwable,
  ): Unit = {
    val test = new Test(name, () => ???, Set.empty, Location.empty)
    val description = createTestDescription(test)
    notifier.fireTestStarted(description)
    trimStackTrace(ex)
    notifier.fireTestFailure(new Failure(description, Exceptions.rootCause(ex)))
    notifier.fireTestFinished(description)
  }
  private def trimStackTrace(ex: Throwable): Unit =
    if (settings.trimStackTraces) StackTraces.trimStackTrace(ex)

  private def valueTransform(thunk: () => Any): Future[Any] = suite match {
    case funSuite: FunSuite => funSuite.munitValueTransform(thunk())
    case _ => futureFromAny(thunk())
  }

}
object MUnitRunner {
  private def ensureEligibleConstructor(
      cls: Class[_ <: Suite]
  ): Class[_ <: Suite] = {
    require(
      hasEligibleConstructor(cls),
      s"Class '${cls.getName}' is missing a public empty argument constructor",
    )
    cls
  }
  private def hasEligibleConstructor(cls: Class[_ <: Suite]): Boolean =
    try {
      val constructor = cls.getConstructor(new Array[java.lang.Class[_]](0): _*)
      Modifier.isPublic(constructor.getModifiers)
    } catch { case nsme: NoSuchMethodException => false }

  /**
   * Exception used to wrap any throwables thrown in afterEach, in order to communicate to the runner that
   * the suite should be aborted.
   */
  private class TestTeardownException(cause: Throwable)
      extends RuntimeException("Teardown of test failed", cause)
}
