package munit

import scala.concurrent.Future

class AsyncFixtureFrameworkSuite extends FunSuite {
  val failingSetup = FunFixture.async[Unit](
    _ => Future.failed(new Error("failure in setup")),
    _ => Future.successful(())
  )

  val failingTeardown = FunFixture.async[Unit](
    _ => Future.successful(()),
    _ => Future.failed(new Error("failure in teardown"))
  )

  val unitFixture = FunFixture.async[Unit](
    _ => Future.successful(()),
    _ => Future.successful(())
  )

  failingSetup.test("fail when setup fails") { _ =>
    fail("failing setup did not fail the test")
  }

  failingTeardown.test("fail when teardown fails") { _ => () }

  FunFixture
    .map2(unitFixture, failingSetup)
    .test("fail when mapped setup fails") { _ =>
      fail("failing setup did not fail the test")
    }

  FunFixture
    .map3(unitFixture, unitFixture, failingSetup)
    .test("fail when even more nested mapped setup fails") { _ =>
      fail("failing setup did not fail the test")
    }

  FunFixture
    .map2(unitFixture, failingTeardown)
    .test("fail when mapped teardown fails") { _ => () }

  FunFixture
    .map3(unitFixture, unitFixture, failingTeardown)
    .test("fail when even more nested mapped teardown fails") { _ => () }
}

object AsyncFixtureFrameworkSuite
    extends FrameworkTest(
      classOf[AsyncFixtureFrameworkSuite],
      """|==> failure munit.AsyncFixtureFrameworkSuite.fail when setup fails - failure in setup
         |==> failure munit.AsyncFixtureFrameworkSuite.fail when teardown fails - failure in teardown
         |==> failure munit.AsyncFixtureFrameworkSuite.fail when mapped setup fails - failure in setup
         |==> failure munit.AsyncFixtureFrameworkSuite.fail when even more nested mapped setup fails - failure in setup
         |==> failure munit.AsyncFixtureFrameworkSuite.fail when mapped teardown fails - failure in teardown
         |==> failure munit.AsyncFixtureFrameworkSuite.fail when even more nested mapped teardown fails - failure in teardown
         |""".stripMargin
    )
