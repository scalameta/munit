package funsuite

import scala.collection.mutable
import java.nio.file.Path
import java.nio.file.PathMatcher
import scala.util.control.NonFatal
import java.lang.annotation.Annotation
import scala.reflect.ClassTag
import scala.runtime.Statics

class Tag(val value: String) extends Annotation {
  // Not a case class so that it possible to override these.
  override def equals(obj: Any): Boolean = obj match {
    case t: Tag => t.value == value
    case _      => false
  }
  override def hashCode(): Int = {
    var acc: Int = -881232
    acc = Statics.mix(acc, Statics.anyHash("funsuite.Tag"))
    acc = Statics.mix(acc, Statics.anyHash(value))
    acc
  }
  def annotationType(): Class[_ <: Annotation] = this.getClass()
  override def toString(): String = s"Tag($value)"
}
