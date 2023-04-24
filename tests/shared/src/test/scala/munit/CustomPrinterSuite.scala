package munit

import org.scalacheck.Prop.forAll
import munit.internal.console.Printers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop

class CustomPrinterSuite extends FunSuite with ScalaCheckSuite {

  private case class Foo(i: Int)

  private case class Bar(l: List[Int])

  private case class FooBar(foo: Foo, bar: Bar)

  private val genFoo = arbitrary[Int].map(Foo(_))

  // limit size to 10 to have a reasonable number of values
  private val genBar = arbitrary[List[Int]].map(l => Bar(l.take(10)))

  private val genFooBar = for {
    foo <- genFoo
    bar <- genBar
  } yield FooBar(foo, bar)

  private val longPrinter: Printer = Printer.apply { case l: Long =>
    s"MoreThanInt($l)"
  }

  private val fooPrinter: Printer = Printer.apply { case Foo(i) =>
    s"Foo(INT($i))"
  }

  private val listPrinter: Printer = Printer.apply { case l: List[_] =>
    l.mkString("[", ",", "]")
  }

  private val intPrinter: Printer = Printer.apply { case i: Int =>
    s"NotNaN($i)"
  }

  private val isScala213: Boolean = BuildInfo.scalaVersion.startsWith("2.13")

  private def checkProp(
      options: TestOptions,
      isEnabled: Boolean = true
  )(p: => Prop): Unit = {
    test(options) {
      assume(isEnabled, "disabled test")
      p
    }
  }

  checkProp("long") {
    forAll(arbitrary[Long]) { (l: Long) =>
      val obtained = Printers.print(l, longPrinter)
      val expected = s"MoreThanInt($l)"
      assertEquals(obtained, expected)
    }
  }

  checkProp("list") {
    forAll(arbitrary[List[Int]]) { l =>
      val obtained = Printers.print(l, listPrinter)
      val expected = l.mkString("[", ",", "]")
      assertEquals(obtained, expected)
    }
  }

  checkProp("product") {
    forAll(genFoo) { foo =>
      val obtained = Printers.print(foo, fooPrinter)
      val expected = s"Foo(INT(${foo.i}))"
      assertEquals(obtained, expected)
    }
  }

  checkProp("int in product", isEnabled = isScala213) {
    forAll(genFoo) { foo =>
      val obtained = Printers.print(foo, intPrinter)
      val expected = s"Foo(\n  i = NotNaN(${foo.i})\n)"
      assertEquals(obtained, expected)
    }
  }

  checkProp("list in product", isEnabled = isScala213) {
    forAll(genBar) { bar =>
      val obtained = Printers.print(bar, listPrinter)
      val expected = s"Bar(\n  l = ${bar.l.mkString("[", ",", "]")}\n)"
      assertEquals(obtained, expected)
    }
  }

  checkProp("list and int in product", isEnabled = isScala213) {
    forAll(genFooBar) { foobar =>
      val obtained = Printers
        .print(foobar, listPrinter.orElse(intPrinter))
        .filterNot(_.isWhitespace)
      val expected =
        s"""|FooBar(
            |  foo = Foo(
            |    i = NotNaN(${foobar.foo.i})
            |  ),
            |  bar = Bar(
            |    l = ${foobar.bar.l.mkString("[", ",", "]")}
            |  )
            |)
            |""".stripMargin.filterNot(_.isWhitespace)
      assertEquals(obtained, expected)
    }
  }

  checkProp("all ints in product", isEnabled = isScala213) {
    forAll(genFooBar) { foobar =>
      val obtained = Printers
        .print(foobar, intPrinter)
        .filterNot(_.isWhitespace)

      val expectedbBarList = foobar.bar.l match {
        case Nil => "Nil"
        case l =>
          l.map(i => s"NotNaN($i)").mkString("List(", ",", ")")
      }

      val expected =
        s"""|FooBar(
            |  foo = Foo(
            |    i = NotNaN(${foobar.foo.i})
            |  ),
            |  bar = Bar(
            |    l = $expectedbBarList
            |  )
            |)
            |""".stripMargin.filterNot(_.isWhitespace)
      assertEquals(obtained, expected)
    }
  }

}
