package munit.internal.console

import munit.internal.io.PlatformIO.{Files, Path, Paths}
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
  def formatPath(location: Location): String =
    location.path
  def formatLine(location: Location, message: String, clues: Clues): String = {
    try {
      val path = Paths.get(location.path)
      val lines = filecache.getOrElseUpdate(
        path, {
          Files.readAllLines(path).asScala.toArray
        }
      )
      val slice = lines.slice(location.line - 2, location.line + 1)
      val out = new StringBuilder()
      if (slice.length >= 2) {
        val width = (location.line + 1).toString().length() + 1
        def format(n: Int): String = {
          s"$n:".padTo(width, ' ')
        }
        val isMultilineMessage = message.contains('\n')
        out
          .append(formatPath(location))
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
          .append(
            munit.diff.console.AnsiColors
              .use(munit.diff.console.AnsiColors.Reversed)
          )
          .append(format(location.line))
          .append(slice(1))
          .append(
            munit.diff.console.AnsiColors
              .use(munit.diff.console.AnsiColors.Reset)
          )
        if (slice.length >= 3)
          out
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
