package munit.internal

object PlatformPathIO {
  def workingDirectoryString: String =
    JSIO.cwd()
}
