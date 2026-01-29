package munit

import munit.diff.{DiffOptions, EmptyPrinter, Printer}
import munit.internal.MacroCompat
import munit.internal.console.{Lines, Printers, StackTraces}

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.control.NonFatal

import org.junit.AssumptionViolatedException

object Assertions extends Assertions {
  def munitPrint(clue: => Any, printer: Printer): String = clue match {
    case message: String => message
    case value => Printers.print(value, printer)
  }
}

trait Assertions extends MacroCompat.CompileErrorMacro {

  val munitLines = new Lines

  def munitAnsiColors: Boolean = true
  private def useAnsiColors(implicit diffOptions: DiffOptions): Boolean =
    diffOptions.ansi(munitAnsiColors)

  /**
   * Asserts that the given condition is true.
   */
  def assert(cond: => Boolean, clue: => Any = "assertion failed")(implicit
      loc: Location
  ): Unit = StackTraces.dropInside {
    val (isTrue, clues) = munitCaptureClues(cond)
    if (!isTrue) fail(munitPrint(clue), clues)
  }

  /**
   * Asserts that the given partial function is defined for the given value and that applying the partial function
   * to the value returns true.
   */
  def assertMatches[A](value: A, clue: => String = "assertion failed")(
      predicate: PartialFunction[A, Boolean]
  )(implicit loc: Location): Unit = StackTraces.dropInside(
    if (predicate.isDefinedAt(value)) {
      val (matches, clues) = munitCaptureClues(predicate(value))
      if (!matches) fail(
        s"${munitPrint(clue)}: predicate returned false for value: $value",
        clues,
      )
    } else fail(s"${munitPrint(clue)}: predicate not defined for value: $value")
  )

  /**
   * Aborts the test if the given condition is false. In contrast with `assert`, a failed assumption does not fail the test,
   * but rather marks it as skipped.
   */
  def assume(cond: Boolean, clue: => Any = "assumption failed")(implicit
      loc: Location
  ): Unit = StackTraces
    .dropInside(if (!cond) throw new AssumptionViolatedException(munitPrint(clue)))

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  protected def assertNoDiff(
      obtained: String,
      expected: String,
      clue: => Any,
      loc: Location,
  ): Unit = {
    implicit val _loc: Location = loc
    assertNoDiff(obtained, expected, clue)
  }

  /**
   * Asserts that two strings are equal, ignoring non-visible differences such as whitespace. Useful for comparing
   * multiline strings.
   */
  def assertNoDiff(
      obtained: String,
      expected: String,
      clue: => Any = "diff assertion failed",
  )(implicit loc: Location, diffOptions: DiffOptions): Unit = StackTraces
    .dropInside(Diffs.assertNoDiff(
      obtained,
      expected,
      exceptionHandlerFromAssertions(this, Clues.empty),
      munitPrint(clue),
    ))

