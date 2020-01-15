package munit.internal

import munit.Location
import scala.quoted._
import scala.reflect.Selectable.reflectiveSelectable

object Compat {
  def productElementNames(p: Product): Iterator[String] =
    p.productElementNames
  def collectionClassName(i: Iterable[_]): String = {
    i.asInstanceOf[{ def collectionClassName: String }].collectionClassName
  }

  trait LocationMacro {
    inline implicit def generateLocation: Location = ${ locationImpl() }
  }

  def locationImpl()(given qctx: QuoteContext): Expr[Location] = {
    import qctx.tasty.{_, given}
    val path = rootPosition.sourceFile.jpath.toString
    val startLine = rootPosition.startLine
    '{ new Location(${Expr(path)}, ${Expr(startLine)}) }
  }

}
