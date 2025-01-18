package munit

import munit.diff.Diff
import munit.diff.DiffOptions

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

  private val listWithA = munit.Assertions
    .munitPrint(List("a", "a", "a", "a", "a", "a", "a", "a", "a"))
  private val listWithB = munit.Assertions
    .munitPrint(List("a", "a", "a", "a", "b", "a", "a", "a", "a"))

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

}
