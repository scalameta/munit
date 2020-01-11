package funsuite

class GenericTest[T](
    val name: String,
    val body: () => T,
    val tags: Set[Tag],
    val location: Location
) {
  override def toString(): String = s"GenericTest($name)"
}
