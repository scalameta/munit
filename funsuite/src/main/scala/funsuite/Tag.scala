package funsuite

import scala.collection.mutable
import java.nio.file.Path
import java.nio.file.PathMatcher
import scala.util.control.NonFatal
import java.lang.annotation.Annotation
import scala.reflect.ClassTag

class Tag(val value: String) extends Annotation {
  def annotationType(): Class[_ <: Annotation] = this.getClass()
}

case object Ignore extends Tag("Ignore")

case object ExpectFailure extends Tag("ExpectFailure")

case object Flaky extends Tag("Flaky")