  /**
   * Asserts that two elements are not equal according to the `Compare[A, B]` type-class.
   *
   * By default, uses `==` to compare values.
   */
  def assertNotEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are the same",
  )(implicit loc: Location, compare: Compare[A, B]): Unit = StackTraces.dropInside(
    if (compare.isEqual(obtained, expected)) failComparison(
      s"${munitPrint(clue)}, expected 2 different values: $expected is equal to $obtained",
      obtained,
      expected,
    )
  )

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  protected def assertEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any,
      loc: Location,
      compare: Compare[A, B],
  ): Unit = {
    implicit val _loc: Location = loc
    implicit val _cmp: Compare[A, B] = compare
    assertEquals(obtained, expected, clue)
  }

  /**
   * Asserts that two elements are equal according to the `Compare[A, B]` type-class.
   *
   * By default, uses `==` to compare values.
   */
  def assertEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same",
  )(implicit
      loc: Location,
      compare: Compare[A, B],
      diffOptions: DiffOptions,
  ): Unit = StackTraces.dropInside {
    if (!compare.isEqual(obtained, expected)) {
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
            expected,
          )
        case _ =>
      }
      compare.failEqualsComparison(obtained, expected, clue, this)
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
      clue: => Any = "values are not the same",
  )(implicit loc: Location): Unit = StackTraces.dropInside {
    val exactlyTheSame = java.lang.Double.compare(expected, obtained) == 0
    val almostTheSame = Math.abs(expected - obtained) <= delta
    if (!exactlyTheSame && !almostTheSame) failComparison(
      s"${munitPrint(clue)} expected: $expected but was: $obtained",
      obtained,
      expected,
    )
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
      clue: => Any = "values are not the same",
  )(implicit loc: Location): Unit = StackTraces.dropInside {
    val exactlyTheSame = java.lang.Float.compare(expected, obtained) == 0
    val almostTheSame = Math.abs(expected - obtained) <= delta
    if (!exactlyTheSame && !almostTheSame) failComparison(
      s"${munitPrint(clue)} expected: $expected but was: $obtained",
      obtained,
      expected,
    )
  }

  /**
   * Evalutes the given expression and asserts that an exception of type T is thrown.
   */
  def intercept[T <: Throwable](
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = runIntercept(None, body)

  /**
   * Evalutes the given expression and asserts that an exception of type T with the expected message is thrown.
   */
  def interceptMessage[T <: Throwable](
      expectedExceptionMessage: String
  )(body: => Any)(implicit T: ClassTag[T], loc: Location): T =
    runIntercept(Some(expectedExceptionMessage), body)

  private def runIntercept[T <: Throwable](
      expectedExceptionMessage: Option[String],
      body: => Any,
  )(implicit T: ClassTag[T], loc: Location): T = {
    val expectedExceptionMsg =
      s"expected exception of type '${T.runtimeClass.getName()}' but body evaluated successfully"
    try {
      body
      fail(expectedExceptionMsg)
    } catch {
      case e: FailExceptionLike[_]
          if !T.runtimeClass.isAssignableFrom(e.getClass()) => throw e
      case e: FailExceptionLike[_]
          if e.getMessage.contains(expectedExceptionMsg) => throw e
      case NonFatal(e) =>
        if (T.runtimeClass.isAssignableFrom(e.getClass()))
          if (
            expectedExceptionMessage.isEmpty ||
            e.getMessage == expectedExceptionMessage.get
          ) e.asInstanceOf[T]
          else {
            val obtained = e.getClass().getName()
            throw new FailException(
              s"intercept failed, exception '$obtained' had message '${e.getMessage}', which was different from expected message '${expectedExceptionMessage.get}'",
              cause = e,
              isStackTracesEnabled = false,
              location = loc,
            )
          }
        else {
          val obtained = e.getClass().getName()
          val expected = T.runtimeClass.getName()
          throw new FailException(
            s"intercept failed, exception '$obtained' is not a subtype of '$expected",
            cause = e,
            isStackTracesEnabled = false,
            location = loc,
          )
        }
    }
  }

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  protected def fail(
      message: String,
      cause: Throwable,
      loc: Location,
  ): Nothing = {
    implicit val _loc: Location = loc
    fail(message, cause)
  }

  /**
   * Unconditionally fails this test with the given message and exception marked as the cause.
   */
  def fail(message: String, cause: Throwable)(implicit
      loc: Location,
      diffOptions: DiffOptions,
  ): Nothing = throw new FailException(
    munitLines.formatLine(loc, message, ansi = useAnsiColors),
    cause,
    isStackTracesEnabled = true,
    location = loc,
  )

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  protected def fail(message: String, clues: Clues, loc: Location): Nothing = {
    implicit val _loc: Location = loc
    fail(message, clues)
  }

  /**
   * Unconditionally fails this test with the given message and optional clues.
   */
  def fail(message: String, clues: Clues = new Clues(Nil))(implicit
      loc: Location,
      diffOptions: DiffOptions,
  ): Nothing = throw new FailException(
    munitLines.formatLine(loc, message, clues, ansi = useAnsiColors),
    loc,
  )

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  protected def failComparison(
      message: String,
      obtained: Any,
      expected: Any,
      clues: Clues,
      loc: Location,
  ): Nothing = {
    implicit val _loc: Location = loc
    failComparison(message, obtained, expected, clues)
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
      clues: Clues = new Clues(Nil),
  )(implicit loc: Location, diffOptions: DiffOptions): Nothing =
    throw new ComparisonFailException(
      munitLines.formatLine(loc, message, clues, ansi = useAnsiColors),
      obtained,
      expected,
      loc,
      isStackTracesEnabled = true,
    )

  // for MIMA compatibility
  @deprecated("Use version with implicit DiffOptions", "1.0.4")
  protected def failSuite(
      message: String,
      clues: Clues,
      loc: Location,
  ): Nothing = {
    implicit val _loc: Location = loc
    failSuite(message, clues)
  }

  /**
   * Unconditionally fail this test case and cancel all the subsequent tests in this suite.
   */
  def failSuite(message: String, clues: Clues = new Clues(Nil))(implicit
      loc: Location,
      diffOptions: DiffOptions,
  ): Nothing = throw new FailSuiteException(
    munitLines.formatLine(loc, message, clues, ansi = useAnsiColors),
    loc,
  )

  private def exceptionHandlerFromAssertions(
      assertions: Assertions,
      clues: => Clues,
  )(implicit diffOptions: DiffOptions): ComparisonFailExceptionHandler = {
    (message: String, obtained: String, expected: String, location: Location) =>
      implicit val loc = location
      assertions.failComparison(message, obtained, expected, clues)
  }

  private val munitCapturedClues: mutable.ListBuffer[Clue[_]] =
    mutable.ListBuffer.empty
  def munitCaptureClues[T](thunk: => T): (T, Clues) = synchronized {
    munitCapturedClues.clear()
    val result = thunk
    (result, new Clues(munitCapturedClues.toList))
  }

  def clue[T](c: Clue[T]): T = synchronized {
    munitCapturedClues += c
    c.value
  }
  def clues(clue: Clue[_]*): Clues = new Clues(clue.toList)

  def printer: Printer = EmptyPrinter

  def munitPrint(clue: => Any): String = Assertions.munitPrint(clue, printer)

}
