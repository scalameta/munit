package munit.internal

import munit.Clue
import munit.Location
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object MacroCompat {

  trait LocationMacro {
    implicit def generate: Location = macro locationImpl
  }

  def locationImpl(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(typeOf[Location], path, line)
  }

  trait ClueMacro {
    implicit def generate[T](value: T): Clue[T] = macro clueImpl
  }

  def clueImpl(c: Context)(value: c.Tree): c.Tree = {
    import c.universe._
    import compat._
    val text: String =
      if (value.pos != null && value.pos.isRange) {
        val chars = value.pos.source.content
        val start = value.pos.start
        val end = value.pos.end
        if (end > start &&
            start >= 0 && start < chars.length &&
            end >= 0 && end < chars.length) {
          new String(chars, start, end - start)
        } else {
          ""
        }
      } else {
        ""
      }
    val source = Literal(Constant(text))
    New(
      TypeRef(NoPrefix, typeOf[Clue[_]].typeSymbol, List(value.tpe.widen)),
      source,
      value
    )
  }
}
