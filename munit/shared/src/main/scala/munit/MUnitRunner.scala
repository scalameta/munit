package munit

import munit.internal.FutureCompat._
import munit.internal.PlatformCompat
import munit.internal.console.Printers
import munit.internal.console.StackTraces
import munit.internal.junitinterface.Configurable
import munit.internal.junitinterface.Settings
import org.junit.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier

import java.lang.reflect.Modifier
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Success
import scala.util.Try
import scala.util.control.NonFatal

class MUnitRunner(val cls: Class[_ <: Suite], newInstance: () => Suite)
    extends Runner
    with Filterable
    with Configurable {

  def this(cls: Class[_ <: Suite]) =
    this(MUnitRunner.ensureEligibleConstructor(cls), () => cls.newInstance())

  val suite: Suite = newInstance()

  private implicit val ec: ExecutionContext = suite.munitExecutionContext

  @volatile private var settings: Settings = Settings.defaults()
  @volatile private var suiteAborted: Boolean = false

  private val descriptions: mutable.Map[Test, Description] =
    mutable.Map.empty[Test, Description]
  private val testNames: mutable.Set[String] =
    mutable.Set.empty[String]

  private lazy val munitTests: mutable.ArrayBuffer[Test] =
    mutable.ArrayBuffer[Test](suite.munitTests(): _*)
  private val suiteFixture = new Fixture[Unit](cls.getName()) {
    def apply(): Unit = ()
    override def beforeAll(): Unit =
      suite.beforeAll()
    override def beforeEach(context: BeforeEach): Unit =
      suite.beforeEach(context)
    override def afterEach(context: AfterEach): Unit =
      suite.afterEach(context)
    override def afterAll(): Unit =
      suite.afterAll()
  }
  private lazy val munitFixtures: List[AnyFixture[_]] =
    suiteFixture :: suite.munitFixtures.toList

  override def filter(filter: Filter): Unit = {
    val newTests = munitTests.filter { t =>
      filter.shouldRun(createTestDescription(t))
    }
    munitTests.clear()
    munitTests ++= newTests
  }
  override def configure(settings: Settings): Unit = {
    this.settings = settings
  }

  private def createTestDescription(test: Test): Description = {
    descriptions.getOrElseUpdate(
      test, {
        val escapedName = Printers.escapeNonVisible(test.name)
        val testName = munit.internal.Compat.LazyList
          .from(0)
          .map {
            case 0 => escapedName
            case n => s"${escapedName}-${n}"
          }
          .find(candidate => !testNames.contains(candidate))
          .head
        testNames += testName
        val desc = Description.createTestDescription(
          cls,
          testName,
          test.annotations: _*
        )
        desc
      }
    )
  }

  override def getDescription(): Description = {
    val description = Description.createSuiteDescription(cls)

    try {
      val suiteTests = StackTraces.dropOutside(munitTests)
      suiteTests.iterator
        .map(createTestDescription)
        .foreach(description.addChild)
    } catch {
      case ex: Throwable =>
        trimStackTrace(ex)
        // Print to stdout because we don't have access to a RunNotifier
        ex.printStackTrace()
    }

    description
  }

  override def run(notifier: RunNotifier): Unit = {
    Await.result(runAsync(notifier), Duration.Inf)
  }
  def runAsync(notifier: RunNotifier): Future[Unit] = {
    val description = getDescription()
    notifier.fireTestSuiteStarted(description)
    runAll(notifier)
      .transformCompat[Unit](result => {
        result.failed.foreach(ex =>
          fireFailedHiddenTest(notifier, "unexpected error running tests", ex)
        )
        notifier.fireTestSuiteFinished(description)
        util.Success(())
      })
  }

  private def runTests(
      notifier: RunNotifier
  ): Future[List[Try[Boolean]]] = {
    sequenceFutures(
      munitTests.iterator.map(t => runTest(notifier, t))
    )
  }

  // Similar `Future.sequence` but with cleaner stack traces for non-async code.
  private def sequenceFutures[A](
      futures: Iterator[Future[A]]
  ): Future[List[Try[A]]] = {
    def loop(
        it: Iterator[Future[A]],
        acc: mutable.ListBuffer[Try[A]]
    ): Future[List[Try[A]]] =
      if (!it.hasNext) {
        Future.successful(acc.toList)
      } else {
        val future = it.next()
        future.value match {
          case Some(value) =>
            acc += value
            // use tail-recursive call if possible to keep stack traces clean.
            loop(it, acc)
          case None =>
            future.flatMap(t => {
              acc += util.Success(t)
              loop(it, acc)
            })
        }
      }
    loop(futures, mutable.ListBuffer.empty)
  }

  private def munitTimeout(): Option[Duration] = {
    suite match {
      case funSuite: FunSuite => Some(funSuite.munitTimeout)
      case _                  => None
    }
  }

  private def runAll(notifier: RunNotifier): Future[Unit] = {
    if (PlatformCompat.isIgnoreSuite(cls) || munitTests.isEmpty) {
      val description = getDescription()
      notifier.fireTestIgnored(description)
      return Future.successful(())
    }
    for {
      beforeAll <- runBeforeAll(notifier)
      _ <- {
        if (beforeAll.isSuccess) {
          runTests(notifier)
        } else {
          Future.successful(Nil)
        }
      }
      _ <- runAfterAll(notifier, beforeAll)
    } yield ()
  }

  private[munit] class BeforeAllResult(
      val isSuccess: Boolean,
      val loadedFixtures: List[AnyFixture[_]],
      val errors: List[Throwable]
  )

  private def runBeforeAll(notifier: RunNotifier): Future[BeforeAllResult] = {
    val result: Future[List[Try[(AnyFixture[_], Boolean)]]] =
      sequenceFutures(
        munitFixtures.iterator.map(f =>
          runHiddenTest(
            notifier,
            s"beforeAll(${f.fixtureName})",
            () => f.beforeAll()
          ).map(isSuccess => f -> isSuccess)
        )
      )
    result.map { results =>
      val loadedFixtures = results.collect { case Success((fixture, true)) =>
        fixture
      }
      val errors = results.collect { case util.Failure(ex) => ex }
      val isSuccess = loadedFixtures.length == results.length
      new BeforeAllResult(isSuccess, loadedFixtures, errors)
    }
  }

  private def runAfterAll(
      notifier: RunNotifier,
      beforeAll: BeforeAllResult
  ): Future[Unit] = {
    sequenceFutures[Boolean](
      beforeAll.loadedFixtures.iterator.map(f =>
        runHiddenTest(
          notifier,
          s"afterAll(${f.fixtureName})",
          () => f.afterAll()
        )
      )
    ).map(_ => ())
  }

  private[munit] class BeforeEachResult(
      val error: Option[Throwable],
      val loadedFixtures: List[AnyFixture[_]]
  )

  private def runBeforeEach(
      test: Test
  ): Future[BeforeAllResult] = {
    val context = new BeforeEach(test)
    val fixtures = mutable.ListBuffer.empty[AnyFixture[_]]
    sequenceFutures(
      munitFixtures.iterator.map(f =>
        valueTransform(() => f.beforeEach(context)).map(_ => f)
      )
    ).map { results =>
      val loadedFixtures = results.collect { case Success(f) => f }
      val errors = results.collect { case util.Failure(ex) => ex }
      val isSuccess = loadedFixtures.length == results.length
      new BeforeAllResult(isSuccess, loadedFixtures, errors)
    }
  }

  private def runAfterEach(
      test: Test,
      fixtures: List[AnyFixture[_]]
  ): Future[Unit] = {
    val context = new AfterEach(test)
    sequenceFutures(
      fixtures.iterator.map(f => valueTransform(() => f.afterEach(context)))
    ).map(_ => ())
  }

  private def runTest(
      notifier: RunNotifier,
      test: Test
  ): Future[Boolean] = {
    val description = createTestDescription(test)

    if (suiteAborted) {
      notifier.fireTestAssumptionFailed(
        new Failure(
          description,
          new FailSuiteException("Suite has been aborted", test.location)
        )
      )
      return Future.successful(false)
    }

    if (test.tags(Ignore)) {
      notifier.fireTestIgnored(description)
      return Future.successful(false)
    }

    notifier.fireTestStarted(description)
    def handleNonFatalOrStackOverflow(ex: Throwable): Future[Unit] = {
      trimStackTrace(ex)
      val cause = Exceptions.rootCause(ex)
      val failure = new Failure(description, cause)
      cause match {
        case _: AssumptionViolatedException =>
          notifier.fireTestAssumptionFailed(failure)
        case _: FailSuiteException =>
          suiteAborted = true
          notifier.fireTestFailure(failure)
        case _ =>
          notifier.fireTestFailure(failure)
      }
      Future.successful(())
    }

    val onError: PartialFunction[Throwable, Future[Unit]] = {
      case ex: AssumptionViolatedException =>
        trimStackTrace(ex)
        notifier.fireTestAssumptionFailed(new Failure(description, ex))
        Future.successful(())
      case NonFatal(ex) =>
        handleNonFatalOrStackOverflow(ex)
      case ex: StackOverflowError =>
        handleNonFatalOrStackOverflow(ex)
    }
    val result: Future[Unit] =
      try runTestBody(notifier, description, test).recoverWith(onError)
      catch onError
    result.map { _ =>
      notifier.fireTestFinished(description)
      true
    }
  }

  private def futureFromAny(any: Any): Future[Any] = any match {
    case f: Future[_] => f
    case _            => Future.successful(any)
  }

  private def runTestBody(
      notifier: RunNotifier,
      description: Description,
      test: Test
  ): Future[Unit] = {
    val result: Future[Any] =
      runBeforeEach(test).flatMap[Any] { beforeEach =>
        beforeEach.errors match {
          case Nil =>
            StackTraces
              .dropOutside(test.body())
              .transformWithCompat(result =>
                runAfterEach(test, beforeEach.loadedFixtures)
                  .transformCompat(_ => result)
              )
          case error :: errors =>
            errors.foreach(err => error.addSuppressed(err))
            try runAfterEach(test, beforeEach.loadedFixtures)
            finally throw error
        }
      }
    result.map {
      case f: TestValues.FlakyFailure =>
        trimStackTrace(f)
        notifier.fireTestAssumptionFailed(new Failure(description, f))
      case TestValues.Ignore =>
        notifier.fireTestIgnored(description)
      case _ =>
        ()
    }
  }

  private[munit] class ForeachUnsafeResult(
      val sync: Try[Unit],
      val async: List[Future[Any]]
  )
  private def foreachUnsafe(
      thunks: Iterable[() => Any]
  ): ForeachUnsafeResult = {
    var errors = mutable.ListBuffer.empty[Throwable]
    val async = mutable.ListBuffer.empty[Future[Any]]
    thunks.foreach { thunk =>
      try {
        thunk() match {
          case f: Future[_] =>
            async += f
          case _ =>
        }
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
        new ForeachUnsafeResult(scala.util.Failure(head), Nil)
      case _ =>
        new ForeachUnsafeResult(scala.util.Success(()), Nil)
    }
  }

  private def runHiddenTest(
      notifier: RunNotifier,
      name: String,
      thunk: () => Any
  ): Future[Boolean] = {
    try {
      StackTraces.dropOutside {
        valueTransform(thunk)
          .transformCompat {
            case util.Success(value) =>
              util.Success(true)
            case util.Failure(exception) =>
              fireFailedHiddenTest(notifier, name, exception)
              util.Success(false)
          }
      }
    } catch {
      case ex: Throwable =>
        fireFailedHiddenTest(notifier, name, ex)
        Future.successful(false)
    }
  }

  private def fireFailedHiddenTest(
      notifier: RunNotifier,
      name: String,
      ex: Throwable
  ): Unit = {
    val test = new Test(name, () => ???, Set.empty, Location.empty)
    val description = createTestDescription(test)
    notifier.fireTestStarted(description)
    trimStackTrace(ex)
    notifier.fireTestFailure(
      new Failure(description, Exceptions.rootCause(ex))
    )
    notifier.fireTestFinished(description)
  }
  private def trimStackTrace(ex: Throwable): Unit = {
    if (settings.trimStackTraces) {
      StackTraces.trimStackTrace(ex)
    }
  }

  private def valueTransform(thunk: () => Any): Future[Any] = {
    suite match {
      case funSuite: FunSuite => funSuite.munitValueTransform(thunk())
      case _                  => futureFromAny(thunk())
    }
  }

}
object MUnitRunner {
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
