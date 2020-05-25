package munit

import cats.effect.{IO, Resource}

import scala.concurrent.{Future, Promise}

class IOFixtureSuite extends CatsEffectSuite with CatsEffectFunFixtures {
  val latch: Promise[Unit] = Promise[Unit]
  var completedFromTest: Option[Boolean] = None
  var completedFromTeardown: Option[Boolean] = None

  var completedFromResourceAcquire: Option[Boolean] = None
  var completedFromResourceRelease: Option[Boolean] = None

  val latchOnTeardown: FunFixture[String] =
    CatsEffectFixture.fromResource[String](
      resource = Resource.make[IO, String](
        IO {
          completedFromResourceAcquire = Some(true)
          "test"
        }
      )(_ =>
        IO {
          completedFromResourceRelease = Some(true)
        }
      ),
      setup = { (_: TestOptions, _: String) =>
        IO {
          completedFromResourceAcquire = Some(false)
        }
      },
      teardown = { _: String =>
        IO {
          completedFromResourceRelease = Some(false)
          completedFromTeardown = Some(latch.trySuccess(()));
        }
      }
    )

  override def afterAll(): Unit = {
    // resource was created before setup
    assertEquals(completedFromResourceAcquire, Some(false))
    // resource was released after teardown
    assertEquals(completedFromResourceRelease, Some(true))
    // promise was completed first by the test
    assertEquals(completedFromTest, Some(true))
    // and then there was a completion attempt by the teardown
    assertEquals(completedFromTeardown, Some(false))
  }

  latchOnTeardown.test("teardown runs only after test completes") { _ =>
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      // Simulate some work here, which increases the certainty that this test
      // will fail by design and not by lucky scheduling if the happens-before
      // relationship between the test and teardown is removed.
      Thread.sleep(50)
      completedFromTest = Some(latch.trySuccess(()))
    }
  }
}
