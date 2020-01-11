package funsuite

import org.junit.AssumptionViolatedException

object Assertions extends Assertions
trait Assertions {

  val funsuiteLines = new Lines

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
        fail(funsuiteLines.formatLine(loc, funsuiteDetails(details)))
      }
    }
  }

  def assume(
      cond: Boolean,
      details: => Any
  )(implicit loc: Location): Unit = {
    StackMarker.dropInside {
      if (!cond) {
        throw new AssumptionViolatedException(funsuiteDetails(details))
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
        funsuiteLines.formatLine(loc, funsuiteDetails(details)),
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
        fail(funsuiteDetails(details))
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
          funsuiteDetails(obtained),
          funsuiteDetails(expected),
          details
        )
      }
    }
  }

  def fail(message: String)(implicit loc: Location): Nothing = {
    throw new FailException(message, loc)
  }

  def funsuiteDetails(details: => Any): String = {
    details match {
      case null            => "null"
      case message: String => message
      case value           => funsuitePrint(value)
    }
  }

  def funsuitePrint(value: Any): String = {
    Printers.print(value)
  }
}
