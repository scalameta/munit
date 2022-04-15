package munit

import munit.internal.console.{Lines, Printers, StackTraces}
import munit.internal.difflib.ComparisonFailExceptionHandler
import munit.internal.difflib.Diffs

import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.collection.mutable
import munit.internal.console.AnsiColors
import org.junit.AssumptionViolatedException
import munit.internal.MacroCompat

object Assertions extends Assertions
trait Assertions extends MacroCompat.CompileErrorMacro {

  val munitLines = new Lines

  def munitAnsiColors: Boolean = true

  private def munitComparisonHandler(
      actualObtained: Any,
      actualExpected: Any
  ): ComparisonFailExceptionHandler =
    new ComparisonFailExceptionHandler {
      override def handle(
          message: String,
          unusedObtained: String,
          unusedExpected: String,
          loc: Location
      ): Nothing = failComparison(message, actualObtained, actualExpected)(loc)
    }

  private def munitFilterAnsi(message: String): String =
    if (munitAnsiColors) message
    else AnsiColors.filterAnsi(message)

  def assert(
      cond: => Boolean,
      clue: => Any = "assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      val (isTrue, clues) = munitCaptureClues(cond)
      if (!isTrue) {
        fail(munitPrint(clue), clues)
      }
    }
  }

  def assume(
      cond: Boolean,
      clue: => Any = "assumption failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        throw new AssumptionViolatedException(munitPrint(clue))
      }
    }
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      clue: => Any = "diff assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      Diffs.assertNoDiff(
        obtained,
        expected,
        munitComparisonHandler(obtained, expected),
        munitPrint(clue),
        printObtainedAsStripMargin = true
      )
    }
  }

  def assertNotEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained == expected) {
        failComparison(
          s"${munitPrint(clue)} expected same: $expected was not: $obtained",
          obtained,
          expected
        )
      }
    }
  }

  /**
   * Asserts that two elements are equal using `==` equality.
   *
   * The "expected" value (second argument) must have the same type or be a
   * subtype of the "obtained" value (first argument). For example:
   * {{{
   *   assertEquals(Option(1), Some(1)) // OK
   *   assertEquals(Some(1), Option(1)) // Error: Option[Int] is not a subtype of Some[Int]
   * }}}
   *
   * Use `assertEquals[Any, Any](a, b)` as an escape hatch to compare two
   * values of different types. For example:
   * {{{
   *   val a: Either[List[String], Int] = Right(42)
   *   val b: Either[String, Int]       = Right(42)
   *   assertEquals[Any, Any](a, b) // OK
   *   assertEquals(a, b) // Error: Either[String, Int] is not a subtype of Either[List[String], Int]
   * }}}
   */
  def assertEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same"
  )(implicit loc: Location, ev: B <:< A): Unit = {
    StackTraces.dropInside {
      if (obtained != expected) {

        (obtained, expected) match {
          case (a: Array[_], b: Array[_]) if a.sameElements(b) =>
            // Special-case error message when comparing arrays. See
            // https://github.com/scalameta/munit/pull/393 and
            // https://github.com/scalameta/munit/issues/339 for a related
            // discussion on how MUnit should handle array comparisons.  Other
            // testing frameworks have special cases for arrays so the
            // comparison succeeds as long as `sameElements()` returns true.
            // MUnit chooses instead to fail the test with a custom error
            // message because arrays have reference equality, for better or
            // worse, and we should not hide that fact from our users.
            failComparison(
              "arrays have the same elements but different reference equality. " +
                "Convert the arrays to a non-Array collection if you intend to assert the two arrays have the same elements. " +
                "For example, `assertEquals(a.toSeq, b.toSeq)",
              obtained,
              expected
            )
          case _ =>
        }

        Diffs.assertNoDiff(
          munitPrint(obtained),
          munitPrint(expected),
          munitComparisonHandler(obtained, expected),
          munitPrint(clue),
          printObtainedAsStripMargin = false
        )
        // try with `.toString` in case `munitPrint()` produces identical formatting for both values.
        Diffs.assertNoDiff(
          obtained.toString(),
          expected.toString(),
          munitComparisonHandler(obtained, expected),
          munitPrint(clue),
          printObtainedAsStripMargin = false
        )
        if (obtained.toString() == expected.toString())
          failComparison(
            s"values are not equal even if they have the same `toString()`: $obtained",
            obtained,
            expected
          )
        else
          failComparison(
            s"values are not equal, even if their text representation only differs in leading/trailing whitespace and ANSI escape characters: $obtained",
            obtained,
            expected
          )
      }
    }
  }

  /**
   * Asserts that two doubles are equal to within a positive delta.
   * If the expected value is infinity then the delta value is ignored.
   * NaNs are considered equal: assertEquals(Double.NaN, Double.NaN, *) passes.
   */
  def assertEqualsDouble(
      obtained: Double,
      expected: Double,
      delta: Double,
      clue: => Any = "values are not the same"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      val exactlyTheSame = java.lang.Double.compare(expected, obtained) == 0
      val almostTheSame = Math.abs(expected - obtained) <= delta
      if (!exactlyTheSame && !almostTheSame) {
        failComparison(
          s"${munitPrint(clue)} expected: $expected but was: $obtained",
          obtained,
          expected
        )
      }
    }
  }

  /**
   * Asserts that two floats are equal to within a positive delta.
   * If the expected value is infinity then the delta value is ignored.
   * NaNs are considered equal: assertEquals(Float.NaN, Float.NaN, *) passes.
   */
  def assertEqualsFloat(
      obtained: Float,
      expected: Float,
      delta: Float,
      clue: => Any = "values are not the same"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      val exactlyTheSame = java.lang.Float.compare(expected, obtained) == 0
      val almostTheSame = Math.abs(expected - obtained) <= delta
      if (!exactlyTheSame && !almostTheSame) {
        failComparison(
          s"${munitPrint(clue)} expected: $expected but was: $obtained",
          obtained,
          expected
        )
      }
    }
  }

  /**
   * Evalutes the given expression and asserts that an exception of type T is thrown.
   */
  def intercept[T <: Throwable](
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    runIntercept(None, body)
  }

  /**
   * Evalutes the given expression and asserts that an exception of type T with the expected message is thrown.
   */
  def interceptMessage[T <: Throwable](expectedExceptionMessage: String)(
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    runIntercept(Some(expectedExceptionMessage), body)
  }

  private def runIntercept[T <: Throwable](
      expectedExceptionMessage: Option[String],
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    try {
      body
      fail(
        s"expected exception of type '${T.runtimeClass.getName()}' but body evaluated successfully"
      )
    } catch {
      case e: FailExceptionLike[_]
          if !T.runtimeClass.isAssignableFrom(e.getClass()) =>
        throw e
      case NonFatal(e) =>
        if (T.runtimeClass.isAssignableFrom(e.getClass())) {
          if (
            expectedExceptionMessage.isEmpty || e.getMessage == expectedExceptionMessage.get
          )
            e.asInstanceOf[T]
          else {
            val obtained = e.getClass().getName()
            throw new FailException(
              s"intercept failed, exception '$obtained' had message '${e.getMessage}', which was different from expected message '${expectedExceptionMessage.get}'",
              cause = e,
              isStackTracesEnabled = false,
              location = loc
            )
          }
        } else {
          val obtained = e.getClass().getName()
          val expected = T.runtimeClass.getName()
          throw new FailException(
            s"intercept failed, exception '$obtained' is not a subtype of '$expected",
            cause = e,
            isStackTracesEnabled = false,
            location = loc
          )
        }
    }
  }

  /**
   * Unconditionally fails this test with the given message and exception marked as the cause.
   */
  def fail(message: String, cause: Throwable)(implicit
      loc: Location
  ): Nothing = {
    throw new FailException(
      munitFilterAnsi(munitLines.formatLine(loc, message)),
      cause,
      isStackTracesEnabled = true,
      location = loc
    )
  }

  /**
   * Unconditionally fails this test with the given message and optional clues.
   */
  def fail(
      message: String,
      clues: Clues = new Clues(Nil)
  )(implicit loc: Location): Nothing = {
    throw new FailException(
      munitFilterAnsi(munitLines.formatLine(loc, message, clues)),
      loc
    )
  }

  /**
   * Unconditionally fails this test due to result of comparing two values.
   *
   * The only reason to use this method instead of `fail()` is if you want to
   * allow comparing the two different values in the the IntelliJ GUI diff
   * viewer.
   */
  def failComparison(
      message: String,
      obtained: Any,
      expected: Any,
      clues: Clues = new Clues(Nil)
  )(implicit loc: Location): Nothing = {
    throw new ComparisonFailException(
      munitFilterAnsi(munitLines.formatLine(loc, message, clues)),
      obtained,
      expected,
      loc,
      isStackTracesEnabled = false
    )
  }

  /**
   * Unconditionally fail this test case and cancel all the subsequent tests in this suite.
   */
  def failSuite(
      message: String,
      clues: Clues = new Clues(Nil)
  )(implicit loc: Location): Nothing = {
    throw new FailSuiteException(
      munitFilterAnsi(munitLines.formatLine(loc, message, clues)),
      loc
    )
  }

  private val munitCapturedClues: mutable.ListBuffer[Clue[_]] =
    mutable.ListBuffer.empty
  def munitCaptureClues[T](thunk: => T): (T, Clues) =
    synchronized {
      munitCapturedClues.clear()
      val result = thunk
      (result, new Clues(munitCapturedClues.toList))
    }

  def clue[T](c: Clue[T]): T = synchronized {
    munitCapturedClues += c
    c.value
  }
  def clues(clue: Clue[_]*): Clues = new Clues(clue.toList)

  def munitPrint(clue: => Any): String = {
    clue match {
      case message: String => message
      case value           => Printers.print(value)
    }
  }

}
