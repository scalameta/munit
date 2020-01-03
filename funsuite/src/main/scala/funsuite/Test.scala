package funsuite

import java.nio.file.Path
import java.nio.file.PathMatcher

class Test(
    val name: String,
    val body: () => Any,
    val tags: Set[Tag],
    val location: Location
) {
  override def toString(): String = s"TestCase($name)"
}
