package munit.diff

class DiffOptions private (
    val forceAnsi: Option[Boolean],
    val contextSize: Int,
    val showLines: Boolean,
    val obtainedAsStripMargin: Boolean,
    val printer: Option[Printer],
) {
  private def privateCopy(
      forceAnsi: Option[Boolean] = this.forceAnsi,
      contextSize: Int = this.contextSize,
      showLines: Boolean = this.showLines,
      obtainedAsStripMargin: Boolean = this.obtainedAsStripMargin,
      printer: Option[Printer] = this.printer,
  ): DiffOptions = new DiffOptions(
    forceAnsi = forceAnsi,
    contextSize = contextSize,
    showLines = showLines,
    obtainedAsStripMargin = obtainedAsStripMargin,
    printer = printer,
  )

  def withForceAnsi(value: Option[Boolean]): DiffOptions =
    privateCopy(forceAnsi = value)
  def ansi(orElse: => Boolean): Boolean = forceAnsi.getOrElse(orElse)
  def ansi: Boolean = ansi(true)

  def withContextSize(value: Int): DiffOptions = privateCopy(contextSize = value)
  def withShowLines(value: Boolean): DiffOptions = privateCopy(showLines = value)
  def withObtainedAsStripMargin(value: Boolean): DiffOptions =
    privateCopy(obtainedAsStripMargin = value)
  def withPrinter(value: Option[Printer]): DiffOptions =
    privateCopy(printer = value)
}

object DiffOptions
    extends DiffOptions(
      forceAnsi = None,
      contextSize = 1,
      showLines = false,
      obtainedAsStripMargin = false,
      printer = None,
    ) {
  implicit val default: DiffOptions = this
}
