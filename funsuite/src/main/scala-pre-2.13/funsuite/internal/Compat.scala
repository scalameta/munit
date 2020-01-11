package funsuite.internal

object Compat {
  def productElementNames(p: Product): Iterator[String] = {
    val cls = p.getClass()
    Iterator.continually("")
  }
  def collectionClassName(i: Iterable[_]): String =
    i.stringPrefix
}
