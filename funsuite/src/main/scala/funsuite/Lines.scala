package funsuite

import scala.collection.mutable
import java.nio.file.Path
import java.nio.file.Paths
import fansi.Str
import java.nio.file.Files
import scala.util.control.NonFatal
import fansi.Bold
import fansi.Reversed
import scala.collection.JavaConverters._

class Lines {
  private val filecache = mutable.Map.empty[Path, Array[String]]

  def formatLine(location: Location, message: String): String = {
    try {
      val path = Paths.get(location.path)
      val lines = filecache.getOrElseUpdate(path, {
        Files.readAllLines(path).asScala.toArray
      })
      val slice = lines.slice(location.line - 2, location.line + 1)
      if (slice.length == 3) {
        Str
          .join(
            location.path,
            ":",
            location.line.toString(),
            if (message.length == 0) "" else " ",
            message,
            "\n",
            Bold.Off(s"${location.line - 1}: ${slice(0)}"),
            "\n",
            Reversed.On(s"${location.line}: ${slice(1)}"),
            "\n",
            Bold.Off(s"${location.line + 1}: ${slice(2)}")
          )
          .render
      } else {
        ""
      }
    } catch {
      case NonFatal(_) =>
        ""
    }
  }
}
