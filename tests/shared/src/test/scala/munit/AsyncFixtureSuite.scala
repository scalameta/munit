package munit

import scala.concurrent.Future

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
