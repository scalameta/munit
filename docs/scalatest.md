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
