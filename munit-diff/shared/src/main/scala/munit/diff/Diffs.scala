package munit.diff

object Diffs {

  def create(obtained: String, expected: String): Diff =
    new Diff(obtained, expected)

  def createDiffOnlyReport(obtained: String, expected: String): String =
    create(obtained, expected).createDiffOnlyReport()

  def createReport(
      obtained: String,
      expected: String,
      title: String,
      printObtainedAsStripMargin: Boolean = true,
  ): String = create(obtained, expected)
    .createReport(title, printObtainedAsStripMargin)

  def unifiedDiff(obtained: String, expected: String): String =
    create(obtained, expected).unifiedDiff

}
