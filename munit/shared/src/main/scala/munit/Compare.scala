package munit

import munit.internal.MacroCompat
import munit.internal.difflib.Diffs
import munit.internal.difflib.ComparisonFailExceptionHandler

/**
 * A type-class that is used to compare values in MUnit assertions.
 *
 * By default, uses == and allows comparison between any two types, even if the
 * types are unrelated. Optionally, enable strict equality by adding the
 * compiler option "-Xmacro-settings:munit.strictEquality" in Scala 2 or
 * the language option "-language:strictEquality" in Scala 3.
 */
abstract class Compare[A, B] {

  /**
   * Returns true if the values are equal according to the rules of this `Compare[A, B]` instance.
   *
   * The default implementation of this method uses `==`.
   */
  def isEqual(obtained: A, expected: B): Boolean

  /**
   * Throws an exception to fail this assertion when two values are not equal.
   *
   * Override this method to customize the error message. For example, it may
   * be helpful to generate an image/HTML file if you're comparing visual
   * values. Anything is possible, use your imagination!
   *
   * @return should ideally throw a org.junit.ComparisonFailException in order
   *         to support the IntelliJ diff viewer.
   */
  def failEqualsComparison(
      obtained: A,
      expected: B,
      title: Any,
      loc: Location,
      assertions: Assertions
  ): Nothing = {
    val diffHandler = new ComparisonFailExceptionHandler {
      override def handle(
          message: String,
          _obtained: String,
          _expected: String,
          loc: Location
      ): Nothing =
        assertions.failComparison(
          message,
          obtained,
          expected
        )(loc)
    }
    // Attempt 1: custom pretty-printer that produces multiline output, which is
    // optimized for line-by-line diffing.
    Diffs.assertNoDiff(
      assertions.munitPrint(obtained),
      assertions.munitPrint(expected),
      diffHandler,
      title = assertions.munitPrint(title),
      printObtainedAsStripMargin = false
    )(loc)

    // Attempt 2: try with `.toString` in case `munitPrint()` produces identical
    // formatting for both values.
    Diffs.assertNoDiff(
      obtained.toString(),
      expected.toString(),
      diffHandler,
      title = assertions.munitPrint(title),
      printObtainedAsStripMargin = false
    )(loc)

    // Attempt 3: string comparison is not working, unconditionally fail the test.
    assertions.failComparison(
      s"values are not equal even if they have the same `toString()`: $obtained",
      obtained,
      expected
    )(loc)
  }

}

object Compare extends MacroCompat.CompareMacro {
  private val anyEquality: Compare[Any, Any] = new Compare[Any, Any] {
    def isEqual(a: Any, b: Any): Boolean = a == b
  }
  def defaultCompare[A, B]: Compare[A, B] =
    anyEquality.asInstanceOf[Compare[A, B]]
}
