package munit

import org.junit.experimental.categories.Category

class Slow extends Tag("Slow")

@Category(Array(classOf[Slow]))
class Issue497FrameworkSuite extends FunSuite {
  def println(msg: String): Unit = TestingConsole.out.println(msg)
  val myFixture = new Fixture[Unit]("myFixture") {
    def apply(): Unit = println("### myFixture apply() ###")

    override def beforeAll(): Unit = {
      println("### beforeAll is running ###")
    }

    override def afterAll(): Unit = {
      println("### afterAll is running ###")
    }
  }
  override def munitFixtures = List(myFixture)

  test("test1") {
    myFixture()
    assertEquals(1, 1)
  }

  test("test2") {
    myFixture()
    assertEquals(1, 1)
  }
}

object Issue497FrameworkSuite
    extends FrameworkTest(
      classOf[Issue497FrameworkSuite],
      """|munit.Issue497FrameworkSuite:
         |""".stripMargin,
      arguments = Array("--exclude-categories=munit.Slow"),
      tags = Set(
        OnlyJVM
      ),
      format = StdoutFormat
    )
