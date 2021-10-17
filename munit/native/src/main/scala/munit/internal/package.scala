package munit

import java.{util => ju}
package object internal {
  type File = java.io.File
  object File {
    def separatorChar = java.io.File.separatorChar
  }

  object Files {
    def readAllLines(path: Path): ju.List[String] =
      java.nio.file.Files.readAllLines(path)
  }

  type Path = java.nio.file.Path
  object Paths {
    def get(path: String): Path = java.nio.file.Paths.get(path)
  }

  type InvocationTargetException = java.lang.reflect.InvocationTargetException
  type UndeclaredThrowableException =
    java.lang.reflect.UndeclaredThrowableException
}
