package munit.internal

import munit.Location
import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

object Compat {
  def productElementNames(p: Product): Iterator[String] =
    p.productElementNames
  def collectionClassName(i: Iterable[_]): String =
    i.asInstanceOf[{ def collectionClassName: String }].collectionClassName

  trait LocationMacro {
    implicit def generateLocation: Location = macro locationImpl
  }

  def locationImpl(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(typeOf[Location], path, line)
  }

}
