package munit

class ScalaVersionFrameworkSuite extends munit.FunSuite {
  val scalaVersion = "2.12.100"
  override def munitNewTest(test: Test): Test =
    test.withName(test.name + "-" + scalaVersion)
  test("foo") {
    assertEquals(List(1).head, 1)
  }
}

object ScalaVersionFrameworkSuite
    extends FrameworkTest(
      classOf[ScalaVersionFrameworkSuite],
      """|==> success munit.ScalaVersionFrameworkSuite.foo-2.12.100
         |""".stripMargin
    )
