package munit

class DiffsSuite extends FunSuite { self =>
  test("ansi") {
    val diff1 = munit.diff.Diffs.unifiedDiff("a", "b", 1)
    val diff2 = munit.diff.Diffs.unifiedDiff("a", "c", 1)
    val obtained = munit.diff.Diffs.unifiedDiff(diff1, diff2, 1)
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

  def check(
      name: String,
      a: String,
      b: String,
      expected: String
  )(implicit loc: Location): Unit = {
    test(name) {
      val obtained = munit.diff.Diffs.unifiedDiff(a, b, 1)
      assertNoDiff(obtained, expected)
    }
  }

  check(
    "trailing-whitespace",
    "a\nb",
    "a \nb",
    """|-a
       |+a ∙
       | b
       |""".stripMargin
  )

  check(
    "windows-crlf",
    "a\r\nb",
    "a\nb",
    ""
  )

  test("contextSize") {
    val a = munit.Assertions.munitPrint(
      List("a", "a", "a", "a", "a", "a", "a", "a", "a")
    )
    val b = munit.Assertions.munitPrint(
      List("a", "a", "a", "a", "b", "a", "a", "a", "a")
    )
    val defaultDiff = munit.diff.Diffs.unifiedDiff(a, b, 1)
    assertNoDiff(
      defaultDiff,
      """|   "a",
         |+  "b",
         |   "a",
         |   "a",
         |-  "a",
         |   "a"
         |""".stripMargin
    )

    val bigDiff = munit.diff.Diffs.unifiedDiff(a, b, 10)
    assertNoDiff(
      bigDiff,
      """|List(
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
         |""".stripMargin
    )
  }

}
