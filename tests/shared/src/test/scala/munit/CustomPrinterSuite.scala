package munit

import org.scalacheck.Prop.forAll
import munit.internal.console.Printers
import org.scalacheck.Arbitrary.arbitrary
import scala.collection.immutable

class CustomPrinterSuite extends FunSuite with ScalaCheckSuite {

  private case class Foo(i: Int)

  private case class Bar(is: List[Int])

  private case class FooBar(foo: Foo, bar: Bar)

  private val genFoo = arbitrary[Int].map(Foo(_))

  private val genBar = arbitrary[List[Int]].map(Bar(_))

  private val genFooBar = for {
    foo <- genFoo
    bar <- genBar
  } yield FooBar(foo, bar)

  private val longPrinter = Printer { case l: Long =>
    s"MoreThanInt($l)"
  }

  private val fooPrinter = Printer { case Foo(i) =>
    s"Foo(INT($i))"
  }

  private val listPrinter = Printer { case l: List[_] =>
    l.mkString("[", ",", "]")
  }

  private val intPrinter = Printer { case i: Int =>
    s"NotNaN($i)"
  }

  property("long") {
    forAll(arbitrary[Long]) { (l: Long) =>
      val obtained = Printers.print(l, longPrinter)
      val expected = s"MoreThanInt($l)"
      assertEquals(obtained, expected)
    }
  }

  property("list") {
    forAll(arbitrary[List[Int]]) { l =>
      val obtained = Printers.print(l, listPrinter)
      val expected = l.mkString("[", ",", "]")
      assertEquals(obtained, expected)
    }
  }

  property("product") {
    forAll(arbitrary[Int]) { i =>
      val input = Foo(i)
      val obtained = Printers.print(input, fooPrinter)
      val expected = s"Foo(INT($i))"
      assertEquals(obtained, expected)
    }
  }

  property("int in product") {
    forAll(arbitrary[Int]) { i =>
      val input = Foo(i)
      val obtained = Printers.print(input, intPrinter)
      val expected = s"Foo(\n  i = NotNaN($i)\n)"
      assertEquals(obtained, expected)
    }
  }

  property("list in product") {
    forAll(arbitrary[List[Int]]) { l =>
      val input = Bar(l)
      val obtained = Printers.print(input, listPrinter)
      val expected = s"Bar(\n  is = ${l.mkString("[", ",", "]")}\n)"
      assertEquals(obtained, expected)
    }
  }

  property("list and int in product") {
    forAll(genFooBar) { foobar =>
      val obtained = Printers
        .print(foobar, listPrinter.orElse(intPrinter))
        .stripMargin
        .stripLeading()
        .stripTrailing()
      val expected =
        s"""|FooBar(
            |  foo = Foo(
            |    i = NotNaN(${foobar.foo.i})
            |  ),
            |  bar = Bar(
            |    is = ${foobar.bar.is.mkString("[", ",", "]")}
            |  )
            |)
            |""".stripMargin.stripLeading().stripTrailing()
      assertEquals(obtained, expected)
    }
  }

  property("all ints in product") {
    forAll(genFooBar) { foobar =>
      val obtained = Printers
        .print(foobar, intPrinter)
        .filterNot(_.isWhitespace)

      val expectedbBarList = foobar.bar.is match {
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
            |    is = $expectedbBarList
            |  )
            |)
            |""".stripMargin.filterNot(_.isWhitespace)
      assertEquals(obtained, expected)
    }
  }

}
