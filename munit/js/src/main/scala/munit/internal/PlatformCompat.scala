package munit.internal

object PlatformCompat {
  // Scala.js does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false
}
