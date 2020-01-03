package funsuite

import java.nio.file.Path
import java.nio.file.PathMatcher

class Test(val name: String, val body: () => Unit, val tags: Set[Tag]) {
  override def toString(): String = s"TestCase($name)"
}
