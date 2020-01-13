package munit

import scala.language.experimental.macros
import java.lang.annotation.Annotation
import java.io.File
import scala.reflect.macros.whitebox.Context
import scala.runtime.Statics

object Location {
  def empty: Location = new Location("", 0)
  implicit def generate: Location = macro impl
  def impl(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(typeOf[Location], path, line)
  }
}

final class Location(
    val path: String,
    val line: Int
) extends Annotation
    with Serializable {
  def filename: String = {
    val sep = path.lastIndexOf(File.separatorChar)
    if (sep < 0) path
    else path.substring(math.min(sep + 1, path.length() - 1))
  }
  override def annotationType(): Class[Annotation] = {
    classOf[Annotation]
  }
  override def toString: String = {
    path + ":" + line
  }
  override def equals(obj: Any): Boolean = {
    obj.asInstanceOf[AnyRef].eq(this) || (obj match {
      case l: Location =>
        l.path == path &&
          l.line == line
      case _ =>
        false
    })
  }
  override def hashCode(): Int = {
    var acc = 423142142
    acc = Statics.mix(acc, Statics.anyHash(path))
    acc = Statics.mix(acc, line)
    acc
  }
}
