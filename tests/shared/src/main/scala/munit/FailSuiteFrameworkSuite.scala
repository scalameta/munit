package munit

class FailSuiteFrameworkSuite extends FunSuite {
  test("pass") {
    // println("pass")
  }
  test("fail")(failSuite("Oops, can not do anything."))
  test(name = "not gonna run") {
    // println("not pass")
  }
}

object FailSuiteFrameworkSuite
    extends FrameworkTest(
      classOf[FailSuiteFrameworkSuite],
      """|==> success munit.FailSuiteFrameworkSuite.pass
         |==> failure munit.FailSuiteFrameworkSuite.fail - tests/shared/src/main/scala/munit/FailSuiteFrameworkSuite.scala:7 Oops, can not do anything.
         |6:  }
         |7:  test("fail")(failSuite("Oops, can not do anything."))
         |8:  test(name = "not gonna run") {
         |==> skipped munit.FailSuiteFrameworkSuite.not gonna run - Suite has been aborted
         |""".stripMargin,
    )
