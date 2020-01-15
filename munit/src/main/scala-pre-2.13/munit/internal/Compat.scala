package munit.internal

object Compat {
  def productElementNames(p: Product): Iterator[String] =
    Iterator.continually("")
  def collectionClassName(i: Iterable[_]): String =
    i.stringPrefix

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
