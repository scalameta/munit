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
    val sb = new StringBuilder
    if (title.nonEmpty) sb.append(title).append("\n")
    if (obtainedClean.length < 1000) {
      header("Obtained", sb).append("\n")
      if (printObtainedAsStripMargin) sb.append(asStripMargin(obtainedClean))
      else sb.append(obtainedClean)
      sb.append("\n")
    }
    appendDiffOnlyReport(sb)
    sb.toString()
  }

  def createReport(title: String): String =
    createReport(title, options.obtainedAsStripMargin)

  def createDiffOnlyReport(): String = {
    val out = new StringBuilder
    appendDiffOnlyReport(out)
    out.toString()
  }

  private def appendDiffOnlyReport(sb: StringBuilder): Unit = {
    header("Diff", sb)
    val red = AnsiColors.use(AnsiColors.LightRed)
    val reset = AnsiColors.use(AnsiColors.Reset)
    val green = AnsiColors.use(AnsiColors.LightGreen)
    sb.append(s" ($red- expected$reset, $green+ obtained$reset)")
    sb.append("\n")
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

  private def asStripMargin(obtained: String): String =
    if (!obtained.contains("\n")) Printers.print(obtained)
    else {
      val out = new StringBuilder
      val lines = obtained.trim.linesIterator
      val head = if (lines.hasNext) lines.next() else ""
      out.append("    \"\"\"|" + head + "\n")
      lines.foreach(line => out.append("       |").append(line).append("\n"))
      out.append("       |\"\"\".stripMargin")
      out.toString()
    }

  private def header(t: String, sb: StringBuilder): StringBuilder = sb
    .append(AnsiColors.c(s"=> $t", AnsiColors.Bold))

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
            case Some('-') => AnsiColors.c(line, AnsiColors.LightRed)
            case Some('+') => AnsiColors.c(line, AnsiColors.LightGreen)
            case _ => line
          }
        ).mkString("\n")
    }
  }

  private def splitIntoLines(string: String): Seq[String] = string.trim()
    .replace("\r\n", "\n").split("\n").toIndexedSeq

}
