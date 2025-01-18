package munit

import munit.diff.Diff
import munit.diff.DiffOptions

object Diffs {

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  def assertNoDiff(
      obtained: String,
      expected: String,
      handler: ComparisonFailExceptionHandler,
      title: String,
      printObtainedAsStripMargin: Boolean,
  )(implicit loc: Location): Boolean = {
    implicit val diffOptions: DiffOptions = DiffOptions
      .withObtainedAsStripMargin(printObtainedAsStripMargin)
    assertNoDiff(obtained, expected, handler, title)
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      handler: ComparisonFailExceptionHandler,
      title: String,
  )(implicit loc: Location, options: DiffOptions): Boolean = {
    if (obtained.isEmpty && expected.nonEmpty) {
      val msg = s"""|Obtained empty output!
                    |=> Expected:
                    |$expected""".stripMargin
      handler.handle(msg, obtained, expected, loc)
    }
    val diff = Diff(obtained, expected)
    if (diff.isEmpty) true
    else handler.handle(diff.createReport(title), obtained, expected, loc)
  }

}
