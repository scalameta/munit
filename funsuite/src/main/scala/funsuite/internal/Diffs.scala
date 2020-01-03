package funsuite.internal

import collection.JavaConverters._
import funsuite.Assertions
import funsuite.Location
import fansi.Str
import fansi.Color
import fansi.Bold

object Diffs {

  def assertNoDiffOrPrintExpected(
      obtained: String,
      expected: String,
      title: String = ""
  )(implicit loc: Location): Boolean = {
    try assertNoDiff(obtained, expected, title)
    catch {
      case ex: Exception =>
        obtained.linesIterator.toList match {
          case head +: tail =>
            println("    \"\"\"|" + head)
            tail.foreach(line => println("       |" + line))
            println("       |\"\"\".stripMargin" + head)
          case head +: Nil =>
            println(head)
          case Nil =>
            println("obtained is empty")
        }
        throw ex
    }
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      title: String = ""
  )(implicit loc: Location): Boolean = {
    if (obtained.isEmpty && !expected.isEmpty) {
      Assertions.fail("Obtained empty output!")
    }
    val result = compareContents(obtained, expected)
    if (result.isEmpty) true
    else Assertions.fail(createDiff(obtained, expected, title))
  }

  def createDiffOnly(
      obtained: String,
      expected: String
  ): String = {
    val out = new StringBuilder
    createDiffOnly(obtained, expected, out)
    out.toString()
  }
  private def createDiffOnly(
      obtained: String,
      expected: String,
      sb: StringBuilder
  ): Unit = {
    header(
      Str
        .join(
          "Diff (",
          Color.LightRed("- obtained"),
          ", ",
          Color.LightGreen("+ expected"),
          ")"
        )
        .render,
      sb
    )
    sb.append(stripTrailingWhitespace(compareContents(obtained, expected)))
  }
  def createDiff(obtained: String, expected: String, title: String): String = {
    val sb = new StringBuilder
    if (title.nonEmpty) {
      sb.append(title)
        .append("\n")
    }
    if (obtained.length < 1000) {
      header("Obtained", sb)
      sb.append(asStripMargin(obtained))
        .append("\n")
    }
    createDiffOnly(obtained, expected, sb)
    sb.toString()
  }

  private def header(t: String, sb: StringBuilder): Unit = {
    sb.append(Bold.On(s"=> $t"))
      .append("\n")
  }

  private def asStripMargin(obtained: String): String = {
    if (!obtained.contains("\n")) pprint.tokenize(obtained).mkString
    else {
      val out = new StringBuilder
      out.append("    \"\"\"|\n")
      obtained.trim.linesIterator.foreach(line =>
        out.append("       |").append(line).append("\n")
      )
      out.append("       |\"\"\".stripMargin")
      out.toString()
    }
  }

  private def stripTrailingWhitespace(str: String): String =
    str.replaceAll(" \n", "∙\n")

  private def splitIntoLines(string: String): Seq[String] =
    string.trim.replace("\r\n", "\n").split("\n")

  private def compareContents(original: String, revised: String): String =
    compareContents(splitIntoLines(original), splitIntoLines(revised))

  private def compareContents(
      original: Seq[String],
      revised: Seq[String]
  ): String = {
    val diff = difflib.DiffUtils.diff(original.asJava, revised.asJava)
    if (diff.getDeltas.isEmpty) ""
    else {
      difflib.DiffUtils
        .generateUnifiedDiff(
          "obtained",
          "expected",
          original.asJava,
          diff,
          1
        )
        .asScala
        .iterator
        .drop(2)
        .filterNot(_.startsWith("@@"))
        .map { line =>
          if (line.startsWith("-")) Color.LightRed(line).render
          else if (line.startsWith("+")) Color.LightGreen(line).render
          else line
        }
        .mkString("\n")
    }
  }

}
