package munit

import cats.effect.{IO, Resource}
import cats.syntax.flatMap._

import scala.concurrent.Promise

trait CatsEffectFunFixtures extends FunFixtures { self: CatsEffectSuite =>

  object CatsEffectFixture {

    def fromResource[T](
        resource: Resource[IO, T]
    ): FunFixture[T] = fromResource(
      resource,
      (_, _) => IO.pure(()),
      _ => IO.pure(())
    )

    def fromResource[T](
        resource: Resource[IO, T],
        setup: (TestOptions, T) => IO[Unit],
        teardown: T => IO[Unit]
    ): FunFixture[T] = {
      val promise = Promise[IO[Unit]]()

      FunFixture.async(
        setup = { testOptions =>
          implicit val ec = munitExecutionContext

          val resourceEffect = resource.allocated
          val setupEffect =
            resourceEffect
              .map {
                case (t, release) =>
                  promise.success(release)
                  t
              }
              .flatTap(t => setup(testOptions, t))

          setupEffect.unsafeToFuture()
        },
        teardown = { argument: T =>
          implicit val cs = munitContextShift

          teardown(argument)
            .flatMap(_ => IO.fromFuture(IO(promise.future)).flatten)
            .unsafeToFuture()
        }
      )
    }

  }

}
