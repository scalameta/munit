package munit

class SkippedFrameworkSuite extends FunSuite {
  test("pass") {
    // println("pass")
  }
  test("ignore".ignore) {
    ???
  }
  test("ignore.failed.pending".ignore.pending) {
    assert(false)
  }
  test("ignore.failed.pending.comment".ignore.pending("comment")) {
    assert(false)
  }
  test("assume(true)") {
    assume(true, "assume it passes")
    // println("pass")
  }
  test("assume(false)") {
    assume(false, "assume it fails")
  }
  test("pending.empty.ignored".pending) {
    //
  }
  test("pending.empty.ignored.comment".pending("comment")) {
    //
  }
  test("pending.successful.ignored".pending) {
    assert(true)
  }
  test("pending.successful.ignored.comment".pending("comment")) {
    assert(true)
  }
  test("pending.failed.not-ignored".pending) {
    assert(false)
  }
  test("pending.failed.not-ignored.comment".pending("comment")) {
    assert(false)
  }
  test("pending.failed.ignored".pending.ignore) {
    assert(false)
  }
  test("pending.failed.ignored.comment".pending("comment").ignore) {
    assert(false)
  }
}

object SkippedFrameworkSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|==> success munit.SkippedFrameworkSuite.pass
         |==> ignored munit.SkippedFrameworkSuite.ignore
         |==> ignored munit.SkippedFrameworkSuite.ignore.failed.pending
         |==> ignored munit.SkippedFrameworkSuite.ignore.failed.pending.comment
         |==> success munit.SkippedFrameworkSuite.assume(true)
         |==> skipped munit.SkippedFrameworkSuite.assume(false) - assume it fails
         |==> ignored munit.SkippedFrameworkSuite.pending.empty.ignored
         |==> ignored munit.SkippedFrameworkSuite.pending.empty.ignored.comment
         |==> ignored munit.SkippedFrameworkSuite.pending.successful.ignored
         |==> ignored munit.SkippedFrameworkSuite.pending.successful.ignored.comment
         |==> failure munit.SkippedFrameworkSuite.pending.failed.not-ignored - tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:36 assertion failed
         |35:  test("pending.failed.not-ignored".pending) {
         |36:    assert(false)
         |37:  }
         |==> failure munit.SkippedFrameworkSuite.pending.failed.not-ignored.comment - tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:39 assertion failed
         |38:  test("pending.failed.not-ignored.comment".pending("comment")) {
         |39:    assert(false)
         |40:  }
         |==> ignored munit.SkippedFrameworkSuite.pending.failed.ignored
         |==> ignored munit.SkippedFrameworkSuite.pending.failed.ignored.comment
         |""".stripMargin,
      format = SbtFormat
    )

object SkippedFrameworkStdoutJsNativeSuite
    extends FrameworkTest(
      classOf[SkippedFrameworkSuite],
      """|munit.SkippedFrameworkSuite:
         |  + pass <elapsed time>
         |==> i ignore ignored <elapsed time>
         |==> i ignore.failed.pending PENDING ignored <elapsed time>
         |==> i ignore.failed.pending.comment PENDING comment ignored <elapsed time>
         |  + assume(true) <elapsed time>
         |==> s assume(false) skipped
         |==> i pending.empty.ignored PENDING ignored <elapsed time>
         |==> i pending.empty.ignored.comment PENDING comment ignored <elapsed time>
         |==> i pending.successful.ignored PENDING ignored <elapsed time>
         |==> i pending.successful.ignored.comment PENDING comment ignored <elapsed time>
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:36 assertion failed
         |35:  test("pending.failed.not-ignored".pending) {
         |36:    assert(false)
         |37:  }
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored.comment <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:39 assertion failed
         |38:  test("pending.failed.not-ignored.comment".pending("comment")) {
         |39:    assert(false)
         |40:  }
         |==> i pending.failed.ignored PENDING ignored <elapsed time>
         |==> i pending.failed.ignored.comment PENDING comment ignored <elapsed time>
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
         |==> i ignore ignored <elapsed time>
         |==> i ignore.failed.pending PENDING ignored <elapsed time>
         |==> i ignore.failed.pending.comment PENDING comment ignored <elapsed time>
         |assume(true) started
         |  + assume(true) <elapsed time>
         |assume(false) started
         |==> s assume(false) skipped
         |pending.empty.ignored started
         |==> i pending.empty.ignored PENDING ignored <elapsed time>
         |pending.empty.ignored.comment started
         |==> i pending.empty.ignored.comment PENDING comment ignored <elapsed time>
         |pending.successful.ignored started
         |==> i pending.successful.ignored PENDING ignored <elapsed time>
         |pending.successful.ignored.comment started
         |==> i pending.successful.ignored.comment PENDING comment ignored <elapsed time>
         |pending.failed.not-ignored started
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:36 assertion failed
         |35:  test("pending.failed.not-ignored".pending) {
         |36:    assert(false)
         |37:  }
         |pending.failed.not-ignored.comment started
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored.comment <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:39 assertion failed
         |38:  test("pending.failed.not-ignored.comment".pending("comment")) {
         |39:    assert(false)
         |40:  }
         |==> i pending.failed.ignored PENDING ignored <elapsed time>
         |==> i pending.failed.ignored.comment PENDING comment ignored <elapsed time>
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
         |==> i munit.SkippedFrameworkSuite.ignore.failed.pending PENDING ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.ignore.failed.pending.comment PENDING comment ignored <elapsed time>
         |  + assume(true) <elapsed time>
         |==> s munit.SkippedFrameworkSuite.assume(false) skipped <elapsed time>
         |==> i munit.SkippedFrameworkSuite.pending.empty.ignored PENDING ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.pending.empty.ignored.comment PENDING comment ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.pending.successful.ignored PENDING ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.pending.successful.ignored.comment PENDING comment ignored <elapsed time>
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored  <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:36 assertion failed
         |35:  test("pending.failed.not-ignored".pending) {
         |36:    assert(false)
         |37:  }
         |    at munit.FunSuite.assert(FunSuite.scala:11)
         |    at munit.SkippedFrameworkSuite.$anonfun$new$21(SkippedFrameworkSuite.scala:36)
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored.comment  <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:39 assertion failed
         |38:  test("pending.failed.not-ignored.comment".pending("comment")) {
         |39:    assert(false)
         |40:  }
         |    at munit.FunSuite.assert(FunSuite.scala:11)
         |    at munit.SkippedFrameworkSuite.$anonfun$new$24(SkippedFrameworkSuite.scala:39)
         |==> i munit.SkippedFrameworkSuite.pending.failed.ignored PENDING ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.pending.failed.ignored.comment PENDING comment ignored <elapsed time>
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
         |==> i munit.SkippedFrameworkSuite.ignore.failed.pending PENDING ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.ignore.failed.pending.comment PENDING comment ignored <elapsed time>
         |munit.SkippedFrameworkSuite.assume(true) started
         |  + assume(true) <elapsed time>
         |munit.SkippedFrameworkSuite.assume(false) started
         |==> s munit.SkippedFrameworkSuite.assume(false) skipped <elapsed time>
         |munit.SkippedFrameworkSuite.pending.empty.ignored started
         |==> i munit.SkippedFrameworkSuite.pending.empty.ignored PENDING ignored <elapsed time>
         |munit.SkippedFrameworkSuite.pending.empty.ignored.comment started
         |==> i munit.SkippedFrameworkSuite.pending.empty.ignored.comment PENDING comment ignored <elapsed time>
         |munit.SkippedFrameworkSuite.pending.successful.ignored started
         |==> i munit.SkippedFrameworkSuite.pending.successful.ignored PENDING ignored <elapsed time>
         |munit.SkippedFrameworkSuite.pending.successful.ignored.comment started
         |==> i munit.SkippedFrameworkSuite.pending.successful.ignored.comment PENDING comment ignored <elapsed time>
         |munit.SkippedFrameworkSuite.pending.failed.not-ignored started
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored  <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:36 assertion failed
         |35:  test("pending.failed.not-ignored".pending) {
         |36:    assert(false)
         |37:  }
         |    at munit.FunSuite.assert(FunSuite.scala:11)
         |    at munit.SkippedFrameworkSuite.$anonfun$new$21(SkippedFrameworkSuite.scala:36)
         |munit.SkippedFrameworkSuite.pending.failed.not-ignored.comment started
         |==> X munit.SkippedFrameworkSuite.pending.failed.not-ignored.comment  <elapsed time>munit.FailException: tests/shared/src/main/scala/munit/SkippedFrameworkSuite.scala:39 assertion failed
         |38:  test("pending.failed.not-ignored.comment".pending("comment")) {
         |39:    assert(false)
         |40:  }
         |    at munit.FunSuite.assert(FunSuite.scala:11)
         |    at munit.SkippedFrameworkSuite.$anonfun$new$24(SkippedFrameworkSuite.scala:39)
         |==> i munit.SkippedFrameworkSuite.pending.failed.ignored PENDING ignored <elapsed time>
         |==> i munit.SkippedFrameworkSuite.pending.failed.ignored.comment PENDING comment ignored <elapsed time>
         |Test run munit.SkippedFrameworkSuite finished: 2 failed, 9 ignored, 9 total, <elapsed time>
         |""".stripMargin,
      format = StdoutFormat,
      tags = Set(OnlyJVM),
      arguments = Array("-v")
    )
