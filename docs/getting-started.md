---
id: getting-started
title: Getting started
---

MUnit is a Scala testing library with the following goals:

- **Reuse JUnit**: MUnit is implemented as a JUnit runner and tries to build on
  top of existing JUnit functionality where possible. Any tool that knows how to
  run a JUnit test suite knows how to run MUnit, including IDEs like IntelliJ.
- **Helpful console output**: test reports are pretty-printed with colors to
  help you quickly understand what caused a test failure. MUnit tries to
  displays diffs and source locations when possible and it does a best-effort to
  highlight relevant stack trace elements.
- **No Scala dependencies**: in order to cross-build MUnit against a wide range
  of Scala compiler versions, MUnit has no external Scala dependencies.
- **Cross-platform**: MUnit compiles to JVM bytecode, JavaScript via Scala.js
  and ahead-of-time optimized binaries via Scala Native/LLVM.

## Quick start

![Badge with version of the latest release](https://img.shields.io/maven-central/v/org.scalameta/munit_2.13?style=for-the-badge)

```scala
libraryDependencies += "org.scalameta" %% "munit" % "@VERSION@"
// Use %%% for non-JVM projects.
testFrameworks += new TestFramework("munit.Framework")
```

| Scala Version | JVM | Scala.js (0.6.x, 1.x) | Native (0.4.x) |
| ------------- | :-: | :-------------------: | :------------: |
| 2.11          | ✅  |          ✅           |       ✅       |
| 2.12          | ✅  |          ✅           |      n/a       |
| 2.13          | ✅  |          ✅           |      n/a       |
| 0.21          | ✅  |          n/a          |      n/a       |

Next, write a test suite.

```scala mdoc
class MySuite extends munit.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEqual(obtained, expected)
  }
}
```

## Usage

See the [usage guide](usage.md).

## Why JUnit?

MUnit builds on top of JUnit in order to benefit from existing JUnit tooling
integrations. For example, IntelliJ can already automatically detect JUnit test
suites and provides a great interface to explore JUnit test results. Some build
tools like Pants support running JUnit tests out-of-the-box.

However, the default JUnit testing syntax is based on annotations and does not
feel idiomatic when used from Scala. MUnit tries to fill in the gap by providing
a small Scala API on top of JUnit.

## Stability

MUnit is a new library with no stability guarantees. While this project is in
the v0.x series, it's expected that new releases, including patch releases, will
have binary and source breaking changes.

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
