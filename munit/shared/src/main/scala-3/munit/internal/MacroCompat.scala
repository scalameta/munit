package munit.internal

import munit.Clue
import munit.Location
import scala.quoted._

object MacroCompat {

  trait LocationMacro {
    inline implicit def generate: Location = ${ locationImpl() }
  }

  def locationImpl()(implicit qctx: QuoteContext): Expr[Location] = {
    import qctx.tasty.{_, given _}
    val path = rootPosition.sourceFile.jpath.toString
    val startLine = rootPosition.startLine + 1
    '{ new Location(${Expr(path)}, ${Expr(startLine)}) }
  }

  trait ClueMacro {
    inline implicit def generate[T](value: T): Clue[T] = ${ clueImpl('value) }
  }

  def clueImpl[T:Type](value: Expr[T])(implicit qctx: QuoteContext): Expr[Clue[T]] = {
    import qctx.tasty.{_, given _}
    val source = value.unseal.pos.sourceCode
    val valueType = implicitly[scala.quoted.Type[T]].show
    '{ new Clue(${Expr(source)}, $value, ${Expr(valueType)}) }
  }

}
