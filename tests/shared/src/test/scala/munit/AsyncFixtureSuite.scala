package munit

import scala.concurrent.Future
import scala.concurrent.Promise

class AsyncFixtureSetupTestTeardownOrderSuite extends FunSuite {
  val latch = Promise[Unit]
  var completedFromTest = Option.empty[Boolean]
  var completedFromTeardown = Option.empty[Boolean]

  val latchOnTeardown = FunFixture.async[String](
    setup = { test => Future.successful(test.name) },
    teardown = { name =>
      implicit val ec = munitExecutionContext
      Future {
        completedFromTeardown = Some(latch.trySuccess(()));
      }
    }
  )

  override def afterAll(): Unit = {
    assertEquals(completedFromTest, Some(true))
    assertEquals(completedFromTeardown, Some(false))
  }

  latchOnTeardown.test("test is ran before the teardown") { _ =>
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      // Ideally we would simulate some work here, for example by using Thread.sleep(50),
      // which would increase the certainty that this test passes by design and not by lucky
      // scheduling. However Thread.sleep is not scala-native compatible.
      //Thread.sleep(50)
      completedFromTest = Some(latch.trySuccess(()))
    }
  }
}

class AsyncFixtureTeardownSuite extends FunSuite {
  @volatile var cleanedUp: Boolean = _

  val cleanupInTeardown = FunFixture.async[Unit](
    _ => { cleanedUp = false; Future.successful(()) },
    _ => { cleanedUp = true; Future.successful(()) }
  )

  override def afterAll(): Unit = {
    assert(cleanedUp)
  }

  cleanupInTeardown.test("calls teardown when test throws".fail) { _ =>
    throw new Error("failure in test")
  }

  cleanupInTeardown.test("calls teardown when test returns failed Future".fail) {
    _ => Future.failed(new Error("failure in test"))
  }
}

class AsyncFixtureSuite extends FunSuite {
  def asyncFixture[T](setup: () => Future[T], teardown: T => Future[Unit]) =
    FunFixture.async[T](
      _ => setup(),
      teardown
    )

  val failingSetup = asyncFixture[Unit](
    () => Future.failed(new Error("failure in setup")),
    _ => Future.successful(())
  )

  val failingTeardown = asyncFixture[Unit](
    () => Future.successful(()),
    _ => Future.failed(new Error("failure in teardown"))
  )

  val unitFixture = asyncFixture[Unit](
    () => Future.successful(()),
    _ => Future.successful(())
  )

  failingSetup
    .test("fail when setup fails".fail) { _ =>
      fail("failing setup did not fail the test")
    }

  failingTeardown.test("fail when teardown fails".fail) { _ =>
    fail("failing teardown did not fail the test")
  }

  FunFixture
    .map2(unitFixture, failingSetup)
    .test("fail when mapped setup fails".fail) { _ =>
      fail("failing setup did not fail the test")
    }

  FunFixture
    .map3(unitFixture, unitFixture, failingSetup)
    .test("fail when even more nested mapped setup fails".fail) { _ =>
      fail("failing setup did not fail the test")
    }

  FunFixture
    .map2(unitFixture, failingTeardown)
    .test("fail when mapped teardown fails".fail) { _ =>
      fail("failing teardown did not fail the test")
    }

  FunFixture
    .map3(unitFixture, unitFixture, failingTeardown)
    .test("fail when even more nested mapped teardown fails".fail) { _ =>
      fail("failing teardown did not fail the test")
    }
}
