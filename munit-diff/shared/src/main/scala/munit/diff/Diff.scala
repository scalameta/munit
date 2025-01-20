package munit.diff

import munit.diff.console.AnsiColors
import munit.diff.console.Printers

import scala.collection.JavaConverters._

class Diff(val obtained: String, val expected: String, val options: DiffOptions)
    extends Serializable {
  import Diff._

  def this(obtained: String, expected: String) =
    this(obtained, expected, implicitly[DiffOptions])

  private implicit def diffOptions: DiffOptions = options

  val obtainedClean: String = AnsiColors.filterAnsi(obtained)
  val expectedClean: String = AnsiColors.filterAnsi(expected)
  val obtainedLines: Seq[String] = splitIntoLines(obtainedClean)
  val expectedLines: Seq[String] = splitIntoLines(expectedClean)
  val unifiedDiff: String = createUnifiedDiff(expectedLines, obtainedLines)
  def isEmpty: Boolean = unifiedDiff.isEmpty

  def createReport(
      title: String,
      printObtainedAsStripMargin: Boolean = true,
  ): String = {
    implicit val sb: StringBuilder = new StringBuilder
    if (title.nonEmpty) sb.append(title).append("\n")
    if (obtainedClean.length < 1000) {
      header("Obtained")
      sb.append("\n")
      sb.append(asStripMargin(obtainedClean, printObtainedAsStripMargin))
      sb.append("\n")
    }
    appendDiffOnlyReport
    sb.toString()
  }

  def createReport(title: String): String =
    createReport(title, options.obtainedAsStripMargin)

  def createDiffOnlyReport(): String = {
    implicit val out: StringBuilder = new StringBuilder
    appendDiffOnlyReport
    out.toString()
  }

  private def appendDiffOnlyReport(implicit sb: StringBuilder): Unit = {
    header("Diff")
    sb.append(" (")
    AnsiColors.c(AnsiColors.LightRed, options.ansi)(_.append("- expected"))
    sb.append(", ")
    AnsiColors.c(AnsiColors.LightGreen, options.ansi)(_.append("+ obtained"))
    sb.append(")\n")
    sb.append(unifiedDiff)
  }

}

object Diff {

  def apply(obtained: String, expected: String)(implicit
      options: DiffOptions
  ): Diff = new Diff(obtained, expected, options)

  def createDiffOnlyReport(obtained: String, expected: String)(implicit
      options: DiffOptions
  ): String = apply(obtained, expected).createDiffOnlyReport()

  def createReport(obtained: String, expected: String, title: String)(implicit
      options: DiffOptions
  ): String = apply(obtained, expected).createReport(title)

  def unifiedDiff(obtained: String, expected: String)(implicit
      options: DiffOptions
  ): String = apply(obtained, expected).unifiedDiff

  private def asStripMargin(obtained: String, flag: Boolean): String =
    if (!flag) obtained
    else if (!obtained.contains("\n")) Printers.print(obtained)
    else {
      val out = new StringBuilder
      val lines = obtained.trim.linesIterator
      val head = if (lines.hasNext) lines.next() else ""
      out.append("    \"\"\"|" + head + "\n")
      lines.foreach(line => out.append("       |").append(line).append("\n"))
      out.append("       |\"\"\".stripMargin")
      out.toString()
    }

  private def header(
      t: String
  )(implicit sb: StringBuilder, options: DiffOptions): Unit = AnsiColors
    .c(AnsiColors.Bold, options.ansi)(_.append("=> ").append(t))

  private def createUnifiedDiff(original: Seq[String], revised: Seq[String])(
      implicit options: DiffOptions
  ): String = {
    val diff = DiffUtils.diff(original.asJava, revised.asJava)
    if (diff.getDeltas.isEmpty) ""
    else {
      val cs = options.contextSize
      val lines = DiffUtils
        .generateUnifiedDiff("expected", "obtained", original.asJava, diff, cs)
        .asScala.iterator.drop(2)
      val filteredLines =
        if (options.showLines) lines else lines.filterNot(_.startsWith("@@"))
      filteredLines
        .map(line => if (line.lastOption.contains(' ')) line + "∙" else line)
        .map(line =>
          line.headOption match {
            case Some('-') if options.ansi =>
              AnsiColors.c(line, AnsiColors.LightRed)
            case Some('+') if options.ansi =>
              AnsiColors.c(line, AnsiColors.LightGreen)
            case _ => line
          }
        ).mkString("\n")
    }
  }

  private def splitIntoLines(string: String): Seq[String] = string.trim()
    .replace("\r\n", "\n").split("\n").toIndexedSeq

}
