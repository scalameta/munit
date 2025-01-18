package munit

class DiffsSuite extends FunSuite {
  self =>
  test("ansi") {
    val diff1 = munit.diff.Diffs.unifiedDiff("a", "b")
    val diff2 = munit.diff.Diffs.unifiedDiff("a", "c")
    val obtained = munit.diff.Diffs.unifiedDiff(diff1, diff2)
    // Asserts that a roundtrip of ANSI color processing still produces
    // intuitive results.
    assertNoDiff(
      obtained,
      """|-a
         |-+b
         |++c
         |""".stripMargin,
    )
  }

  def check(name: String, a: String, b: String, expected: String)(implicit
      loc: Location
  ): Unit = test(name) {
    val obtained = munit.diff.Diffs.unifiedDiff(a, b)
    assertNoDiff(obtained, expected)
  }

  check(
    "trailing-whitespace",
    "a\nb",
    "a \nb",
    """|-a
       |+a ∙
       | b
       |""".stripMargin,
  )

  check("windows-crlf", "a\r\nb", "a\nb", "")

}
