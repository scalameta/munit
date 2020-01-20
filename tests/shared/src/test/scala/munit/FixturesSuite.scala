package munit

class FixturesSuite extends FunSuite with Fixtures {
  val name: Fixture[String] = new Fixture[String] {
    def beforeEach(context: BeforeEachFixture): String = {
      context.options.name
    }
    def afterEach(context: AfterEachFixture): Unit = ()
  }
  val name2: Fixture[(String, String)] = Fixture.map2(name, name)

  name.test("basic") { name =>
    assertEquals(name, "basic")
  }

  name2.test("map2") {
    case (name1, name2) =>
      assertEquals(name1, "map2")
      assertEquals(name1, name2)
  }
}
