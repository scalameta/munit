package munit

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.Path

trait FileLayout {

  protected def fileLayoutDefaultTempDirName = "file-layout"

  /**
   * Parse a file layout from string
   *
   * @param layout
   * @return the file layout as a map of path -> content
   * @throws IllegalArgumentException if the layout is formally invalid
   */
  protected def parseFileLayout(layout: String): Map[String, String] =
    if (!layout.trim.isEmpty) {
      val lines = layout.replaceAllLiterally("\r\n", "\n")
      lines
      // lines starting with '/' are file paths, but lines starting with '//' are comments
        .split("(\n/[^/])")
        .map { row =>
          row.split("\n", 2).toList match {
            case path :: contents :: Nil =>
              path.stripPrefix("/") -> contents
            case els =>
              throw new IllegalArgumentException(
                s"Unable to split argument info path/contents! \n$els"
              )
          }
        }
        .toMap
    } else {
      Map.empty
    }

  /**
   * Write a file layout to disk
   *
   * @param layout
   * @param root the root of the file layout. Defaults to a temporary directory
   * @param charset the charset for writing the files. Defaults to UTF-8
   * @return the root of the file layout
   * @throws IllegalArgumentException if the layout is formally invalid
   */
  def writeFileLayout(
      layout: String,
      root: Path = Files.createTempDirectory(fileLayoutDefaultTempDirName),
      charset: Charset = StandardCharsets.UTF_8
  ): Path = {
    if (!layout.trim.isEmpty) {
      parseFileLayout(layout).foreach {
        case (path, contents) =>
          val file = root.resolve(path)
          val parent = file.getParent
          if (!Files.exists(parent)) { // cannot create directories when parent is a symlink
            Files.createDirectories(parent)
          }
          Files.deleteIfExists(file)
          Files.write(
            file,
            contents.getBytes(charset),
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE
          )
      }
    }
    root
  }

}
