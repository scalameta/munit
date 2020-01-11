package munit

import scala.language.experimental.macros
import java.lang.annotation.Annotation
import java.io.File
import scala.reflect.macros.whitebox.Context

class Location(
    val path: String,
    val line: Int
) extends Annotation
    with Serializable {
  def filename: String = {
    val sep = path.lastIndexOf(File.separatorChar)
    if (sep < 0) path
    else path.substring(math.min(sep + 1, path.length() - 1))
  }
  def annotationType(): Class[Annotation] = classOf[Annotation]
}

object Location {
  implicit def generate: Location = macro impl
  def impl(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(typeOf[Location], path, line)
  }
}
