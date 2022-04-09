package munit.internal.io

object PlatformPathIO {
  def workingDirectoryString: String =
    JSIO.cwd()
}
