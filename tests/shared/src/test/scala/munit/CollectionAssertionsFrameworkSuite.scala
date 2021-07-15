package munit

import scala.collection.{SortedMap, immutable, mutable}
import scala.collection.immutable.SortedSet

class CollectionAssertionsFrameworkSuite extends BaseSuite {
  test("An assertion succeeds on collections with the same elements") {
    assertSameElements(Array[Byte](0, 1, 2, 4), Array[Byte](0, 1, 2, 4))

    assertSameElements(Seq(true, false), Seq(true, false))

    assertSameElements(List("0", "1", "2", "4"), List("0", "1", "2", "4"))

    assertSameElements(
      SortedSet("0", "1", "2", "4"),
      SortedSet("0", "1", "2", "4")
    )

    assertSameElements(
      SortedMap("a" -> 0, "b" -> 1),
      SortedMap("a" -> 0, "b" -> 1)
    )

    assertSameElements(
      mutable.IndexedSeq("a", "b", "c"),
      mutable.IndexedSeq("a", "b", "c")
    )

    assertSameElements(
      mutable.Queue("a", "b", "c"),
      mutable.Queue("a", "b", "c")
    )

    assertSameElements(Seq[Byte](0, 1, 2, 4), Array[Byte](0, 1, 2, 4))

    assertSameElements(Vector(true, false), List(true, false))

    assertSameElements(
      Array("0", "1", "2", "4"),
      IndexedSeq("0", "1", "2", "4")
    )

    assertSameElements(
      SortedMap("a" -> 0, "b" -> 1),
      SortedSet("a" -> 0, "b" -> 1)
    )

    assertSameElements(
      mutable.IndexedSeq("a", "b", "c"),
      IndexedSeq("a", "b", "c")
    )

    assertSameElements(
      mutable.Queue("a", "b", "c"),
      immutable.Queue("a", "b", "c")
    )
  }

  test("An assertion fails on collections with different elements".fail) {
    assertSameElements(Array[Byte](0, 1, 2), Array[Byte](0, 1, 2, 4))

    assertSameElements(Seq(true, false), Seq(true, false))

    assertSameElements(List(), List(0))

    assertSameElements(Set(0, 1, 2, 4), Set())

    assertSameElements(Seq[Any]("0", "1", "2", "4"), Seq[Any]("0", 1, "2", "4"))

    assertSameElements(
      SortedSet("0", "1", "2", "4"),
      SortedSet("0", "1", "4", "2")
    )

    assertSameElements(
      SortedMap("a" -> 0, "b" -> 1),
      SortedSet("b" -> 1, "a" -> 0)
    )

    assertSameElements(
      mutable.IndexedSeq("a", "b", "c"),
      mutable.IndexedSeq("a", "b")
    )

    assertSameElements(mutable.Queue("a", "b", "c"), mutable.Queue("b", "c"))

    assertSameElements(Seq[Byte](0, 1, 2), Array[Byte](0, 1, 2, 4))

    assertSameElements(Vector(true, false), List(true, false))

    assertSameElements(IndexedSeq(), List(0))

    assertSameElements(List(0, 1, 2, 4), IndexedSeq())

    assertSameElements(
      Array[Any]("0", "1", "2", "4"),
      Seq[Any]("0", 1, "2", "4")
    )

    assertSameElements(
      mutable.IndexedSeq("a", "b", "c"),
      immutable.IndexedSeq("a", "b")
    )

    assertSameElements(immutable.Queue("a", "b", "c"), mutable.Queue("b", "c"))
  }
}
