package munit

class TestNameFrameworkSuite extends FunSuite {
  test("basic") {
    // pass
  }
  test("basic") {
    // pass
  }
  test("newline\n") {
    // pass
  }
}

object TestNameFrameworkSuite
    extends FrameworkTest(
      classOf[TestNameFrameworkSuite],
      """|==> success munit.TestNameFrameworkSuite.basic
         |==> success munit.TestNameFrameworkSuite.basic-1
         |==> success munit.TestNameFrameworkSuite.newline\n
         |""".stripMargin
    )
