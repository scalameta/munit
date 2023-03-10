package munit

class SuiteLocalFixtureSuite extends FunSuite {
  private val counter = new Fixture[Int]("counter") {
    private var n: Int = -1
    def apply(): Int = {
      n += 1
      n
    }
    override def beforeAll(): Unit = {
      assertEquals(n, 0)
      n = 1
    }
    override def afterAll(): Unit = {
      assertEquals(n, 16)
      n = -11
    }
  }

  override def munitFixtures: Seq[Fixture[_]] = List(counter)

  override def beforeAll(): Unit = {
    assertEquals(counter(), 0)
  }

  override def beforeEach(context: BeforeEach): Unit = {
    val n = context.test.name.toInt
    assertEquals(counter(), n * 3 - 1)
  }

  override def afterEach(context: AfterEach): Unit = {
    val n = context.test.name.toInt
    assertEquals(counter(), n * 3 + 1)
  }

  override def afterAll(): Unit = {
    assertEquals(counter(), -10)
  }

  1.to(5).foreach { i =>
    test(i.toString()) {
      assertEquals(counter(), 3 * i)
    }
  }
}
