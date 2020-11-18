package munit

class TestNameFrameworkSuite extends FunSuite {
  test("basic") {}
  test("basic") {}
  test("newline\n") {}
  test("carriage-return\r\n") {}
  test("tab\t") {}
  test("return\t") {}
  test("form-feed\f") {}
  test("substitute\u001az") {}
  test("emoji😆") {}
  test("red" + Console.RED) {}
}

object TestNameFrameworkSuite
    extends FrameworkTest(
      classOf[TestNameFrameworkSuite],
      s"""|==> success munit.TestNameFrameworkSuite.basic
          |==> success munit.TestNameFrameworkSuite.basic-1
          |==> success munit.TestNameFrameworkSuite.newline\\n
          |==> success munit.TestNameFrameworkSuite.carriage-return\\r\\n
          |==> success munit.TestNameFrameworkSuite.tab\\t
          |==> success munit.TestNameFrameworkSuite.return\\t
          |==> success munit.TestNameFrameworkSuite.form-feed\\f
          |==> success munit.TestNameFrameworkSuite.substitute\\u001az
          |==> success munit.TestNameFrameworkSuite.emoji😆
          |==> success munit.TestNameFrameworkSuite.red\\u001b[31m
          |""".stripMargin
    )
