package munit

import munit.internal.junitinterface.Configurable
import munit.internal.PlatformCompat
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.UndeclaredThrowableException

import munit.internal.FutureCompat._
import munit.internal.console.StackTraces
import org.junit.runner.notification.Failure

import scala.util.control.NonFatal
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.Filter
import org.junit.runner.Runner
import org.junit.AssumptionViolatedException

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.util.concurrent.ExecutionException

import munit.internal.junitinterface.Settings
import munit.internal.console.Printers

import scala.util.Try

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

  private val descriptions: mutable.Map[suite.Test, Description] =
    mutable.Map.empty[suite.Test, Description]
  private val testNames: mutable.Set[String] =
    mutable.Set.empty[String]

  private lazy val munitTests: mutable.ArrayBuffer[suite.Test] =
    mutable.ArrayBuffer[suite.Test](suite.munitTests(): _*)

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

  def createTestDescription(test: suite.Test): Description = {
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
    try {
      runAll(notifier)
    } catch {
      case ex: Throwable =>
        Future.successful(
          fireHiddenTest(notifier, "expected error running tests", ex)
        )
    } finally {
      notifier.fireTestSuiteFinished(description)
    }
  }

  private def runAsyncTestsSynchronously(
      notifier: RunNotifier
  ): Future[Unit] = {
    def loop(it: Iterator[suite.Test]): Future[Unit] =
      if (!it.hasNext) {
        Future.successful(())
      } else {
        val future = runTest(notifier, it.next())
        future.value match {
          case Some(_) =>
            // use tail-recursive call if possible to keep stack traces clean.
            loop(it)
          case None =>
            future.flatMap(_ => loop(it))
        }
      }
    loop(munitTests.iterator)
  }

  private def runAll(notifier: RunNotifier): Future[Unit] = {
    if (PlatformCompat.isIgnoreSuite(cls)) {
      val description = getDescription()
      notifier.fireTestIgnored(description)
      Future.successful(())
    } else {
      val result =
        for {
          isBeforeAllRun <- runBeforeAll(notifier)
          if isBeforeAllRun
          _ <- runAsyncTestsSynchronously(notifier)
        } yield isBeforeAllRun

      result.transformWithCompat {
        case util.Success(true) =>
          runAfterAll(notifier)

        case util.Success(false) =>
          Future.successful(())

        case util.Failure(ex) =>
          Future.failed(ex)
      }
    }
  }

  private def runBeforeAll(notifier: RunNotifier): Future[Boolean] = {
    val suiteBeforeAllF = Future.fromTry(
      runHiddenTest(notifier, "beforeAll", suite.beforeAll())
    )

    def beforeAllFixtureF =
      Future.sequence(
        suite.munitFixtures.map(f =>
          Future.fromTry(
            runHiddenTest(
              notifier,
              s"beforeAllFixture(${f.fixtureName})",
              f.beforeAll()
            )
          )
        )
      )

    def beforeAllAsyncFixtureF =
      Future
        .sequence(
          suite.munitAsyncFixtures.map(asyncFixture =>
            asyncFixture.beforeAll().flatMap { r =>
              Future.fromTry(
                runHiddenTest(
                  notifier,
                  s"beforeAllAsyncFixture(${asyncFixture.fixtureName})",
                  r
                )
              )
            }
          )
        )

    for {
      suiteBeforeAll <- suiteBeforeAllF
      beforeAllFixture <- beforeAllFixtureF
      beforeAllAsyncFixture <- beforeAllAsyncFixtureF
    } yield suiteBeforeAll && beforeAllFixture.forall(_ == true) &&
      beforeAllAsyncFixture.forall(_ == true)
  }

  private def runAfterAll(notifier: RunNotifier): Future[Unit] = {
    def suiteAfterAllF = Future.fromTry(
      runHiddenTest(notifier, "afterAll", suite.afterAll())
    )

    def afterAllFixtureF =
      Future.sequence(
        suite.munitFixtures.map(f =>
          Future.fromTry(
            runHiddenTest(
              notifier,
              s"afterAllFixture(${f.fixtureName})",
              f.afterAll()
            )
          )
        )
      )

    def afterAllAsyncFixtureF =
      Future.sequence(
        suite.munitAsyncFixtures.map(asyncFixture =>
          asyncFixture.afterAll().flatMap { r =>
            Future.fromTry(
              runHiddenTest(
                notifier,
                s"afterAllAsyncFixture(${asyncFixture.fixtureName})",
                r
              )
            )
          }
        )
      )

    for {
      _ <- afterAllFixtureF
      _ <- afterAllAsyncFixtureF
      _ <- suiteAfterAllF
    } yield ()
  }

  private[munit] class BeforeEachResult(
      val error: Option[Throwable],
      val loadedFixtures: List[suite.Fixture[_]],
      val loadedAsyncFixtures: List[suite.AsyncFixture[_]]
  )

  private def runBeforeEach(
      test: suite.Test
  ): Future[BeforeEachResult] = {
    val beforeEach = new GenericBeforeEach(test)

    def suiteBeforeEachF = foreachUnsafe[Unit](
      List(() -> (() => Future(suite.beforeEach(beforeEach))))
    )

    def beforeEachFixtureF =
      foreachUnsafe[suite.Fixture[_]](
        suite.munitFixtures.toList.map(fixture =>
          fixture -> (() => Future(fixture.beforeEach(beforeEach)))
        )
      )

    def beforeEachAsyncFixtureF =
      foreachUnsafe[suite.AsyncFixture[_]](
        suite.munitAsyncFixtures.toList.map(fixture =>
          fixture -> (() => fixture.beforeEach(beforeEach))
        )
      )

    for {
      suiteBeforeEach <- suiteBeforeEachF
      result <- suiteBeforeEach match {
        case Left((ex, _)) =>
          Future.successful(
            new BeforeEachResult(Some(ex), List.empty, List.empty)
          )

        case Right(_) =>
          beforeEachFixtureF.flatMap {
            case Left((ex, allocatedFixtures)) =>
              Future.successful(
                new BeforeEachResult(Some(ex), allocatedFixtures, List.empty)
              )

            case Right(allocatedFixtures) =>
              beforeEachAsyncFixtureF.map {
                case Left((ex, allocatedAsyncFixtures)) =>
                  new BeforeEachResult(
                    Some(ex),
                    allocatedFixtures,
                    allocatedAsyncFixtures
                  )

                case Right(allocatedAsyncFixtures) =>
                  new BeforeEachResult(
                    None,
                    allocatedFixtures,
                    allocatedAsyncFixtures
                  )
              }
          }
      }
    } yield result
  }

  private def runAfterEach(
      test: suite.Test,
      fixtures: List[suite.Fixture[_]],
      asyncFixtures: List[suite.AsyncFixture[_]]
  ): Future[Unit] = {
    val afterEach = new GenericAfterEach(test)

    def suiteAfterEachF = foreachUnsafe[Unit](
      List(() -> (() => Future(suite.afterEach(afterEach))))
    )

    def afterEachFixtureF =
      foreachUnsafe[suite.Fixture[_]](
        fixtures.map(fixture =>
          fixture -> (() => Future(fixture.afterEach(afterEach)))
        )
      )

    def afterEachAsyncFixtureF =
      foreachUnsafe[suite.AsyncFixture[_]](
        asyncFixtures.map(fixture =>
          fixture -> (() => fixture.afterEach(afterEach))
        )
      )

    afterEachFixtureF.flatMap {
      case Left((ex, _)) =>
        Future.failed(ex)

      case Right(_) =>
        afterEachAsyncFixtureF.flatMap {
          case Left((ex, _)) =>
            Future.failed(ex)

          case Right(_) =>
            suiteAfterEachF.flatMap {
              case Left((ex, _)) =>
                Future.failed(ex)

              case Right(_) =>
                Future.successful(())
            }
        }
    }
  }

  private def runTest(
      notifier: RunNotifier,
      test: suite.Test
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

    notifier.fireTestStarted(description)
    if (test.tags(Ignore)) {
      notifier.fireTestIgnored(description)
      return Future.successful(false)
    }
    val onError: PartialFunction[Throwable, Future[Unit]] = {
      case ex: AssumptionViolatedException =>
        trimStackTrace(ex)
        Future.successful(())
      case NonFatal(ex) =>
        trimStackTrace(ex)
        val cause = rootCause(ex)
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
    val result: Future[Unit] =
      try runTestBody(notifier, description, test).recoverWith(onError)
      catch onError
    result.map { _ =>
      notifier.fireTestFinished(description)
      true
    }
  }

  // NOTE(olafur): these exceptions appear when we await on futures. We unwrap
  // these exception in order to provide more helpful error messages.
  private def rootCause(x: Throwable): Throwable = x match {
    case _: InvocationTargetException | _: ExceptionInInitializerError |
        _: UndeclaredThrowableException | _: ExecutionException
        if x.getCause != null =>
      rootCause(x.getCause)
    case _ => x
  }

  private def runTestBody(
      notifier: RunNotifier,
      description: Description,
      test: suite.Test
  ): Future[Unit] = {
    val result = StackTraces.dropOutside {
      val beforeEachResult = runBeforeEach(test)
      beforeEachResult.flatMap { r =>
        r.error match {
          case None =>
            Future(test.body()).transformWithCompat {
              case util.Failure(ex) =>
                runAfterEach(test, r.loadedFixtures, r.loadedAsyncFixtures)
                  .transformWithCompat(_ => Future.failed(ex))

              case util.Success(testResult) =>
                runAfterEach(test, r.loadedFixtures, r.loadedAsyncFixtures)
                  .transformWithCompat {
                    case util.Failure(ex) =>
                      Future.failed(ex)

                    case util.Success(_) =>
                      Future.successful(testResult)
                  }
            }

          case Some(ex) =>
            runAfterEach(test, r.loadedFixtures, r.loadedAsyncFixtures)
              .transformWithCompat(_ => Future.failed(ex))
        }
      }
    }

    result
      .map {
        case TestValues.Ignore =>
          notifier.fireTestIgnored(description)
        case _ =>
          ()
      }
      .recover { case f: TestValues.FlakyFailure =>
        trimStackTrace(f)
        notifier.fireTestAssumptionFailed(new Failure(description, f))
      }
  }

  private def foreachUnsafe[A](
      thunks: Seq[(A, () => Future[Unit])]
  ): Future[Either[(Throwable, List[A]), List[A]]] =
    for {
      computed <- Future.sequence(
        thunks.map { case (fixture, computation) =>
          computation().map(_ => Right[Throwable, A](fixture)).recover {
            case ex if NonFatal(ex) =>
              Left[Throwable, A](ex)
          }
        }
      )

      aggregated = computed.foldLeft(List.empty[Throwable] -> List.empty[A]) {
        case (acc, Left(ex))       => (acc._1 :+ ex) -> acc._2
        case (acc, Right(fixture)) => acc._1 -> (acc._2 :+ fixture)
      }

      result <- aggregated match {
        case (head :: tail, allocatedFixtures) =>
          Future(
            tail.foreach {
              case e if e ne head =>
                head.addSuppressed(e)
              case _ => ()
            }
          ).map(_ => Left(head -> allocatedFixtures))

        case (Nil, allocatedFixtures) =>
          Future.successful(Right(allocatedFixtures))
      }

    } yield result

  private def runHiddenTest(
      notifier: RunNotifier,
      name: String,
      thunk: => Unit
  ): Try[Boolean] = Try {
    StackTraces.dropOutside(thunk)
    true
  }.recover {
    case ex: Throwable =>
      fireHiddenTest(notifier, name, ex)
      false
  }

  private def fireHiddenTest(
      notifier: RunNotifier,
      name: String,
      ex: Throwable
  ): Unit = {
    val test = new suite.Test(name, () => ???, Set.empty, Location.empty)
    val description = createTestDescription(test)
    notifier.fireTestStarted(description)
    trimStackTrace(ex)
    notifier.fireTestFailure(new Failure(description, ex))
    notifier.fireTestFinished(description)
  }
  private def trimStackTrace(ex: Throwable): Unit = {
    if (settings.trimStackTraces) {
      StackTraces.trimStackTrace(ex)
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
