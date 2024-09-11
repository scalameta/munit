package munit

class Issue179FrameworkSuite extends FunSuite {
  test("issue-179") {
    assertNoDiff("\n", "A\n")
  }
}

object Issue179FrameworkSuite
    extends FrameworkTest(
      classOf[Issue179FrameworkSuite],
      """|==> failure munit.Issue179FrameworkSuite.issue-179 - tests/shared/src/main/scala/munit/Issue179FrameworkSuite.scala:5
         |4:  test("issue-179") {
         |5:    assertNoDiff("/n", "A/n")
         |6:  }
         |diff assertion failed
         |=> Obtained
         |    '''|
         |       |'''.stripMargin
         |=> Diff (- obtained, + expected)
         |-
         |+A
         |""".stripMargin
    )
