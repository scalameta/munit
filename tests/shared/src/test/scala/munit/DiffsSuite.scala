package munit

import munit.diff.console.AnsiColors
import munit.diff.{Diff, DiffOptions}

class DiffsSuite extends FunSuite {
  self =>
  test("ansi") {
    val diff1 = Diff.unifiedDiff("a", "b")
    val diff2 = Diff.unifiedDiff("a", "c")
    val obtained = Diff.unifiedDiff(diff1, diff2)
    // Asserts that a roundtrip of ANSI color processing still produces
    // intuitive results.
    assertNoDiff(
      obtained,
      """|--c
         |+-b
         | +a
         |""".stripMargin,
    )
  }

  def check(name: String, a: String, b: String, expected: String)(implicit
      loc: Location
  ): Unit = test(name) {
    val obtained = Diff.unifiedDiff(a, b)
    assertNoDiff(obtained, expected)
  }

  check(
    "trailing-whitespace",
    "a\nb",
    "a \nb",
    """|-a ∙
       |+a
       | b
       |""".stripMargin,
  )

  check("windows-crlf", "a\r\nb", "a\nb", "")

  private val listWithAOrig = List("a", "a", "a", "a", "a", "a", "a", "a", "a")
  private val listWithBOrig = List("a", "a", "a", "a", "b", "a", "a", "a", "a")
  private val listWithA = munit.Assertions.munitPrint(listWithAOrig)
  private val listWithB = munit.Assertions.munitPrint(listWithBOrig)

  test("DiffOptions: default") {
    implicit val diffOptions = DiffOptions.withContextSize(1)
    assertNoDiff(
      Diff.unifiedDiff(listWithB, listWithA),
      """|   "a",
         |+  "b",
         |   "a",
         |   "a",
         |-  "a",
         |   "a"
         |""".stripMargin,
    )
  }

  def testAnsi(ansi: Boolean): Unit = test(s"DiffOptions: ansi=$ansi") {
    def mark(color: String)(str: String): String =
      if (ansi) AnsiColors.c(str, color) else str
    val bold = mark(AnsiColors.Bold) _
    val red = mark(AnsiColors.LightRed) _
    val grn = mark(AnsiColors.LightGreen) _

    val error = // with ansi markers
      s"""|values are not the same
          |${bold("=> Obtained")}
          |List(
          |  "a",
          |  "a",
          |  "a",
          |  "a",
          |  "b",
          |  "a",
          |  "a",
          |  "a",
          |  "a"
          |)
          |${bold("=> Diff")} (${red("- expected")}, ${grn("+ obtained")})
          |   "a",
          |${grn("""+  "b",""")}
          |   "a",
          |   "a",
          |${red("""-  "a",""")}
          |   "a"""".stripMargin
    locally {
      implicit val loc = munit.Location.empty
      implicit val diffOptions = DiffOptions.withForceAnsi(Some(ansi))
      assertEquals(
        intercept[ComparisonFailException](assertEquals(listWithB, listWithA))
          .message,
        error,
      )
    }
  }

  testAnsi(true)
  testAnsi(false)

  test("DiffOptions: contextSize=10") {
    implicit val diffOptions = DiffOptions.withContextSize(10)
    assertNoDiff(
      Diff.unifiedDiff(listWithB, listWithA),
      """| List(
         |   "a",
         |   "a",
         |   "a",
         |   "a",
         |+  "b",
         |   "a",
         |   "a",
         |   "a",
         |-  "a",
         |   "a"
         | )
         |""".stripMargin,
    )
  }

  test("DiffOptions: showLines=true") {
    implicit val diffOptions = DiffOptions.withShowLines(true)
    assertNoDiff(
      Diff.unifiedDiff(listWithB, listWithA),
      """|@@ -5,2 +5,3 @@
         |   "a",
         |+  "b",
         |   "a",
         |@@ -8,3 +9,2 @@
         |   "a",
         |-  "a",
         |   "a"
         |""".stripMargin,
    )
  }

  test("DiffOptions: triple-quote") {
    implicit val diffOptions = DiffOptions.withObtainedAsStripMargin(true)
    assertNoDiff(
      Diff.unifiedDiff(listWithB, listWithA),
      """|   "a",
         |+  "b",
         |   "a",
         |   "a",
         |-  "a",
         |   "a"
         |""".stripMargin,
    )
  }

  test("DiffOptions: printer") {
    implicit val diffOptions: DiffOptions = DiffOptions
      .withPrinter(Some { (value: Any, out: StringBuilder, indent: Int) =>
        out.append("indent=").append(indent).append(": [\n  ").append(value)
          .append("\n]")
        true
      })
    implicit val loc: Location = munit.Location.empty
    assertNoDiff(
      intercept[ComparisonFailException](
        assertEquals(listWithBOrig, listWithAOrig)
      ).message,
      """|values are not the same
         |=> Obtained
         |indent=0: [
         |  List(a, a, a, a, b, a, a, a, a)
         |]
         |=> Diff (- expected, + obtained)
         | indent=0: [
         |-  List(a, a, a, a, a, a, a, a, a)
         |+  List(a, a, a, a, b, a, a, a, a)
         | ]""".stripMargin,
    )
  }

}
