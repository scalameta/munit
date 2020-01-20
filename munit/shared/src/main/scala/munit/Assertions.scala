package munit

import munit.internal.console.{Lines, Printers, StackTraces}
import munit.internal.difflib.Diffs

import scala.reflect.ClassTag
import scala.util.control.NonFatal

// FIXME(gabro): Constructing a `org.junit.AssumptionViolatedException` causes Dotty to crash
// so we use our own Exception class to work around it.
// See https://github.com/lampepfl/dotty/issues/7990
class DottyBugAssumptionViolatedException(message: String)
    extends RuntimeException

object Assertions extends Assertions
trait Assertions {

  val munitLines = new Lines

  def assert(
      cond: Boolean,
      details: => Any = "assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        fail(munitPrint(details))
      }
    }
  }

  def assume(
      cond: Boolean,
      details: => Any = "assumption failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        throw new DottyBugAssumptionViolatedException(munitPrint(details))
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
        munitPrint(details),
        printObtainedAsStripMargin = true
      )
    }
  }

  def assertNotEquals[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained == expected) {
        fail(munitPrint(details))
      }
    }
  }

  def assertEquals[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are not the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained != expected) {
        Diffs.assertNoDiff(
          munitPrint(obtained),
          munitPrint(expected),
          munitPrint(details),
          printObtainedAsStripMargin = false
        )
      }
    }
  }

  def intercept[T <: Throwable](
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    try {
      body
      fail(
        s"expected exception of type '${T.runtimeClass.getName()}' but body evaluated successfully"
      )
    } catch {
      case e: FailException => throw e
      case NonFatal(e) =>
        if (T.runtimeClass.isAssignableFrom(e.getClass())) {
          e.asInstanceOf[T]
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

  def fail(message: String, cause: Throwable)(
      implicit loc: Location
  ): Nothing = {
    throw new FailException(
      munitLines.formatLine(loc, message),
      cause,
      isStackTracesEnabled = true,
      location = loc
    )
  }
  def fail(message: String)(implicit loc: Location): Nothing = {
    throw new FailException(munitLines.formatLine(loc, message), loc)
  }

  def munitPrint(details: => Any): String = {
    details match {
      case message: String => message
      case value           => Printers.print(value)
    }
  }

}
