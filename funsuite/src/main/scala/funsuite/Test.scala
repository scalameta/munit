package funsuite

class Test(
    val name: String,
    val body: () => Any,
    val tags: Set[Tag],
    val location: Location
) {
  override def toString(): String = s"TestCase($name)"
}
