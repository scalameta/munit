package munit

import scala.concurrent.Future

class AsyncFixtureTeardownSuite extends FunSuite {
  @volatile var cleanedUp: Boolean = _

  val cleanupInTeardown = FunFixture.async[Unit](
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

object AsyncFixtureTeardownSuite
    extends FrameworkTest(
      classOf[AsyncFixtureTeardownSuite],
      """|==> failure munit.AsyncFixtureTeardownSuite.calls teardown when test throws - failure in test
         |==> failure munit.AsyncFixtureTeardownSuite.calls teardown when test returns failed Future - failure in test
         |""".stripMargin
    )
