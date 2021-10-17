package munit

package object internal {
  type File = java.io.File
  object Files {
    def readAllLines(path: Path) = java.nio.file.Files.readAllLines(path)
  }

  type Path = java.nio.file.Path
  object Paths {
    def get(path: String) = java.nio.file.Paths.get(path)
  }
}
