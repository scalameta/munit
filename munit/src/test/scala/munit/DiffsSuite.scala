package munit

class DiffsSuite extends FunSuite {
  test("ansi") {
    val diff1 = Diffs.unifiedDiff("a", "b")
    val diff2 = Diffs.unifiedDiff("a", "c")
    val obtained = Diffs.unifiedDiff(diff1, diff2)
    // Asserts that a roundtrip of ANSI color processing still produces
    // intuitive results.
    assertNoDiff(
      obtained,
      """|-a
         |-+b
         |++c
         |""".stripMargin
    )
  }
}
