package munit.internal.console

import java.nio.file.{Files, Path, Paths}

import munit.Location

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.NonFatal
import munit.Clues

class Lines extends Serializable {
  private val filecache = mutable.Map.empty[Path, Array[String]]

  def formatLine(location: Location, message: String): String = {
    formatLine(location, message, new Clues(Nil))
  }
  def formatLine(location: Location, message: String, clues: Clues): String = {
    try {
      val path = Paths.get(location.path)
      val lines = filecache.getOrElseUpdate(path, {
        Files.readAllLines(path).asScala.toArray
      })
      val slice = lines.slice(location.line - 2, location.line + 1)
      val out = new StringBuilder()
      if (slice.length == 3) {
        val width = (location.line + 1).toString().length()
        def format(n: Int): String = {
          val number = n.toString() + ":"
          val padding = " " * (width - number.length() + 1)
          number + padding
        }
        val isMultilineMessage = message.contains('\n')
        out
          .append(location.path)
          .append(':')
          .append(location.line.toString())
        if (message.length() > 0 && !isMultilineMessage) {
          out.append(" ").append(message)
        }
        out
          .append('\n')
          .append(format(location.line - 1))
          .append(slice(0))
          .append('\n')
          .append(AnsiColors.Reversed)
          .append(format(location.line))
          .append(slice(1))
          .append(AnsiColors.Reset)
          .append('\n')
          .append(format(location.line + 1))
          .append(slice(2))
        if (isMultilineMessage) {
          out.append('\n').append(message)
        }
        if (clues.values.nonEmpty) {
          out.append('\n').append(Printers.print(clues))
        }
      }
      out.toString()
    } catch {
      case NonFatal(_) =>
        if (clues.values.isEmpty) {
          message
        } else {
          message + "\n" + Printers.print(clues)
        }
    }
  }
}
