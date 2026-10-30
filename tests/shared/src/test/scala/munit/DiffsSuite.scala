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

  private val obtained999 = "a" * 997 + "\nb"
  private val obtained1000 = "a" * 998 + "\nb"
  private def obtainedReport(
      obtained: String,
      options: DiffOptions = DiffOptions,
  ): String = new Diff(obtained, "b", options.withForceAnsi(Some(false)))
    .createReport("title")

  test("createReport: obtained of 999 characters")(assertNoDiff(
    obtainedReport(obtained999),
    s"""|title
        |=> Obtained
        |$obtained999
        |=> Diff (- expected, + obtained)
        |+${"a" * 997}
        | b""".stripMargin,
  ))

  test("createReport: obtained of 1000 characters")(assertNoDiff(
    obtainedReport(obtained1000),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("DiffOptions: obtainedMaxLength=998")(assertNoDiff(
    obtainedReport(obtained999, DiffOptions.withObtainedMaxLength(998)),
    s"""|title
        |=> Obtained not shown: 999 characters, limit 998 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 997}
        | b""".stripMargin,
  ))

  test("DiffOptions: obtainedMaxLength=1000")(assertNoDiff(
    obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(1000)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("DiffOptions: obtainedMaxLength=0")(assertNoDiff(
    obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(0)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  private def withMaxLengthProperty[A](value: String)(body: => A): A = {
    val previous = System.setProperty("munit.diff.obtainedMaxLength", value)
    try body
    finally
      if (previous eq null) System.clearProperty("munit.diff.obtainedMaxLength")
      else System.setProperty("munit.diff.obtainedMaxLength", previous)
  }

  test("obtainedMaxLength property: 1000")(assertNoDiff(
    withMaxLengthProperty("1000")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 1000, option: 999")(assertNoDiff(
    withMaxLengthProperty("1000")(
      obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(999))
    ),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: not a number")(assertNoDiff(
    withMaxLengthProperty("x")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 0")(assertNoDiff(
    withMaxLengthProperty("0")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: -5")(assertNoDiff(
    withMaxLengthProperty("-5")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 998")(assertNoDiff(
    withMaxLengthProperty("998")(obtainedReport(obtained999)),
    s"""|title
        |=> Obtained not shown: 999 characters, limit 998 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 997}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 1000 with spaces")(assertNoDiff(
    withMaxLengthProperty(" 1000 ")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: too large")(assertNoDiff(
    withMaxLengthProperty("99999999999")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: empty")(assertNoDiff(
    withMaxLengthProperty("")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: blank")(assertNoDiff(
    withMaxLengthProperty("   ")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 1000 with tab and newline")(assertNoDiff(
    withMaxLengthProperty("\t1000\n")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 1000 with inner space")(assertNoDiff(
    withMaxLengthProperty("1 000")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: too small")(assertNoDiff(
    withMaxLengthProperty("-99999999999")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: fraction")(assertNoDiff(
    withMaxLengthProperty("1.5")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: hexadecimal")(assertNoDiff(
    withMaxLengthProperty("0x10")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 1000 with plus")(assertNoDiff(
    withMaxLengthProperty("+1000")(obtainedReport(obtained1000)),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 1000, option: -5")(assertNoDiff(
    withMaxLengthProperty("1000")(
      obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(-5))
    ),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: 5, option: 0")(assertNoDiff(
    withMaxLengthProperty("5")(
      obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(0))
    ),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: x, option: -1")(assertNoDiff(
    withMaxLengthProperty("x")(
      obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(-1))
    ),
    s"""|title
        |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: x, option: 1000")(assertNoDiff(
    withMaxLengthProperty("x")(
      obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(1000))
    ),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("obtainedMaxLength property: x, option: 0")(assertNoDiff(
    withMaxLengthProperty("x")(
      obtainedReport(obtained1000, DiffOptions.withObtainedMaxLength(0))
    ),
    s"""|title
        |=> Obtained
        |$obtained1000
        |=> Diff (- expected, + obtained)
        |+${"a" * 998}
        | b""".stripMargin,
  ))

  test("DiffOptions: obtainedMaxLength=998, ansi=true") {
    import AnsiColors.{Bold, LightGreen, LightRed, c}
    val options = DiffOptions.withObtainedMaxLength(998).withForceAnsi(Some(true))
    assertEquals(
      new Diff(obtained999, "b", options).createReport("title"),
      s"""|title
          |${c("=> Obtained", Bold)} not shown: 999 characters, limit 998 (munit.diff.obtainedMaxLength, see docs)
          |${c("=> Diff", Bold)} (${c("- expected", LightRed)}, ${c("+ obtained", LightGreen)})
          |${c("+" + "a" * 997, LightGreen)}
          | b""".stripMargin,
    )
  }

  test("withMaxLengthProperty: property set before") {
    System.setProperty("munit.diff.obtainedMaxLength", "5")
    try {
      withMaxLengthProperty("1000")(())
      assertEquals(sys.props.get("munit.diff.obtainedMaxLength"), Some("5"))
    } finally System.clearProperty("munit.diff.obtainedMaxLength")
  }

  test("createReport: obtained of 999 characters, strip margin") {
    val q = "\"\"\""
    assertNoDiff(
      obtainedReport(obtained999, DiffOptions.withObtainedAsStripMargin(true)),
      s"""|title
          |=> Obtained
          |    $q|${"a" * 997}
          |       |b
          |       |$q.stripMargin
          |=> Diff (- expected, + obtained)
          |+${"a" * 997}
          | b""".stripMargin,
    )
  }

  test("createReport: obtained of 999 characters in color")(assertNoDiff(
    obtainedReport(AnsiColors.c("a" * 997, AnsiColors.LightRed) + "\nb"),
    s"""|title
        |=> Obtained
        |$obtained999
        |=> Diff (- expected, + obtained)
        |+${"a" * 997}
        | b""".stripMargin,
  ))

  test("assertNoDiff: obtained of 1000 characters") {
    implicit val options: DiffOptions = DiffOptions.withForceAnsi(Some(false))
    implicit val loc: Location = munit.Location.empty
    assertNoDiff(
      intercept[ComparisonFailException](assertNoDiff(obtained1000, "b"))
        .message,
      s"""|diff assertion failed
          |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
          |=> Diff (- expected, + obtained)
          |+${"a" * 998}
          | b""".stripMargin,
    )
  }

  test("assertEquals: obtained of 1000 characters") {
    implicit val options: DiffOptions = DiffOptions.withForceAnsi(Some(false))
    implicit val loc: Location = munit.Location.empty
    assertNoDiff(
      intercept[ComparisonFailException](assertEquals(obtained1000, "b"))
        .message,
      s"""|values are not the same
          |=> Obtained not shown: 1000 characters, limit 999 (munit.diff.obtainedMaxLength, see docs)
          |=> Diff (- expected, + obtained)
          |+${"a" * 998}
          | b""".stripMargin,
    )
  }

}
