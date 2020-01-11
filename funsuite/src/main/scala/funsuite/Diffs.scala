package funsuite

import collection.JavaConverters._

object Diffs {

  def assertNoDiff(
      obtained: String,
      expected: String,
      title: String = "",
      printObtainedAsStripMargin: Boolean = true
  )(implicit loc: Location): Boolean = {
    if (obtained.isEmpty && !expected.isEmpty) {
      Assertions.fail("Obtained empty output!")
    }
    val result = unifiedDiff(obtained, expected)
    if (result.isEmpty) true
    else {
      Assertions.fail(
        createDiff(obtained, expected, title, printObtainedAsStripMargin)
      )
    }
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
    header("Diff", sb)
    sb.append(
        s" (${AnsiColors.LightRed}- obtained${AnsiColors.Reset}, ${AnsiColors.LightGreen}+ expected${AnsiColors.Reset})"
      )
      .append("\n")
    sb.append(stripTrailingWhitespace(unifiedDiff(obtained, expected)))
  }

  def createDiff(
      obtained: String,
      expected: String,
      title: String,
      printObtainedAsStripMargin: Boolean = true
  ): String = {
    val sb = new StringBuilder
    if (title.nonEmpty) {
      sb.append(title)
        .append("\n")
    }
    if (obtained.length < 1000) {
      header("Obtained", sb).append("\n")
      if (printObtainedAsStripMargin) {
        sb.append(asStripMargin(obtained))
      } else {
        sb.append(obtained)
      }
      sb.append("\n")
    }
    createDiffOnly(obtained, expected, sb)
    sb.toString()
  }

  private def header(t: String, sb: StringBuilder): StringBuilder = {
    sb.append(AnsiColors(s"=> $t", AnsiColors.Bold))
  }

  private def asStripMargin(obtained: String): String = {
    if (!obtained.contains("\n")) pprint.tokenize(obtained).mkString
    else {
      val out = new StringBuilder
      val lines = obtained.trim.linesIterator
      out.append("    \"\"\"|" + lines.next() + "\n")
      lines.foreach(line => out.append("       |").append(line).append("\n"))
      out.append("       |\"\"\".stripMargin")
      out.toString()
    }
  }

  def stripTrailingWhitespace(str: String): String =
    str.replaceAll(" \n", "∙\n")

  def splitIntoLines(string: String): Seq[String] =
    string.trim.replace("\r\n", "\n").split("\n")

  def unifiedDiff(original: String, revised: String): String =
    unifiedDiff(splitIntoLines(original), splitIntoLines(revised))

  def unifiedDiff(
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
          if (line.startsWith("-")) AnsiColors(line, AnsiColors.LightRed)
          else if (line.startsWith("+")) AnsiColors(line, AnsiColors.LightGreen)
          else line
        }
        .mkString("\n")
    }
  }

}
