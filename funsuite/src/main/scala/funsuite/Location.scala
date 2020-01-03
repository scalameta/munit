package funsuite

import java.lang.annotation.Annotation
import java.io.File

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
  implicit def generate(
      implicit
      filename: sourcecode.File,
      line: sourcecode.Line
  ): Location = {
    new Location(filename.value, line.value)
  }
}
