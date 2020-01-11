package funsuite.internal

import scala.annotation.switch

trait Printable {
  def print(out: StringBuilder, indent: Int): Unit
}
trait Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean
}
object EmptyPrinter extends Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean = false
}

object Printers {
  def print(any: Any, printer: Printer = EmptyPrinter): String = {
    val out = new StringBuilder()
    val indentStep = 2
    def loop(a: Any, indent: Int): Unit = {
      val nextIndent = indent + indentStep
      class IterableFunction(value: Any) extends Function0[Unit] {
        override def apply(): Unit = {
          loop(value, nextIndent)
        }
      }
      class MapFunction(key: Any, value: Any) extends Function0[Unit] {
        override def apply(): Unit = {
          loop(key, nextIndent)
          out.append(" -> ")
          loop(value, nextIndent)
        }
      }
      class ProductElementFunction(key: String, value: Any)
          extends Function0[Unit] {
        override def apply(): Unit = {
          if (key.nonEmpty) {
            out.append(key).append(" = ")
          }
          loop(value, nextIndent)
        }
      }
      def printApply(prefix: String, it: Iterator[() => Unit]): Unit = {
        out.append(prefix)
        out.append('(')
        if (it.hasNext) {
          printNewline(out, nextIndent)
          while (it.hasNext) {
            val fn = it.next()
            fn()
            if (it.hasNext) {
              out.append(',')
              printNewline(out, nextIndent)
            } else {
              printNewline(out, indent)
            }
          }
        }
        out.append(')')
      }
      val isDone = printer.print(a, out, indent)
      if (!isDone) {
        a match {
          case null         => out.append("null")
          case x: Printable => x.print(out, indent)
          case x: Char =>
            out.append('\'')
            printChar(x, out)
            out.append('\'')
          case x: Byte   => out.append(x.toString())
          case x: Short  => out.append(x.toString())
          case x: Int    => out.append(x.toString())
          case x: Long   => out.append(x.toString())
          case x: Float  => out.append(x.toString())
          case x: Double => out.append(x.toString())
          case x: String => printString(x, out)
          case x: Map[_, _] =>
            printApply(
              Compat.collectionClassName(x),
              x.iterator.map {
                case (key, value) => new MapFunction(key, value)
              }
            )
          case x: Iterable[_] =>
            printApply(
              Compat.collectionClassName(x),
              x.iterator.map(new IterableFunction(_))
            )
          case x: Array[_] =>
            printApply("Array", x.iterator.map(new IterableFunction(_)))
          case None =>
            out.append("None")
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
              p.productIterator.zip(infiniteElementNames).map {
                case (value, key) =>
                  new ProductElementFunction(key, value)
              }
            )
          case _ =>
            out.append(any.toString())
        }
      }
    }
    loop(any, indent = 0)
    out.toString()
  }

  private def printNewline(out: StringBuilder, indent: Int): Unit = {
    out.append("\n")
    var i = 0
    while (i < indent) {
      out.append(' ')
      i += 1
    }
  }
  private def printString(string: String, out: StringBuilder): Unit = {
    val isMultiline = string.contains('\n')
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

  private def printChar(c: Char, sb: StringBuilder) =
    (c: @switch) match {
      case '"'  => sb.append("\\\"")
      case '\'' => sb.append("\\'")
      case '\\' => sb.append("\\\\")
      case '\b' => sb.append("\\b")
      case '\f' => sb.append("\\f")
      case '\n' => sb.append("\\n")
      case '\r' => sb.append("\\r")
      case '\t' => sb.append("\\t")
      case c =>
        val isUnicode = true
        if (c < ' ' || (c > '~' && isUnicode))
          sb.append("\\u%04x" format c.toInt)
        else sb.append(c)
    }

}
