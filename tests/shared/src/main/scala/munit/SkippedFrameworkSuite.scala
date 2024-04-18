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
         |""".stripMargin,
      format = SbtFormat
    )

object SkippedFrameworkStdoutJsNativeSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|munit.SkippedFrameworkSuite:
         |  + pass <elapsed time>
         |==> i ignore ignored
         |  + assume(true) <elapsed time>
         |==> s assume(false) skipped
         |""".stripMargin,
      format = StdoutFormat,
      tags = Set(NoJVM)
    )

object SkippedFrameworkStdoutJsNativeVerboseSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|munit.SkippedFrameworkSuite:
         |pass started
         |  + pass <elapsed time>
         |==> i ignore ignored
         |assume(true) started
         |  + assume(true) <elapsed time>
         |assume(false) started
         |==> s assume(false) skipped
         |""".stripMargin,
      format = StdoutFormat,
      tags = Set(NoJVM),
      arguments = Array("-v")
    )

object SkippedFrameworkStdoutJVMSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|munit.SkippedFrameworkSuite:
         |  + pass <elapsed time>
         |==> i munit.SkippedFrameworkSuite.ignore ignored <elapsed time>
         |  + assume(true) <elapsed time>
         |==> s munit.SkippedFrameworkSuite.assume(false) skipped <elapsed time>
         |""".stripMargin,
      format = StdoutFormat,
      tags = Set(OnlyJVM)
    )

object SkippedFrameworkStdoutJVMVerboseSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|munit.SkippedFrameworkSuite started
         |munit.SkippedFrameworkSuite:
         |munit.SkippedFrameworkSuite.pass started
         |  + pass <elapsed time>
         |==> i munit.SkippedFrameworkSuite.ignore ignored <elapsed time>
         |munit.SkippedFrameworkSuite.assume(true) started
         |  + assume(true) <elapsed time>
         |munit.SkippedFrameworkSuite.assume(false) started
         |==> s munit.SkippedFrameworkSuite.assume(false) skipped <elapsed time>
         |Test run munit.SkippedFrameworkSuite finished: 0 failed, 1 ignored, 3 total, <elapsed time>
         |""".stripMargin,
      format = StdoutFormat,
      tags = Set(OnlyJVM),
      arguments = Array("-v")
    )
