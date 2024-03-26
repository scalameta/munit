package munit.internal.io

object PlatformIO {
  type File = munit.internal.io.File
  object File {
    def separatorChar = munit.internal.io.File.separatorChar
  }

  object Files {
    def readAllLines(path: Path): java.util.List[String] =
      munit.internal.io.Files.readAllLines(path)
  }

  type Path = MunitPath
  val Paths = munit.internal.io.Paths
}
