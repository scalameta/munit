package munit.internal

object Compat {
  def productElementNames(p: Product): Iterator[String] = Iterator
    .continually("")
  def collectionClassName(i: Iterable[_]): String = i.stringPrefix
}
