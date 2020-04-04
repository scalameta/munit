package munit

import munit.internal.FutureCompat._

import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure

trait FunFixtures { self: FunSuite =>

  class FunFixture[T] private (
      val setupAndTeardown: (TestOptions => Future[T], T => Future[Unit])
  ) {
    def setup = setupAndTeardown._1
    def teardown = setupAndTeardown._2

    @deprecated("Use `FunFixture(...)` without `new` instead", "0.7.2")
    def this(setup: TestOptions => T, teardown: T => Unit) =
      this(
        (
          options => Future(setup(options))(munitExecutionContext),
          argument => Future(teardown(argument))(munitExecutionContext)
        )
      )

    def test(options: TestOptions)(
        body: T => Any
    )(implicit loc: Location): Unit = {
      self.test(options) {
        implicit val ec = munitExecutionContext
        setup(options).flatMap { argument =>
          munitValueTransform(body(argument))
            .transformWithCompat(o =>
              teardown(argument).transformCompat {
                case Success(_)     => o
                case f @ Failure(_) => f
              }
            )
        }
      }(loc)
    }
  }

  object FunFixture {
    def apply[T](setup: TestOptions => T, teardown: T => Unit) = {
      implicit val ec = munitExecutionContext
      async[T](
        options => Future { setup(options) },
        argument => Future { teardown(argument) }
      )
    }
    def async[T](setup: TestOptions => Future[T], teardown: T => Future[Unit]) =
      new FunFixture((setup, teardown))

    def map2[A, B](a: FunFixture[A], b: FunFixture[B]): FunFixture[(A, B)] =
      FunFixture.async[(A, B)](
        setup = { options =>
          implicit val ec = munitExecutionContext
          val setupA = a.setup(options)
          val setupB = b.setup(options)
          for {
            argumentA <- setupA
            argumentB <- setupB
          } yield (argumentA, argumentB)
        },
        teardown = {
          case (argumentA, argumentB) =>
            implicit val ec = munitExecutionContext
            Future
              .sequence(List(a.teardown(argumentA), b.teardown(argumentB)))
              .map(_ => ())
        }
      )
    def map3[A, B, C](
        a: FunFixture[A],
        b: FunFixture[B],
        c: FunFixture[C]
    ): FunFixture[(A, B, C)] =
      FunFixture.async[(A, B, C)](
        setup = { options =>
          implicit val ec = munitExecutionContext
          val setupA = a.setup(options)
          val setupB = b.setup(options)
          val setupC = c.setup(options)
          for {
            argumentA <- setupA
            argumentB <- setupB
            argumentC <- setupC
          } yield (argumentA, argumentB, argumentC)
        },
        teardown = {
          case (argumentA, argumentB, argumentC) =>
            implicit val ec = munitExecutionContext
            Future
              .sequence(
                List(
                  a.teardown(argumentA),
                  b.teardown(argumentB),
                  c.teardown(argumentC)
                )
              )
              .map(_ => ())
        }
      )
  }

}
