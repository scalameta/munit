package munit

import munit.diff.Diff

object Diffs {

  def assertNoDiff(
      obtained: String,
      expected: String,
      handler: ComparisonFailExceptionHandler,
      title: String,
      printObtainedAsStripMargin: Boolean,
  )(implicit loc: Location): Boolean = {
    if (obtained.isEmpty && !expected.isEmpty) {
      val msg = s"""|Obtained empty output!
                    |=> Expected:
                    |$expected""".stripMargin
      handler.handle(msg, obtained, expected, loc)
    }
    val diff = new Diff(obtained, expected)
    if (diff.isEmpty) true
    else handler.handle(
      diff.createReport(title, printObtainedAsStripMargin),
      obtained,
      expected,
      loc,
    )
  }

}
