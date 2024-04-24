// Adaptation of https://github.com/lihaoyi/PPrint/blob/e6a918c259ed7ae1998bbf58c360334a3f0157ca/pprint/src/pprint/Walker.scala
package munit.internal.console

import munit.Location
import munit.diff.Printer
import munit.diff.EmptyPrinter
import munit.diff.console.{Printers => DiffPrinters}
import munit.Clues
import munit.Printable
import munit.internal.Compat

object Printers {

  import DiffPrinters._

  def log(any: Any, printer: Printer = EmptyPrinter)(implicit
      loc: Location
  ): Unit = {
    println(loc.toString)
    println(print(any, printer))
  }

  /** Pretty-prints the value in a format that's optimized for producing diffs */
  def print(any: Any, printer: Printer = EmptyPrinter): String = {
    var height = printer.height
    val out = new StringBuilder()
    val indentStep = 2
    def loop(a: Any, indent: Int): Unit = {
      height -= 1
      if (height < 0) {
        out.append("...")
        return
      }
      val nextIndent = indent + indentStep
      val isDone = printer.print(a, out, indent)
      if (!isDone) {
        a match {
          case null         => out.append("null")
          case x: Printable => x.print(out, indent)
          case x: Char =>
            out.append('\'')
            if (x == '\'') out.append("\\'")
            else printChar(x, out)
            out.append('\'')
          case x: Byte   => out.append(x.toString())
          case x: Short  => out.append(x.toString())
          case x: Int    => out.append(x.toString())
          case x: Long   => out.append(x.toString())
          case x: Float  => out.append(x.toString())
          case x: Double => out.append(x.toString())
          case x: String => printString(x, out, printer)
          case x: Clues =>
            printApply(
              "Clues",
              x.values.iterator,
              out,
              indent,
              nextIndent,
              open = " {",
              close = "}",
              comma = ""
            ) { clue =>
              if (clue.source.nonEmpty) {
                out.append(clue.source)
              }
              if (clue.valueType.nonEmpty) {
                out.append(": ").append(clue.valueType)
              }
              out.append(" = ")
              loop(clue.value, nextIndent)
            }
          case None =>
            out.append("None")
          case Nil =>
            out.append("Nil")
          case x: Map[_, _] =>
            printApply(
              Compat.collectionClassName(x),
              x.iterator,
              out,
              indent,
              nextIndent
            ) { case (key, value) =>
              loop(key, nextIndent)
              out.append(" -> ")
              loop(value, nextIndent)
            }
          case x: Iterable[_] =>
            printApply(
              Compat.collectionClassName(x),
              x.iterator,
              out,
              indent,
              nextIndent
            ) { value => loop(value, nextIndent) }
          case x: Array[_] =>
            printApply(
              "Array",
              x.iterator,
              out,
              indent,
              nextIndent
            ) { value => loop(value, nextIndent) }
          case it: Iterator[_] =>
            if (it.isEmpty) out.append("empty iterator")
            else out.append("non-empty iterator")
          case p: Product =>
            val elementNames = Compat.productElementNames(p)
            val infiniteElementNames = Iterator.continually {
              if (elementNames.hasNext) elementNames.next()
              else ""
            }
            printApply(
              p.productPrefix,
              p.productIterator.zip(infiniteElementNames),
              out,
              indent,
              nextIndent
            ) { case (value, key) =>
              if (key.nonEmpty) {
                out.append(key).append(" = ")
              }
              loop(value, nextIndent)
            }
          case _ =>
            out.append(a.toString())
        }
      }
    }
    loop(any, indent = 0)
    munit.diff.console.AnsiColors.filterAnsi(out.toString())
  }

  private def printApply[T](
      prefix: String,
      it: Iterator[T],
      out: StringBuilder,
      indent: Int,
      nextIndent: Int,
      open: String = "(",
      close: String = ")",
      comma: String = ","
  )(fn: T => Unit): Unit = {
    out.append(prefix)
    out.append(open)
    if (it.hasNext) {
      printNewline(out, nextIndent)
      while (it.hasNext) {
        val value = it.next()
        fn(value)
        if (it.hasNext) {
          out.append(comma)
          printNewline(out, nextIndent)
        } else {
          printNewline(out, indent)
        }
      }
    }
    out.append(close)
  }

  private def printNewline(out: StringBuilder, indent: Int): Unit = {
    out.append("\n")
    var i = 0
    while (i < indent) {
      out.append(' ')
      i += 1
    }
  }

  /**
   * Pretty-prints this string with non-visible characters escaped.
   *
   * The exact definition of "non-visible" is fuzzy and is subject to change.
   * The original motivation for this method was to fix
   * https://github.com/scalameta/munit/issues/258 related to escaping \r in
   * test names.
   *
   * The spirit of this method is to preserve "visible" characters like emojis
   * and double quotes and escape "non-visible" characters like newlines and
   * ANSI escape codes. A non-goal of this method is to make the output
   * copy-pasteable back into source code unlike the `printChar` method, which
   * escapes for example double-quote characters.
   */
  def escapeNonVisible(string: String): String = {
    val out = new StringBuilder()
    var i = 0
    while (i < string.length()) {
      val ch = string.charAt(i)
      ch match {
        case '"' | '\'' => out.append(ch)
        case _          => printChar(ch, out, isEscapeUnicode = false)
      }
      i += 1
    }
    out.toString()
  }

}
