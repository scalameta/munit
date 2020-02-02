package munit

import munit.internal.console.StackTraces
import munit.internal.FutureCompat._

import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.Try
import scala.concurrent.duration.Duration
import munit.internal.PlatformCompat

abstract class FunSuite
    extends Suite
    with Assertions
    with TestOptionsConversions { self =>

  final type TestValue = Future[Any]

  def isCI: Boolean = "true" == System.getenv("CI")
  def munitIgnore: Boolean = false
  def munitFlakyOK: Boolean = "true" == System.getenv("MUNIT_FLAKY_OK")

  private val defaultTimeout = Duration(30, "s")
  def munitTimeout: Duration = defaultTimeout
  val munitTestsBuffer: mutable.ArrayBuffer[Test] =
    mutable.ArrayBuffer.empty[Test]
  def munitTests(): Seq[Test] = {
    if (munitIgnore) {
      Nil
    } else {
      val onlyTests = munitTestsBuffer.filter(_.tags(Only))
      if (onlyTests.nonEmpty) {
        if (isCI) {
          onlyTests.toSeq.map(t =>
            if (t.tags(Only)) {
              t.withBody[TestValue](() =>
                fail("'Only' tag is not allowed when `isCI=true`")(t.location)
              )
            } else {
              t
            }
          )
        } else {
          onlyTests.toSeq
        }
      } else {
        munitTestsBuffer.toSeq
      }
    }
  }

  def munitTestValue(testValue: => Any): Future[Any] = {
    // Takes an arbitrarily nested future `Future[Future[Future[...]]]` and
    // returns a `Future[T]` where `T` is not a `Future`.
    def flattenFuture(future: Future[_]): Future[_] = {
      val nested = future.map {
        case f: Future[_] => flattenFuture(f)
        case x            => Future.successful(x)
      }(munitExecutionContext)
      nested.flattenCompat(munitExecutionContext)
    }
    val wrappedFuture = Future.fromTry(Try(StackTraces.dropOutside(testValue)))
    val flatFuture = flattenFuture(wrappedFuture)
    val awaitedFuture = PlatformCompat.waitAtMost(flatFuture, munitTimeout)
    awaitedFuture
  }

  def munitNewTest(test: Test): Test =
    test

  def test(options: TestOptions)(
      body: => Any
  )(implicit loc: Location): Unit = {
    munitTestsBuffer += munitNewTest(
      new Test(
        options.name, { () =>
          munitRunTest(options, () => {
            try {
              munitTestValue(body)
            } catch {
              case NonFatal(e) =>
                Future.failed(e)
            }
          })
        },
        options.tags.toSet,
        loc
      )
    )
  }

  def munitRunTest(
      options: TestOptions,
      body: () => Future[Any]
  ): Future[Any] = {
    if (options.tags(Fail)) {
      munitExpectFailure(options, body)
    } else if (options.tags(Flaky)) {
      munitFlaky(options, body)
    } else if (options.tags(Ignore)) {
      Future.successful(Ignore)
    } else {
      body()
    }
  }

  def munitFlaky(
      options: TestOptions,
      body: () => Future[Any]
  ): Future[Any] = {
    body().transformCompat {
      case Success(value) => Success(value)
      case Failure(exception) =>
        if (munitFlakyOK) {
          Success(new TestValues.FlakyFailure(exception))
        } else {
          throw exception
        }
    }(munitExecutionContext)
  }

  def munitExpectFailure(
      options: TestOptions,
      body: () => Future[Any]
  ): Future[Any] = {
    body().transformCompat {
      case Success(value) =>
        Failure(
          throw new FailException(
            munitLines.formatLine(
              options.location,
              "expected failure but test passed"
            ),
            options.location
          )
        )
      case Failure(exception) =>
        Success(())
    }(munitExecutionContext)
  }

  class FunFixture[T](
      val setup: TestOptions => T,
      val teardown: T => Unit
  ) {
    def test(options: TestOptions)(
        body: T => Any
    )(implicit loc: Location): Unit = {
      self.test(options) {
        val argument = setup(options)
        try body(argument)
        finally teardown(argument)
      }(loc)
    }
  }
  object FunFixture {
    def map2[A, B](a: FunFixture[A], b: FunFixture[B]): FunFixture[(A, B)] =
      new FunFixture[(A, B)](
        setup = { options =>
          (a.setup(options), b.setup(options))
        },
        teardown = {
          case (argumentA, argumentB) =>
            try a.teardown(argumentA)
            finally b.teardown(argumentB)
        }
      )
    def map3[A, B, C](
        a: FunFixture[A],
        b: FunFixture[B],
        c: FunFixture[C]
    ): FunFixture[(A, B, C)] =
      new FunFixture[(A, B, C)](
        setup = { options =>
          (a.setup(options), b.setup(options), c.setup(options))
        },
        teardown = {
          case (argumentA, argumentB, argumentC) =>
            try a.teardown(argumentA)
            finally {
              try b.teardown(argumentB)
              finally c.teardown(argumentC)
            }
        }
      )
  }

}
