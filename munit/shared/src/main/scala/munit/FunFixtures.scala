package munit

import munit.internal.FutureCompat._

import scala.concurrent.Future

trait FunFixtures { self: FunSuite =>

  class FunFixture[T] private (
      val setup: TestOptions => Future[T],
      val teardown: T => Future[Unit]
  ) {
    def test(options: TestOptions)(
        body: T => Any
    )(implicit loc: Location): Unit = {
      self.test(options) {
        implicit val ec = munitExecutionContext
        setup(options).flatMap { argument =>
          munitValueTransform(body(argument))
            .transformWithCompat(o =>
              teardown(argument).transformCompat(_ => o)
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
      new FunFixture(setup, teardown)

    def map2[A, B](a: FunFixture[A], b: FunFixture[B]): FunFixture[(A, B)] =
      FunFixture.async[(A, B)](
        setup = { options =>
          implicit val ec = munitExecutionContext
          for {
            argumentA <- a.setup(options)
            argumentB <- b.setup(options)
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
      new FunFixture[(A, B, C)](
        setup = { options =>
          implicit val ec = munitExecutionContext
          for {
            argumentA <- a.setup(options)
            argumentB <- b.setup(options)
            argumentC <- c.setup(options)
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
