---
id: scalatest
title: Coming from ScalaTest
---

# Initiating the Conversion

Begin by ensuring that you have
[added a dependency](./getting-started.html#quick-start) on MUnit.

It is not important to immediately remove the ScalaTest bindings, because SBT
can handle having both test frameworks registered at once, and can run both sets
of tests together. Waiting to remove ScalaTest until the end will help you
making the changes more incrementally, and this can be critical to success if
you have a lot of tests; but even if you have a small number of tests, it can be
beneficial.

# Converting the Tests

If you have left ScalaTest temporarily configured in your build, you can convert
the tests one suite at a time. Each time you convert a suite, you can run all
your tests to ensure you are green.

## Converting Suite Structure

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

If you are coming from `WordSpec` style tests, make sure to flatten them, or your tests
will not run: Only the outer test will be selected to run, and the inner tests will do
nothing. For this reason, watch out for tests that suddenly take almost no time.
Additionally, you may want to deliberate break some of your tests to make sure
they are wired up as you expect (and to get a taste of MUnit's nice assertion
failure output).

```diff
-class FooSpec extends AnyWordSpec with Matchers {
+class FooSpec extends FunSuite {
-  "Foo" must {
-    "succeed" in {
+  test("Foo must succeed") {
...
-    }

```

If your test suites have setup or teardown steps, i.e. if they need to manage
resources, you will need to make use of an MUnit [fixture](./fixtures.html).

## Converting Assertions

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

# Cleaning Up After the Conversion

Once you have converted all test suites to MUnit, you can remove ScalaTest from
your dependencies, e.g.:

```diff
- libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0"
- libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"
```
