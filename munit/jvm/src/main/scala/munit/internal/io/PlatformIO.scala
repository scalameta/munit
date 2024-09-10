package munit.internal.io

object PlatformIO {
  type File = java.io.File
  object File {
    def separatorChar = java.io.File.separatorChar
  }

  object Files {
    def readAllLines(path: Path): java.util.List[String] =
      java.nio.file.Files.readAllLines(path)
    def exists(path: Path): Boolean =
      java.nio.file.Files.exists(path)
  }

  type Path = java.nio.file.Path
  object Path {
    def workingDirectory: Path = Paths.get(sys.props("user.dir"))
  }
  object Paths {
    def get(path: String): Path = java.nio.file.Paths.get(path)
  }
}
