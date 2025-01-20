package munit.internal.console

import munit.Clues
import munit.Location
import munit.diff.console.AnsiColors
import munit.internal.io.PlatformIO.Files
import munit.internal.io.PlatformIO.Path
import munit.internal.io.PlatformIO.Paths

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.NonFatal

class Lines extends Serializable {
  private val filecache = mutable.Map.empty[Path, Array[String]]

  def formatPath(location: Location): String = location.path

  def findPath(cwd: String, path: String, max: Int): Path = {
    val p = Paths.get(cwd).resolve(path)
    def getParentPath(somePath: String, sep: String): String = {
      val somePath1 =
        if (somePath.endsWith(sep)) somePath.dropRight(sep.length) else somePath
      val sep1 = if (sep == "\\") "\\\\" else sep
      somePath1.split(sep1).dropRight(1).mkString(sep)
    }
    if (Files.exists(p)) p
    else if (max < 1) sys.error(s"$path was not found")
    else if (cwd.contains("\\"))
      findPath(getParentPath(cwd, "\\"), path, max - 1)
    else findPath(getParentPath(cwd, "/"), path, max - 1)
  }

  def formatLine(
      location: Location,
      message: String,
      clues: Clues = Clues.empty,
      ansi: Boolean = true,
  ): String =
    try {
      val path = findPath(Path.workingDirectory.toString, location.path, 3)
      val lines = filecache
        .getOrElseUpdate(path, Files.readAllLines(path).asScala.toArray)
      val slice = lines.slice(location.line - 2, location.line + 1)
      implicit val out: StringBuilder = new StringBuilder()
      if (slice.length >= 2) {
        val width = (location.line + 1).toString.length() + 1
        def format(n: Int): String = s"$n:".padTo(width, ' ')
        val isMultilineMessage = message.contains('\n')
        out.append(formatPath(location)).append(':').append(location.line)
        if (message.nonEmpty && !isMultilineMessage) out.append(" ")
          .append(message)
        out.append('\n').append(format(location.line - 1)).append(slice(0))
          .append('\n')
        AnsiColors.c(AnsiColors.Reversed, ansi)(
          _.append(format(location.line)).append(slice(1))
        )
        if (slice.length >= 3) out.append('\n').append(format(location.line + 1))
          .append(slice(2))
        if (isMultilineMessage) out.append('\n').append(message)
        if (clues.values.nonEmpty) out.append('\n').append(Printers.print(clues))
      }
      out.toString()
    } catch {
      case NonFatal(_) =>
        if (clues.values.isEmpty) message
        else message + "\n" + Printers.print(clues)
    }
}
