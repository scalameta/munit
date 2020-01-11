package munit

/**
  * Metadata about a single test case.
  *
  * @param body the function to be evaluated for this test case.
  * @param tags the annotated tags for this test case.
  * @param location the file and line number where this test was defined.
  */
class GenericTest[T](
    val name: String,
    val body: () => T,
    val tags: Set[Tag],
    val location: Location
) {
  override def toString(): String = s"GenericTest($name)"
}
