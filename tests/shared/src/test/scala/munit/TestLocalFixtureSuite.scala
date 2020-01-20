package munit

class TestLocalFixtureSuite extends FunSuite {
  val name: Fixture[String] = new Fixture[String]("name") {
    private var name = ""
    def apply() = name
    override def beforeEach(context: BeforeEach): Unit = {
      name = context.test.name
    }
  }
  val name2 = name
  override def munitFixtures: Seq[Fixture[_]] = List(name, name2)

  test("basic") {
    assertEquals(name(), "basic")
  }

  test("two") {
    assertEquals(name(), "two")
    assertEquals(name(), name2())
  }
}
