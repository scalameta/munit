package munit

import munit.internal.PlatformCompat

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

class EventuallySuite extends FunSuite {
  implicit val ec: ExecutionContext = PlatformCompat.executionContext
  override def munitExecutionContext: ExecutionContext = ec

  private implicit val options: EventuallyOptions =
    EventuallyOptions(7, 10.millis)

  case object DummyException
      extends Exception("dummy") with control.NoStackTrace

  test("non-positive arguments") {
    intercept[IllegalArgumentException](EventuallyOptions(0, 1.milli))
    intercept[IllegalArgumentException](EventuallyOptions(-1, 1.milli))
    intercept[IllegalArgumentException](EventuallyOptions(1, Duration.Zero))
    intercept[IllegalArgumentException](EventuallyOptions(1, -1.milli))
    intercept[IllegalArgumentException](options.withMaxRetries(0))
    intercept[IllegalArgumentException](options.withSleep(Duration.Zero))
    assertEquals(EventuallyOptions.disabled.maxRetries, 0)
  }

  test("disabled") {
    var counter = 0

    intercept[FailException](EventuallyOptions.disabled.eventually {
      counter += 1
      assert(false)
    })
    assertEquals(counter, 1)
  }

  test("options implicitly in scope") {
    var counter = 0

    def v = {
      counter += 1
      false
    }

    locally {
      implicit val options: EventuallyOptions = this.options.withMaxRetries(3)
      intercept[FailException](eventually(assert(v)))
      assertEquals(counter, 4)
      counter = 0
      intercept[FailException](eventually(assert(v)))
      assertEquals(counter, 4)
    }

    counter = 0
    intercept[FailException](eventually(assert(v)))
    assertEquals(counter, options.maxRetries + 1)
  }

  test("options implicitly in scope for a future") {
    var counter = 0

    def fut = Future.successful {
      counter += 1
      counter > 3
    }

    implicit val options: EventuallyOptions = this.options.withMaxRetries(3)
    eventually(fut.map(assert(_))).map(_ => assertEquals(counter, 4))
  }

  test("exhaust") {
    var counter = 0

    def v = {
      counter += 1
      false
    }

    intercept[FailException](eventually(assert(v)))
    assertEquals(counter, options.maxRetries + 1)
  }

  test("retry a pure value") {
    var counter = 0

    def v = {
      val ok = counter > options.maxRetries
      if (!ok) counter += 1
      ok
    }

    assert(!v)
    eventually(assert(v))
    assert(v)
  }

  test("retry a pure value when it throws") {
    var counter = 0

    def v = {
      if (counter <= options.maxRetries) {
        counter += 1
        throw DummyException
      }
      true
    }

    intercept[DummyException.type](v)
    assertEquals(counter, 1)
    intercept[DummyException.type](eventually(assert(v)))
    assertEquals(counter, 2)

    locally {
      val options = this.options.withFilters(_ == DummyException)
      options.eventually(assert(v))
      assertEquals(counter, options.maxRetries + 1)
    }
  }

  test("retry a future") {
    var counter = 0

    def fut = Future.successful {
      counter += 1
      counter > options.maxRetries
    }

    eventually(fut.map(assert(_))).transform { res =>
      assertEquals(counter, options.maxRetries + 1)
      assert(res.isSuccess)
      res
    }
  }

  test("retry a future when it throws on evaluation") {
    var counter = 0

    def fut: Future[Boolean] =
      if (counter <= options.maxRetries) {
        counter += 1
        Future.failed(DummyException)
      } else Future.successful(true)
    def run(options: EventuallyOptions) = options.eventually(fut.map(assert(_)))

    run(this.options).transformWith { res =>
      assertEquals(res, Failure(DummyException))
      assertEquals(counter, 1)

      val options = this.options.withFilters(_ == DummyException)
      run(options).map(_ => assertEquals(counter, options.maxRetries + 1))
    }
  }
}
