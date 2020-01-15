package munit.internal

object Compat {
  def productElementNames(p: Product): Iterator[String] =
    p.productElementNames
  def collectionClassName(i: Iterable[_]): String =
    i.asInstanceOf[{ def collectionClassName: String }].collectionClassName
}
