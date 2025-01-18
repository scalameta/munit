package munit

import munit.internal.console.Lines
import munit.internal.io.PlatformIO.Paths

import org.junit.ComparisonFailure

class ComparisonFailExceptionSuite extends BaseSuite {
  override val munitLines: Lines = new Lines {
    override def formatPath(location: Location): String = Paths
      .get(location.path).getFileName().toString()
  }
  test("comparison-failure") {
    val e = intercept[ComparisonFailException](
      assertEquals[Any, Any](List("1", "2", "3"), List(1, 2))
    )
    assert(clue(e).isInstanceOf[ComparisonFailure])
    assert(clue(e).isInstanceOf[Serializable])
    // NOTE: assert that we use the `toString` of values in the
    // `org.junit.ComparisionFailure` exception. The stdout message in the
    // console still uses `munitPrint()`, which would have displayed `List("1",
    // "2", "3")` instead of `List(1, 2, 3)`.
    assertNoDiff(e.getActual(), "List(1, 2, 3)")
    assertNoDiff(e.getExpected(), "List(1, 2)")
    assertEquals(e.expected, List(1, 2))
    assertEquals(e.obtained, List("1", "2", "3"))
    assertNoDiff(
      e.getMessage(),
      """|ComparisonFailExceptionSuite.scala:15
         |14:    val e = intercept[ComparisonFailException](
         |15:      assertEquals[Any, Any](List("1", "2", "3"), List(1, 2))
         |16:    )
         |values are not the same
         |=> Obtained
         |List(
         |  "1",
         |  "2",
         |  "3"
         |)
         |=> Diff (- expected, + obtained)
         | List(
         |-  1,
         |-  2
         |+  "1",
         |+  "2",
         |+  "3"
         | )
         |""".stripMargin,
    )
  }

  test("assert-no-diff-obtained-empty") {
    val e = intercept[ComparisonFailException](assertNoDiff("", "Lorem ipsum"))
    assertNoDiff(
      e.getMessage(),
      """|ComparisonFailExceptionSuite.scala:53
         |52:  test("assert-no-diff-obtained-empty") {
         |53:    val e = intercept[ComparisonFailException](assertNoDiff("", "Lorem ipsum"))
         |54:    assertNoDiff(
         |Obtained empty output!
         |=> Expected:
         |Lorem ipsum
         |""".stripMargin,
    )
  }

}
