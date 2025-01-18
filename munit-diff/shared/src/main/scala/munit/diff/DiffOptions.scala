package munit.diff

class DiffOptions private (
    val contextSize: Int,
    val showLines: Boolean,
    val obtainedAsStripMargin: Boolean,
) {
  private def privateCopy(
      contextSize: Int = this.contextSize,
      showLines: Boolean = this.showLines,
      obtainedAsStripMargin: Boolean = this.obtainedAsStripMargin,
  ): DiffOptions = new DiffOptions(
    contextSize = contextSize,
    showLines = showLines,
    obtainedAsStripMargin = obtainedAsStripMargin,
  )

  def withContextSize(value: Int): DiffOptions = privateCopy(contextSize = value)
  def withShowLines(value: Boolean): DiffOptions = privateCopy(showLines = value)
  def withObtainedAsStripMargin(value: Boolean): DiffOptions =
    privateCopy(obtainedAsStripMargin = value)
}

object DiffOptions
    extends DiffOptions(
      contextSize = 1,
      showLines = false,
      obtainedAsStripMargin = false,
    ) {
  implicit val default: DiffOptions = this
}
