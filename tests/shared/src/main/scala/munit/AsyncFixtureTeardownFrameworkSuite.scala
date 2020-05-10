package munit

import scala.concurrent.Future

class AsyncFixtureTeardownFrameworkSuite extends FunSuite {
  @volatile var cleanedUp: Boolean = _

  val cleanupInTeardown: FunFixture[Unit] = FunFixture.async[Unit](
    _ => { cleanedUp = false; Future.successful(()) },
    _ => { cleanedUp = true; Future.successful(()) }
  )

  override def afterAll(): Unit = {
    assert(cleanedUp)
  }

  cleanupInTeardown.test("calls teardown when test throws") { _ =>
    throw new Error("failure in test")
  }

  cleanupInTeardown.test("calls teardown when test returns failed Future") {
    _ => Future.failed(new Error("failure in test"))
  }
}

object AsyncFixtureTeardownFrameworkSuite
    extends FrameworkTest(
      classOf[AsyncFixtureTeardownFrameworkSuite],
      """|==> failure munit.AsyncFixtureTeardownFrameworkSuite.calls teardown when test throws - failure in test
         |==> failure munit.AsyncFixtureTeardownFrameworkSuite.calls teardown when test returns failed Future - failure in test
         |""".stripMargin
    )
