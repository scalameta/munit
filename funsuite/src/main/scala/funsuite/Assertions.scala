package funsuite

import funsuite.internal.StackMarker
import fansi.Str
import funsuite.internal.Lines
import funsuite.internal.Diffs
import org.junit.AssumptionViolatedException

object Assertions extends Assertions
trait Assertions {

  private[funsuite] val lines = new Lines

  def assert(cond: Boolean)(implicit loc: Location): Unit = {
    StackMarker.dropInside {
      assert(cond, "assertion failed")
    }
  }

  def assert(
      cond: Boolean,
      details: => Any
  )(implicit loc: Location): Unit = {
    StackMarker.dropInside {
      if (!cond) {
        fail(locatedDetails(loc, details).render)
      }
    }
  }

  def assume(
      cond: Boolean,
      details: => Any
  )(implicit loc: Location): Unit = {
    StackMarker.dropInside {
      if (!cond) {
        throw new AssumptionViolatedException(detailsFromAny(details))
      }
    }
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      details: => Any = "diff assertion failed"
  )(implicit loc: Location): Unit = {
    StackMarker.dropInside {
      Diffs.assertNoDiff(
        obtained,
        expected,
        locatedDetails(loc, details).render,
        printObtainedAsStripMargin = false
      )
    }
  }

  def assertNotEqual[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackMarker.dropInside {
      if (obtained == expected) {
        fail(detailsFromAny(details))
      }
    }
  }

  def assertEqual[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are not the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackMarker.dropInside {
      if (obtained != expected) {
        assertNoDiff(
          detailsFromAny(obtained),
          detailsFromAny(expected),
          details
        )
      }
    }
  }

  def fail(message: String)(implicit loc: Location): Nothing =
    throw new FailException(message, loc)

  def locatedDetails(loc: Location, details: => Any): Str =
    lines.formatLine(loc, detailsFromAny(details))

  private def detailsFromAny(details: => Any): String = {
    details match {
      case null            => "null"
      case message: String => message
      case value           => prettyPrint(value)
    }
  }

  private def prettyPrint(value: Any): String = {
    Printers.print(value)
  }
}
