package munit.internal

package object io {
  type File = java.io.File
  object File {
    def separatorChar = java.io.File.separatorChar
  }

  object Files {
    def readAllLines(path: Path): java.util.List[String] =
      java.nio.file.Files.readAllLines(path)
  }

  type Path = java.nio.file.Path
  object Paths {
    def get(path: String): Path = java.nio.file.Paths.get(path)
  }
}
