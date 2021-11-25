---
id: getting-started
title: Getting started
---

MUnit is a Scala testing library with the following goals:

- **Reuse JUnit**: MUnit is implemented as a JUnit runner and tries to build on
  top of existing JUnit functionality where possible. Any tool that knows how to
  run a JUnit test suite knows how to run MUnit, including IDEs like IntelliJ.
- **Actionable errors**: test reports are pretty-printed with colors to help you
  quickly understand what caused a test failure. MUnit tries to displays diffs
  and source locations when possible and it does a best-effort to highlight
  relevant stack trace elements.
- **No Scala dependencies**: MUnit has no external Scala dependencies so that it
  can easily cross-build against a wide range of Scala compiler versions.
- **Cross-platform**: MUnit compiles to JVM bytecode, JavaScript via Scala.js
  and ahead-of-time optimized binaries via Scala Native/LLVM.

## Quick start

![Badge with version of the latest release](https://img.shields.io/maven-central/v/org.scalameta/munit_2.13?style=for-the-badge)

**sbt:**

```scala
libraryDependencies += "org.scalameta" %% "munit" % "@STABLE_VERSION@" % Test
// Use %%% for non-JVM projects.
```

If you are using a version of sbt lower than 1.5.0, you will also need to add:

```scala
testFrameworks += new TestFramework("munit.Framework")
```

**Mill**

```scala
object test extends Tests {
  def ivyDeps =
    Agg(
      ivy"org.scalameta::munit::@STABLE_VERSION@"
    )

  def testFrameworks = Seq("munit.Framework")
}
```

| Scala Version | JVM | Scala.js (0.6.x) | Scala.js (1.x) | Native (0.4.x) |
| ------------- | :-: | :--------------: | :------------: | :------------: |
| 2.11.x        | ✅  | ✅ until 0.7.16  |       ✅       |       ✅       |
| 2.12.x        | ✅  | ✅ until 0.7.16  |       ✅       |       ✅       |
| 2.13.x        | ✅  | ✅ until 0.7.16  |       ✅       |       ✅       |
| 3.0.x         | ✅  |       n/a        |       ✅       |      n/a       |

Next, write a test suite.

```scala mdoc
class MySuite extends munit.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
}
```

### Run tests in sbt

Execute `sbt test` to run MUnit tests in the terminal. It's recommended to stay
in the sbt shell for the best compiler performance.

```sh
$ sbt
> test
```

Use `testOnly` to run only a single test suite in the sbt shell.

```sh
# sbt shell
> testOnly com.MySuite
```

### Run tests in IntelliJ

MUnit test suites can be executed from in IntelliJ like normal test suites.

![Running MUnit from IntelliJ](https://i.imgur.com/oAA2ZeQ.png)

It's possible to run individual test cases from IntelliJ by clicking on the
green "Play" icon in the left gutter. Alternatively, you can also use the
`.only` marker to run an individual test.

```diff
- test("name") {
+ test("name".only) {
    // ...
  }
```

### Run tests in VS Code

MUnit test suites can be executed from VS Code like normal test suites.

![Running MUnit from VS Code](https://i.imgur.com/hmL0hAp.png)

### Search for failed tests in CI logs

Test results are formatted in a specific way to make it easy to search for them
in a large log file.

| Test    | Prefix  |
| ------- | ------- |
| Success | `+`     |
| Failed  | `==> X` |
| Ignored | `==> i` |
| Skipped | `==> s` |

Knowing these prefixes may come in handy for example when browsing test logs in
a browser. Search for `==> X` to quickly navigate to the failed tests.

## Usage guide

See the guide on [writing tests](tests.html) to learn more about using MUnit.

## Why JUnit?

MUnit builds on top of JUnit in order to benefit from existing JUnit tooling
integrations. For example, IntelliJ can already automatically detect JUnit test
suites and provides a nice interface to explore JUnit test results. Some build
tools like Pants also have built-in support to JUnit test suites.

## Why not just JUnit?

The default JUnit testing syntax is based on annotations and does not feel
idiomatic when used from Scala. MUnit tries to fill in the gap by providing a
small Scala API on top of JUnit.

## Stability

MUnit is a new library with no stability guarantees. While this project is
versioned at v0.x, it's expected that new releases, including patch releases,
will have binary and source breaking changes.

## Inspirations

MUnit is inspired by several existing testing libraries:

- ScalaTest: the syntax for defining `munit.FunSuite` test suites is the same as
  for `org.scalatest.FunSuite`.
- JUnit: MUnit is implemented as a custom JUnit runner and features like
  `assume` test filters are implemented on top of existing JUnit functionality.
- utest: the nicely formatted stack traces and test reports is heavily inspired
  by the beautifully formatted output in utest.
- ava: the idea for showing the source locations for assertion errors comes from
  [ava](https://github.com/avajs/ava), a JavaScript testing library.
