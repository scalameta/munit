package munit

import scala.concurrent.Future

class AsyncFunFixtureFrameworkSuite extends FunSuite {
  val failingSetup: FunFixture[Unit] = FunFixture.async[Unit](
    _ => Future.failed(new Error("failure in setup")),
    _ => Future.successful(()),
  )

  val failingTeardown: FunFixture[Unit] = FunFixture.async[Unit](
    _ => Future.successful(()),
    _ => Future.failed(new Error("failure in teardown")),
  )

  val unitFixture: FunFixture[Unit] = FunFixture
    .async[Unit](_ => Future.successful(()), _ => Future.successful(()))

  failingSetup.test("fail when setup fails")(_ =>
    fail("failing setup did not fail the test")
  )

  failingTeardown.test("fail when teardown fails")(_ => ())

  failingTeardown
    .test("fail when test and teardown fail")(_ => fail("failure in test"))

  FunFixture.map2(unitFixture, failingSetup).test("fail when mapped setup fails")(
    _ => fail("failing setup did not fail the test")
  )

  FunFixture.map3(unitFixture, unitFixture, failingSetup)
    .test("fail when even more nested mapped setup fails")(_ =>
      fail("failing setup did not fail the test")
    )

  FunFixture.map2(unitFixture, failingTeardown)
    .test("fail when mapped teardown fails")(_ => ())

  FunFixture.map3(unitFixture, unitFixture, failingTeardown)
    .test("fail when even more nested mapped teardown fails")(_ => ())
}

object AsyncFunFixtureFrameworkSuite
    extends FrameworkTest(
      classOf[AsyncFunFixtureFrameworkSuite],
      """|==> failure munit.AsyncFunFixtureFrameworkSuite.fail when setup fails - failure in setup
         |==> failure munit.AsyncFunFixtureFrameworkSuite.fail when teardown fails - failure in teardown
         |==> failure munit.AsyncFunFixtureFrameworkSuite.fail when test and teardown fail - tests/shared/src/main/scala/munit/AsyncFunFixtureFrameworkSuite.scala:26 failure in test
         |25:  failingTeardown
         |26:    .test("fail when test and teardown fail")(_ => fail("failure in test"))
         |27:
         |==> failure munit.AsyncFunFixtureFrameworkSuite.fail when mapped setup fails - failure in setup
         |==> failure munit.AsyncFunFixtureFrameworkSuite.fail when even more nested mapped setup fails - failure in setup
         |==> failure munit.AsyncFunFixtureFrameworkSuite.fail when mapped teardown fails - failure in teardown
         |==> failure munit.AsyncFunFixtureFrameworkSuite.fail when even more nested mapped teardown fails - failure in teardown
         |""".stripMargin,
    )
