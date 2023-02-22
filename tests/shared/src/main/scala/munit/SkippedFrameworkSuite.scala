package munit

class SkippedFrameworkSuite extends FunSuite {
  test("pass") {
    // println("pass")
  }
  test("ignore".ignore) {
    ???
  }
  test("assume(true)") {
    assume(true, "assume it passes")
    // println("pass")
  }
  test("assume(false)") {
    assume(false, "assume it fails")
  }
}

object SkippedFrameworkSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|==> success munit.SkippedFrameworkSuite.pass
         |==> ignored munit.SkippedFrameworkSuite.ignore
         |==> success munit.SkippedFrameworkSuite.assume(true)
         |==> skipped munit.SkippedFrameworkSuite.assume(false) - assume it fails
         |""".stripMargin
    )
