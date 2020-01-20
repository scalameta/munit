package munit

class BaseSuite extends FunSuite {
  override def munitRunTest(options: TestOptions, body: => Any): Any = {
    if (options.tags(NoDotty) && BuildInfo.scalaVersion.startsWith("0.")) {
      Ignore
    } else {
      super.munitRunTest(options, body)
    }
  }
}
