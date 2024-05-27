package munit.diff

object Diffs {

  def create(obtained: String, expected: String, contextSize: Int): Diff =
    new Diff(obtained, expected, contextSize)

  def createDiffOnlyReport(
      obtained: String,
      expected: String,
      contextSize: Int
  ): String = {
    create(obtained, expected, contextSize).createDiffOnlyReport()
  }

  def createReport(
      obtained: String,
      expected: String,
      title: String,
      contextSize: Int,
      printObtainedAsStripMargin: Boolean = true
  ): String = {
    create(obtained, expected, contextSize).createReport(
      title,
      printObtainedAsStripMargin
    )
  }

  def unifiedDiff(
      obtained: String,
      expected: String,
      contextSize: Int
  ): String = {
    create(obtained, expected, contextSize).unifiedDiff
  }

}
