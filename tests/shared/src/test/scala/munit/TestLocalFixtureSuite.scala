package munit

class TestLocalFixtureSuite extends FunSuite {
  val name: Fixture[String] = new Fixture[String]("name") {
    private var name = ""
    def apply() = name
    override def beforeEach(context: BeforeEach): Unit = {
      name = context.test.name + "-before"
    }
    override def afterEach(context: AfterEach): Unit = {
      name = context.test.name + "-after"
    }
  }
  val name2 = name

  override def afterEach(context: AfterEach): Unit = {
    assertEquals(name(), context.test.name + "-after")
  }

  override def munitFixtures: Seq[Fixture[_]] = List(name, name2)

  test("basic") {
    assertEquals(name(), "basic-before")
  }

  test("two") {
    assertEquals(name(), "two-before")
    assertEquals(name(), name2())
  }
}
