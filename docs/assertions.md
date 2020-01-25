---
id: assertions
title: Writing assertions
---

MUnit provides a few ways to fail a test given a condition.

```scala mdoc:invisible
object Tests extends munit.FunSuite {
  override val munitAnsiColors = false
}
import Tests._
```

## `assert()`

Use `assert()` to fail a test if a boolean condition does not hold true. For
example, assume we have two values:

```scala mdoc
val a = 1
val b = 2
```

In the most basic case when no hints are provided, the error message is
"assertion failed" when the condition is false.

```scala mdoc:crash
assert(a > b)
```

Include an optional message to explain why the assertion failed.

```scala mdoc:crash
assert(a > b, "a was smaller than b")
```

Use `clue()` to include optional clues in the boolean condition based on values
in the expression.

```scala mdoc:crash
assert(clue(a) > clue(b))
```

Clues can wrap more complicated expressions.

```scala mdoc:crash
assert(clue(List(a).head) > clue(b))
```

## `assertEquals()`

Use `assertEquals()` to assert that two values are the same.

```scala mdoc:crash
assertEquals(a, b)
```

The error message automatically produces a diff on assertion failure.

```scala mdoc
case class Library(name: String, awesome: Boolean, versions: Range = 0.to(1))
val munitLibrary = Library("MUnit", true)
val mdocLibrary = Library("MDoc", true)
```

```scala mdoc:crash
assertEquals(munitLibrary, mdocLibrary)
```

Diffs make it easy to track down differences even in large data structures.

```scala mdoc:crash
assertEquals(
  Map(1 -> List(1.to(3))),
  Map(1 -> List(1.to(4)))
)
```

Comparing two values of different types is a compile error.

```scala mdoc:fail
assertEquals(1, "")
```

The two types must match exactly, it's a type error to compare two values even
if one value is a subtype of the other.

```scala mdoc:fail
assertEquals(Some(1), Option(1))
```

Upcast the subtype using a type ascription `subtype: Supertype` when you want to
compare a subtype with a supertype.

```scala mdoc
// OK
assertEquals(Some(1): Option[Int], Option(1))
```

## `assertNotEquals()`

Use `assertNotEqual()` to assert that two values are not the same.

```scala mdoc:crash
assertNotEquals(a, a)
```

The assertion does not fail when both values are different.

```scala mdoc
// OK
assertNotEquals(a, b)
```

## `fail()`

Use `fail()` to make the test case fail immediately.

```scala mdoc:crash
fail("test failed")
```

Use `clues()` to include optional context why the test failed.

```scala mdoc:crash
fail("test failed", clues(a + b))
```
