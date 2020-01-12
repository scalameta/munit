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
- **No Scala dependencies**: MUnit is implemented in ~1k lines of Scala code
  with no external Scala dependencies. The transitive Java dependencies weigh in
  total ~500kb, which is mostly just JUnit.

## Quick start

![Badge with version of the latest release](https://img.shields.io/maven-central/v/org.scalameta/munit_2.13?style=for-the-badge)

```scala
// Published for 2.11, 2.12 and 2.13. JVM-only.
libraryDependencies += "org.scalameta" %% "munit" % "VERSION"
testFrameworks += new TestFramework("munit.Framework")
```

Next, write a test suite.

```scala
class MySuite extends munit.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEqual(obtained, expected)
  }
}
```
