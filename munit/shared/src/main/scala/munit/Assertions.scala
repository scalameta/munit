package munit

import munit.internal.console.{Lines, Printers, StackTraces}
import munit.internal.difflib.Diffs

import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.collection.mutable
import munit.internal.console.AnsiColors
import org.junit.AssumptionViolatedException

object Assertions extends Assertions
trait Assertions {

  val munitLines = new Lines

  def munitAnsiColors: Boolean = true

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
        message => fail(message),
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
        fail(munitPrint(clue))
      }
    }
  }

  def assertEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained != expected) {
        Diffs.assertNoDiff(
          munitPrint(obtained),
          munitPrint(expected),
          message => fail(message),
          munitPrint(clue),
          printObtainedAsStripMargin = false
        )
      }
    }
  }

  def intercept[T <: Throwable](
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    _intercept(None, body)
  }

  def interceptMessage[T <: Throwable](msg: String)(
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    _intercept(Some(msg), body)
  }

  private def _intercept[T <: Throwable](
      msg: Option[String],
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    try {
      body
      fail(
        s"expected exception of type '${T.runtimeClass.getName()}' but body evaluated successfully"
      )
    } catch {
      case e: FailException if !T.runtimeClass.isAssignableFrom(e.getClass()) =>
        throw e
      case NonFatal(e) =>
        if (T.runtimeClass.isAssignableFrom(e.getClass())) {
          if (msg.isEmpty || e.getMessage == msg.get)
            e.asInstanceOf[T]
          else {
            val obtained = e.getClass().getName()
            throw new FailException(
              s"intercept failed, exception '$obtained' had message '${e.getMessage}', which was different from expected message '${msg.get}'",
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

  def fail(message: String, cause: Throwable)(
      implicit loc: Location
  ): Nothing = {
    throw new FailException(
      munitFilterAnsi(munitLines.formatLine(loc, message)),
      cause,
      isStackTracesEnabled = true,
      location = loc
    )
  }
  def fail(
      message: String,
      clues: Clues = new Clues(Nil)
  )(implicit loc: Location): Nothing = {
    throw new FailException(
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
