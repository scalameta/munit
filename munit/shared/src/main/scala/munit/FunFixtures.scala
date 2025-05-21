package munit

import scala.concurrent.Future
import scala.util.Failure

trait FunFixtures {
  self: BaseFunSuite =>

  class FunFixture[T] private (
      val setup: TestOptions => Future[T],
      val teardown: T => Future[Unit],
  )(implicit dummy: DummyImplicit) {
    fixture =>

    def test(name: String)(body: T => Any)(implicit loc: Location): Unit =
      fixture.test(TestOptions(name))(body)
    def test(
        options: TestOptions
    )(body: T => Any)(implicit loc: Location): Unit = self.test(options) {
      implicit val ec = munitExecutionContext
      // the setup, test and teardown need to keep the happens-before execution order
      setup(options).flatMap(argument =>
        munitValueTransform(body(argument)).transformWith(testValue =>
          teardown(argument).transform {
            case teardownFailure: Failure[_] => testValue match {
                case testFailure: Failure[_] =>
                  testFailure.exception.addSuppressed(teardownFailure.exception)
                  testFailure
                case _ => teardownFailure
              }
            case _ => testValue
          }
        )
      )
    }(loc)
  }

  object FunFixture {
    def apply[T](
        setup: TestOptions => T,
        teardown: T => Unit,
    ): FunFixture[T] = {
      implicit val ec = munitExecutionContext
      async[T](
        options => Future(setup(options)),
        argument => Future(teardown(argument)),
      )
    }
    def async[T](setup: TestOptions => Future[T], teardown: T => Future[Unit]) =
      new FunFixture(setup, teardown)

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
        teardown = { case (argumentA, argumentB) =>
          implicit val ec = munitExecutionContext
          Future.sequence(List(a.teardown(argumentA), b.teardown(argumentB)))
            .map(_ => ())
        },
      )
    def map3[A, B, C](
        a: FunFixture[A],
        b: FunFixture[B],
        c: FunFixture[C],
    ): FunFixture[(A, B, C)] = FunFixture.async[(A, B, C)](
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
      teardown = { case (argumentA, argumentB, argumentC) =>
        implicit val ec = munitExecutionContext
        Future.sequence(List(
          a.teardown(argumentA),
          b.teardown(argumentB),
          c.teardown(argumentC),
        )).map(_ => ())
      },
    )
  }

}
