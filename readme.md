# FunSuite

FunSuite is a Scala testing library with the following distinctive features:

- It's implemented as a JUnit runner. Any tools that knows how to run a JUnit
  test suite knows how to run FunSuite, including IDEs like IntelliJ.
- Nicely formatted console output with a focus on diffs, showing source
  locations and highlighting relevant stack traces.

## Usage

![Badge with version of the latest release](https://img.shields.io/maven-central/v/com.geirsson/funsuite_2.13?style=for-the-badge)

```scala
// Published for 2.11, 2.12 and 2.13, JVM-only.
libraryDependencies += "com.geirsson" %% "funsuite" % "VERSION"
```

Next, write a test suite.

```scala
class MySuite extends funsuite.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
}
```

### Source locations for assertion errors

Assertion errors show the source code location where the assertion failed. Use
cmd+click on the location "`/path/to/BasicSuite.scala:36`" to open the exact
line number in your editor (may not work in all terminals).

![Source locations for assertion errors](https://i.imgur.com/6qhmz5F.png)

### Highlighted stack traces

Stack frame elements from your project sources are highlighted so you can focus
on the most important parts of the stack trace.

![Highlighted stack traces example](https://i.imgur.com/C6m6PbT.png)

### Multiline string diff

Use `assertNoDiff` to compare large multi-line strings.

![Multiline string diff](https://i.imgur.com/VY79UXX.png)

Test failures include the obtained multiline string in a copy-paste friendly
format making it easy to update the test as the expected behavior of your
program changes.

### Skip test based on dynamic conditions

Use `assume` to skip tests when some conditions do not hold. For example, use
this to conditionally run tests based on the operating system or the Scala
compiler version.

```scala
  test("paths") {
    assume(Properties.isLinux, "this test runs only on Linux")
    // Linux-specific assertions
  }
```

### Tag flaky tests

Use `.flaky` to mark a test case that has a tendendency to fail sometimes.

```scala
  test("requests".flaky) {
    // I/O heavy tests that sometimes fail
  }
```

By default, flaky tests fail unless the `FUNSUITE_FLAKY_OK` environment variable
is set to `true`. Override the `isFlakyFailureOk` method to customize when it's
OK for flaky tests to fail.

### Failing tests

Use `.fail` to mark a test case that is expected to fail.

```scala
  test("issue-456".fail) {
    // Reproduce reported bug
  }
```

A failed test only succeeds if the test body fails. If the test body succeeds,
the test fails.

### Running individual tests

Use `.only` to run only a single test.

```scala
  test("issue-457") {
    // will not run
  }
  test("issue-456".only) {
    // only test that runs
  }
  test("issue-455") {
    // will not run
  }
```

### Ignore tests

Use the `@Ignore` annotation to skip all tests in a test suite.

```scala
@funsuite.Ignore
class MySuite extends funsuite.FunSuite {
  test("hello1") {
    // will not run
  }
  test("hello2") {
    // will not run
  }
  // ...
}
```

Use `.ignore` to skip an individual test case in a test suite.

```scala
  test("issue-456".ignore) {
    // will not run
  }
```

## JVM-only

FunSuite is currently only published for the JVM. It's unlikely that FunSuite
will get published for Scala.js or Scala Native unless somebody reimplements the
[JUnit testing interface](https://github.com/olafurpg/junit-interface) for sbt,
which is currently written in Java.

## Inspirations

FunSuite is inspired by several existing testing libraries:

- ScalaTest: the syntax for defining FunSuite test suites is the same as for
  `org.scalatest.FunSuite`.
- JUnit: FunSuite is implemented as a custom JUnit runner and features like
  `assume` and tags are implemented on top of existing JUnit functionality.
- utest: the nicely formatted stack traces and test reports is heavily inspired
  by the beautifully formatted output in utest.
- ava: the idea for showing the source locations for assertion errors comes from
  [ava](https://github.com/avajs/ava), a JavaScript testing library.

## Changelog

### 0.1.2 (Jan 6th, 2020)

- Add support for `@Ignore` annotation

### 0.1.1 (Jan 6th, 2020)

- Add support for Scala 2.11.

### 0.1.0 (Jan 6th, 2020)

- Initial release with basic functionality.
- Expect breaking changes.

```

```
