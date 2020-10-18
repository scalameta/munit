package munit

import scala.collection.mutable

class StrictCompareErrorSuite extends BaseSuite {
  implicit def iterableCompare[A, B, C[x] <: Iterable[x], D[x] <: Iterable[x]]
      : Compare[C[A], D[B]] = Compare.defaultCompare[C[A], D[B]]

  test("basic".fail) {
    assertEquals(List(1), mutable.ArrayBuffer(1, 2))
  }

  test("seq-ok") {
    assertNotEquals(List(1), Set(1))
    assertEquals(List(1), Vector(1))
    // NOTE: the examples below succeed at compile-time and runtime, for better or worse
    assertEquals(List[Float](1f), Vector[Int](1))
    assertEquals(List[Long](1L), Vector[Int](1))
    // NOTE: assertEquals('a', 'a'.toInt) does not compile for some reason, see test below.
    assertEquals(List[Char]('a'), Vector[Int]('a'.toInt))
  }

  test("assertEquals") {
    assertNoDiff(
      compileErrors("assertEquals('a', 'a'.toInt)"),
      """|error:
         |Can't compare these two types when using strict equality.
         |  First type:  Char
         |  Second type: Int
         |Possible ways to fix this problem:
         |  Alternative 1: provide an implicit instance of type Equality[Char, Int]
         |  Alternative 2: use assertEquals[Any, Any](...) if you think it's OK to compare these types at runtime
         |  Alternative 3: disable strict equality by removing the compiler option "-Xmacro-settings:munit.strictEquality"
         |
         |assertEquals('a', 'a'.toInt)
         |            ^
         |""".stripMargin
    )
  }

  test("assertNotEquals") {
    assertNoDiff(
      compileErrors("assertNotEquals('a', 42)"),
      """|error:
         |Can't compare these two types when using strict equality.
         |  First type:  Char
         |  Second type: Int
         |Possible ways to fix this problem:
         |  Alternative 1: provide an implicit instance of type Equality[Char, Int]
         |  Alternative 2: use assertNotEquals[Any, Any](...) if you think it's OK to compare these types at runtime
         |  Alternative 3: disable strict equality by removing the compiler option "-Xmacro-settings:munit.strictEquality"
         |
         |assertNotEquals('a', 42)
         |               ^
         |""".stripMargin
    )
  }

  test("subtyping") {
    assertNoDiff(
      compileErrors("""assertEquals("a".subSequence(0, 1), "a")"""),
      """|error:
         |Can't compare these two types when using strict equality.
         |  First type:  CharSequence
         |  Second type: String
         |Possible ways to fix this problem:
         |  Alternative 1: provide an implicit instance of type Equality[CharSequence, String]
         |  Alternative 2: use assertEquals[Any, Any](...) if you think it's OK to compare these types at runtime
         |  Alternative 3: disable strict equality by removing the compiler option "-Xmacro-settings:munit.strictEquality"
         |
         |assertEquals("a".subSequence(0, 1), "a")
         |            ^
         |""".stripMargin
    )
  }

}
