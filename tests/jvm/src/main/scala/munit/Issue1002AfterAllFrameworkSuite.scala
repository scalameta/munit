package munit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Issue1002AfterAllFrameworkSuite extends FunSuite {

  private val futureFixture = new FutureFixture[Unit](name = "futureFixture") {
    override def apply(): Unit = ()
    // must use global ec to trigger the issue
    override def afterAll(): Future[Unit] = Future {
      Thread.sleep(50)
    }
  }

  override def munitFixtures = super.munitFixtures ++ List(futureFixture)

  // afterAll will not execute if fixture is still running

  override def afterAll(): Unit = assert(false)

  test("dummy")()
}

object Issue1002AfterAllFrameworkSuite
    extends FrameworkTest(
      classOf[Issue1002AfterAllFrameworkSuite],
      """|==> success munit.Issue1002AfterAllFrameworkSuite.dummy
         |==> failure munit.Issue1002AfterAllFrameworkSuite.afterAll(munit.Issue1002AfterAllFrameworkSuite) - tests/jvm/src/main/scala/munit/Issue1002AfterAllFrameworkSuite.scala:20 assertion failed
         |19:
         |20:  override def afterAll(): Unit = assert(false)
         |21:
         |""".stripMargin,
    )
