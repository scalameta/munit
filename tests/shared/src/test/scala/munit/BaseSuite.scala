package munit

import munit.internal.PlatformCompat

class BaseSuite extends FunSuite {
  override def munitRunTest(options: TestOptions, body: => Any): Any = {
    def isDotty: Boolean =
      BuildInfo.scalaVersion.startsWith("0.")
    def is213: Boolean =
      BuildInfo.scalaVersion.startsWith("2.13") || isDotty
    if (options.tags(NoDotty) && isDotty) {
      Ignore
    } else if (options.tags(Only213) && !is213) {
      Ignore
    } else if (options.tags(OnlyJVM) && !PlatformCompat.isJVM) {
      Ignore
    } else {
      super.munitRunTest(options, body)
    }
  }
}
