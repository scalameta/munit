package munit

import scala.concurrent.duration.Duration
import munit.internal.PlatformCompat
import scala.concurrent.Promise

class Issue285FrameworkSuite extends FunSuite {
  def println(msg: String): Unit = TestingConsole.out.println(msg)
  val hello: Fixture[Unit] = new Fixture[Unit]("hello") {
    def apply(): Unit = ()
    override def beforeAll(): Unit = {
      println("beforeAll")
    }
    override def beforeEach(context: BeforeEach): Unit = {
      println("beforeEach - " + context.test.name)
    }
    override def afterEach(context: AfterEach): Unit = {
      println("afterEach - " + context.test.name)
    }
    override def afterAll(): Unit = {
      println("afterAll")
    }
  }
  override def munitFixtures: List[Fixture[Unit]] = List(hello)
  override def munitTimeout: Duration = Duration(5, "ms")
  test("issue-285-ok") {
    ()
  }
  test("issue-285-fail") {
    val promise = Promise[Unit]()
    PlatformCompat.setTimeout(40) {
      promise.trySuccess(())
    }
    promise.future
  }
  test("issue-285-ok") {
    ()
  }
}

object Issue285FrameworkSuite
    extends FrameworkTest(
      classOf[Issue285FrameworkSuite],
      """|munit.Issue285FrameworkSuite:
         |beforeAll
         |beforeEach - issue-285-ok
         |afterEach - issue-285-ok
         |  + issue-285-ok <elapsed time>
         |beforeEach - issue-285-fail
         |afterEach - issue-285-fail
         |==> X munit.Issue285FrameworkSuite.issue-285-fail <elapsed time>java.util.concurrent.TimeoutException: test timed out after 5 milliseconds
         |beforeEach - issue-285-ok
         |afterEach - issue-285-ok
         |  + issue-285-ok-1 <elapsed time>
         |afterAll
         |""".stripMargin,
      format = StdoutFormat
    )
