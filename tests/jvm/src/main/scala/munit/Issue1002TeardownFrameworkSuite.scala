package munit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Issue1002TeardownFrameworkSuite extends FunSuite {
  @volatile
  var cleanedUp: Boolean = false

  private val futureFixture = new FutureFixture[Unit](name = "futureFixture") {
    override def apply(): Unit = ()
    // must use global ec to trigger the issue
    override def afterAll(): Future[Unit] = Future {
      Thread.sleep(50)
      cleanedUp = true
      throw new Error("failure in afterAll")
    }
  }

  override def munitFixtures: Seq[AnyFixture[_]] = super.munitFixtures ++
    List(futureFixture)

  override def afterAll(): Unit = assert(cleanedUp)

  test("dummy") {}
}

object Issue1002TeardownFrameworkSuite
    extends FrameworkTest(
      classOf[Issue1002TeardownFrameworkSuite],
      """|==> success munit.Issue1002TeardownFrameworkSuite.dummy
         |==> failure munit.Issue1002TeardownFrameworkSuite.afterAll(futureFixture) - failure in afterAll
         |""".stripMargin,
    )
