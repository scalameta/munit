package munit

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TestLocalAsyncFixtureJVMSuite extends FunSuite {
  val name: AsyncFixture[String] = new AsyncFixture[String]("name") {
    private var name = ""
    def apply() = name
    override def beforeEach(context: BeforeEach): Future[Unit] = Future {
      name = context.test.name + "-before"
    }
    override def afterEach(context: AfterEach): Future[Unit] = Future {
      name = context.test.name + "-after"
    }
  }
  val name2 = name

  override def afterEach(context: GenericAfterEach[TestValue]): Unit = {
    assertEquals(name(), context.test.name + "-after")
  }

  override def munitAsyncFixtures: Seq[AsyncFixture[_]] = List(name, name2)

  test("basic") {
    assertEquals(name(), "basic-before")
  }

  test("two") {
    assertEquals(name(), "two-before")
    assertEquals(name(), name2())
  }
}
