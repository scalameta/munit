package munit

class StackTraceFrameworkSuite extends FunSuite {
  test("fail") {
    assertNoDiff("a", "b")
  }
}

class BaseStackTraceFrameworkSuite(arguments: Array[String], expected: String)
    extends FrameworkTest(
      classOf[StackTraceFrameworkSuite],
      expected,
      arguments = arguments,
      tags = Set(OnlyJVM),
      onEvent = { event =>
        if (event.throwable().isDefined()) {
          val s = event.throwable().get().getStackTrace()
          s.take(4)
            .map(e => s"  at ${e.getClassName()}:${e.getMethodName()}")
            .mkString("", "\n", "\n")
        } else {
          ""
        }
      }
    )

object FullStackTraceFrameworkSuite
    extends BaseStackTraceFrameworkSuite(
      Array("-F"),
      """|at munit.Assertions:failComparison
         |  at munit.Assertions:failComparison$
         |  at munit.FunSuite:failComparison
         |  at munit.Assertions$$anon$1:handle
         |==> failure munit.StackTraceFrameworkSuite.fail - tests/shared/src/main/scala/munit/StackTraceFrameworkSuite.scala:5
         |4:  test("fail") {
         |5:    assertNoDiff("a", "b")
         |6:  }
         |diff assertion failed
         |=> Obtained
         |"a"
         |=> Diff (- obtained, + expected)
         |-a
         |+b
         |""".stripMargin
    )

object SmallStackTraceFrameworkSuite
    extends BaseStackTraceFrameworkSuite(
      Array(),
      """|at munit.FunSuite:assertNoDiff
         |  at munit.StackTraceFrameworkSuite:$anonfun$new$1
         |  at scala.runtime.java8.JFunction0$mcV$sp:apply
         |==> failure munit.StackTraceFrameworkSuite.fail - tests/shared/src/main/scala/munit/StackTraceFrameworkSuite.scala:5
         |4:  test("fail") {
         |5:    assertNoDiff("a", "b")
         |6:  }
         |diff assertion failed
         |=> Obtained
         |"a"
         |=> Diff (- obtained, + expected)
         |-a
         |+b
         |""".stripMargin
    )
