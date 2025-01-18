package munit

class AssertionsFrameworkSuite extends FunSuite {
  test("equal-tostring") {
    class A() {
      override def toString = "C"
    }
    class B() {
      override def toString = "C"
    }
    assertEquals[Any, Any](new A(), new B())
  }

  test("case-class-productPrefix") {
    object a {
      case class A()
    }
    object b {
      case class A()
    }
    assertEquals[Any, Any](a.A(), b.A())
  }

  test("different-toString") {
    object a {
      case class A() {
        override def toString = "a.A()"
      }
    }
    object b {
      case class A() {
        override def toString = "b.B()"
      }
    }
    assertEquals[Any, Any](a.A(), b.A())
  }

  test("toString-has-different-whitespace")(
    assertEquals[Any, Any]("foo", "foo  ")
  )
}

object AssertionsFrameworkSuite
    extends FrameworkTest(
      classOf[AssertionsFrameworkSuite],
      """|==> failure munit.AssertionsFrameworkSuite.equal-tostring - tests/shared/src/main/scala/munit/AssertionsFrameworkSuite.scala:11 values are not equal even if they have the same `toString()`: C
         |10:    }
         |11:    assertEquals[Any, Any](new A(), new B())
         |12:  }
         |==> failure munit.AssertionsFrameworkSuite.case-class-productPrefix - tests/shared/src/main/scala/munit/AssertionsFrameworkSuite.scala:21 values are not equal even if they have the same `toString()`: A()
         |20:    }
         |21:    assertEquals[Any, Any](a.A(), b.A())
         |22:  }
         |==> failure munit.AssertionsFrameworkSuite.different-toString - tests/shared/src/main/scala/munit/AssertionsFrameworkSuite.scala:35
         |34:    }
         |35:    assertEquals[Any, Any](a.A(), b.A())
         |36:  }
         |values are not the same
         |=> Obtained
         |a.A()
         |=> Diff (- expected, + obtained)
         |-b.B()
         |+a.A()
         |==> failure munit.AssertionsFrameworkSuite.toString-has-different-whitespace - tests/shared/src/main/scala/munit/AssertionsFrameworkSuite.scala:39 values are not equal, even if their text representation only differs in leading/trailing whitespace and ANSI escape characters: foo
         |38:  test("toString-has-different-whitespace")(
         |39:    assertEquals[Any, Any]("foo", "foo  ")
         |40:  )
         |""".stripMargin,
    )
