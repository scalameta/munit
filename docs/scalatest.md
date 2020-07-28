---
id: scalatest
title: Coming from ScalaTest
---

If you only use basic ScalaTest features, you should be able to replace usage of
`org.scalatest.FunSuite` with minimal changes like below.

```diff
- import org.scalatest.munit.AnyFunSuite
- import org.scalatest.FunSuite
+ import munit.FunSuite

- class MySuite extends FunSuite with BeforeAll with AfterAll {
+ class MySuite extends FunSuite {
  test("name") {
    // unchanged
  }

- ignore("ignored") {
+ test("ignored".ignore) {
    // unchanged
  }
```

Optionally, replace usage of `assert(a == b)` with `assertEquals(a, b)` to
improve the error message in failed assertions:

```diff
- assert(a == b)
+ assertEquals(a, b)
```

Or if you are using match-style assertions via `with Matchers`, change the
assertions like so:

```diff
- result shouldEqual Some(Name(first, last))
+ assertEquals(result, Some(Name(first, last)))
```

MUnit doesn't ship with a large set of assertions, so some fancier matchers will
have to be converted to a simple assertion:

```diff
- myList should have size 2
+ assertEquals(myList.length, 2)
```

If you are coming from `WordSpec` style tests, make sure to flatten them, or your tests
will not run. (Only the outer test will be selected to run, and the inner tests will do
nothing).

```diff
-class FooSpec extends AnyWordSpec with Matchers {
+class FooSpec extends FunSuite {
-  "Foo" must {
-    "succeed" in {
+  test("Foo must succeed") {
...
-    }

```
