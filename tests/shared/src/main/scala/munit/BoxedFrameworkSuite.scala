package munit

class BoxedFrameworkSuite extends FunSuite {

  case class Inner(value: Double)

  case class Outer(data: Seq[Inner])

  test("exist issue") {
    val values = Array(
      Outer(Seq(Inner(1), Inner(2), Inner(10))),
      Outer(Seq(Inner(1), Inner(2), Inner(10))),
      Outer(Seq(Inner(1), Inner(2), Inner(10)))
    )
    assert(values.exists(outer => outer.data.exists(inner => inner.value > 90)))
  }

}

object BoxedFrameworkSuite
    extends FrameworkTest(
      classOf[BoxedFrameworkSuite],
      """|==> failure munit.BoxedFrameworkSuite.exist issue - /scala/munit/BoxedFrameworkSuite.scala:15 assertion failed
         |14:    )
         |15:    assert(values.exists(outer => outer.data.exists(inner => inner.value > 90)))
         |16:  }
         |""".stripMargin
    )
