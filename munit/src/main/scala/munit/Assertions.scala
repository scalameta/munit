package munit

import org.junit.AssumptionViolatedException

object Assertions extends Assertions
trait Assertions {

  val munitLines = new Lines

  def assert(cond: Boolean)(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      assert(cond, "assertion failed")
    }
  }

  def assert(
      cond: Boolean,
      details: => Any
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        fail(munitDetails(details))
      }
    }
  }

  def assume(
      cond: Boolean,
      details: => Any
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        throw new AssumptionViolatedException(munitDetails(details))
      }
    }
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      details: => Any = "diff assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      Diffs.assertNoDiff(
        obtained,
        expected,
        munitDetails(details),
        printObtainedAsStripMargin = true
      )
    }
  }

  def assertNotEqual[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained == expected) {
        fail(munitDetails(details))
      }
    }
  }

  def assertEqual[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are not the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained != expected) {
        Diffs.assertNoDiff(
          munitDetails(obtained),
          munitDetails(expected),
          munitDetails(details),
          printObtainedAsStripMargin = false
        )
      }
    }
  }
  def fail(message: String)(implicit loc: Location): Nothing = {
    throw new FailException(munitLines.formatLine(loc, message), loc)
  }

  def munitDetails(details: => Any): String = {
    details match {
      case null            => "null"
      case message: String => message
      case value           => munitPrint(value)
    }
  }

  def munitPrint(value: Any): String = {
    Printers.print(value)
  }
}
