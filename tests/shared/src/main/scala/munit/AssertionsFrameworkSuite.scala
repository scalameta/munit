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
}

object AssertionsFrameworkSuite
    extends FrameworkTest(
      classOf[AssertionsFrameworkSuite],
      """|==> failure munit.AssertionsFrameworkSuite.equal-tostring - /scala/munit/AssertionsFrameworkSuite.scala:11 values are not equal even if they have the same `toString()`: C
         |10:    }
         |11:    assertEquals[Any, Any](new A(), new B())
         |12:  }
         |==> failure munit.AssertionsFrameworkSuite.case-class-productPrefix - /scala/munit/AssertionsFrameworkSuite.scala:21 values are not equal even if they have the same `toString()`: A()
         |20:    }
         |21:    assertEquals[Any, Any](a.A(), b.A())
         |22:  }
         |==> failure munit.AssertionsFrameworkSuite.different-toString - /scala/munit/AssertionsFrameworkSuite.scala:35
         |34:    }
         |35:    assertEquals[Any, Any](a.A(), b.A())
         |36:  }
         |values are not the same
         |=> Obtained
         |a.A()
         |=> Diff (- obtained, + expected)
         |-a.A()
         |+b.B()
         |""".stripMargin
    )
