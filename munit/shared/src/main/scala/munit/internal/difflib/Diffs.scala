package munit.internal.difflib

import munit.Location

object Diffs {

  def create(obtained: String, expected: String): Diff =
    new Diff(obtained, expected)

  @deprecated("")
  def assertNoDiff(
      obtained: String,
      expected: String,
      fail: String => Nothing,
      title: String = "",
      printObtainedAsStripMargin: Boolean = true
  )(implicit loc: Location): Boolean = {
    assertNoDiff(
      obtained,
      expected,
      new ComparisonFailExceptionHandler {
        def handle(
            message: String,
            obtained: String,
            expected: String,
            loc: Location
        ): Nothing = fail(message)
      },
      title,
      printObtainedAsStripMargin
    )
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      handler: ComparisonFailExceptionHandler,
      title: String,
      printObtainedAsStripMargin: Boolean
  )(implicit loc: Location): Boolean = {
    if (obtained.isEmpty && !expected.isEmpty) {
      val msg = 
        s"""|Obtained empty output!
            |=> Expected:
            |$expected""".stripMargin
      handler.handle(msg, obtained, expected, loc)
    }
    val diff = new Diff(obtained, expected)
    if (diff.isEmpty) true
    else {
      handler.handle(
        diff.createReport(title, printObtainedAsStripMargin),
        obtained,
        expected,
        loc
      )
    }
  }

  def createDiffOnlyReport(
      obtained: String,
      expected: String
  ): String = {
    create(obtained, expected).createDiffOnlyReport()
  }

  def createReport(
      obtained: String,
      expected: String,
      title: String,
      printObtainedAsStripMargin: Boolean = true
  ): String = {
    create(obtained, expected).createReport(title, printObtainedAsStripMargin)
  }

  def unifiedDiff(obtained: String, expected: String): String = {
    create(obtained, expected).unifiedDiff
  }

}
