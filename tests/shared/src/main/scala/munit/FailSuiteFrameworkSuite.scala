package munit

class FailSuiteFrameworkSuite extends FunSuite {
  test("pass") {
    // println("pass")
  }
  test("fail") {
    failSuite("Oops, can not do anything.")
  }
  test(name = "not gonna run") {
    // println("not pass")
  }
}

object FailSuiteFrameworkSuite
    extends FrameworkTest(
      classOf[FailSuiteFrameworkSuite],
      """|==> success munit.FailSuiteFrameworkSuite.pass
         |==> failure munit.FailSuiteFrameworkSuite.fail - /scala/munit/FailSuiteFrameworkSuite.scala:8 Oops, can not do anything.
         |7:  test("fail") {
         |8:    failSuite("Oops, can not do anything.")
         |9:  }
         |==> skipped munit.FailSuiteFrameworkSuite.not gonna run - Suite has been aborted
         |""".stripMargin
    )
