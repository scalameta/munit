package funsuite

import scala.collection.mutable
import java.nio.file.Path
import java.nio.file.PathMatcher
import scala.util.control.NonFatal

class Tag(val value: String)

object Tag {
  case object Ignore extends Tag("Ignore")
  case object ExpectFailure extends Tag("ExpectFailure")
  case object Flaky extends Tag("Flaky")
}
