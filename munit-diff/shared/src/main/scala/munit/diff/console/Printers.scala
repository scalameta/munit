package munit.diff.console

import munit.diff.{EmptyPrinter, Printer}

import scala.annotation.switch

object Printers {

  def print(input: String): String = {
    val out = new StringBuilder()
    printString(input, out, EmptyPrinter)
    munit.diff.console.AnsiColors.filterAnsi(out.toString())
  }

  def printString(
      string: String,
      out: StringBuilder,
      printer: Printer,
  ): Unit = {
    val isMultiline = printer.isMultiline(string)
    if (isMultiline) {
      out.append('"')
      out.append('"')
      out.append('"')
      out.append(string)
      out.append('"')
      out.append('"')
      out.append('"')
    } else {
      out.append('"')
      var i = 0
      while (i < string.length()) {
        printChar(string.charAt(i), out)
        i += 1
      }
      out.append('"')
    }
  }

  def printChar(
      c: Char,
      sb: StringBuilder,
      isEscapeUnicode: Boolean = true,
  ): Unit = (c: @switch) match {
    case '"' => sb.append("\\\"")
    case '\\' => sb.append("\\\\")
    case '\b' => sb.append("\\b")
    case '\f' => sb.append("\\f")
    case '\n' => sb.append("\\n")
    case '\r' => sb.append("\\r")
    case '\t' => sb.append("\\t")
    case c =>
      val isNonReadableAscii = c < ' ' || c > '~' && isEscapeUnicode
      if (isNonReadableAscii && !Character.isLetter(c)) sb
        .append("\\u%04x".format(c.toInt))
      else sb.append(c)
  }
}
