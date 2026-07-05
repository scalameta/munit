package munit

class EmptyFrameworkSuite extends FunSuite

object EmptyFrameworkStdoutJVMSuite
    extends FrameworkTest(
      classOf[EmptyFrameworkSuite],
      """|Test run munit.EmptyFrameworkSuite started
         |==> i munit.EmptyFrameworkSuite ignored <elapsed time>
         |Test run munit.EmptyFrameworkSuite finished: 0 failed, 1 ignored, 0 total <elapsed time>
         |""".stripMargin,
      tags = Set(OnlyJVM),
      format = StdoutFormat,
    )

object EmptyFrameworkStdoutJsNativeSuite
    extends FrameworkTest(
      classOf[EmptyFrameworkSuite],
      """|==> i munit.EmptyFrameworkSuite ignored
         |""".stripMargin,
      tags = Set(NoJVM),
      format = StdoutFormat,
    )
