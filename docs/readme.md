# MUnit

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

**Table of contents**

## Getting started

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

## Features

### Source locations for assertion errors

Assertion errors show the source code location where the assertion failed. Use
cmd+click on the location "`/path/to/BasicSuite.scala:36`" to open the exact
line number in your editor (may not work in all terminals).

![Source locations for assertion errors](https://i.imgur.com/6qhmz5F.png)

### Highlighted stack traces

Stack frame elements for classes that are defined in your project sources are
highlighted so you can focus on the important parts of the stack trace.

![Highlighted stack traces example](https://i.imgur.com/C6m6PbT.png)

### Multiline string diff

Use `assertNoDiff(obtained, expected)` to compare large multi-line strings.

![Multiline string diff](https://i.imgur.com/VY79UXX.png)

Test failures include the obtained multiline string in a copy-paste friendly
format making it easy to update the test as the expected behavior of your
program changes.

## Usage

### Running logic before and after tests

Override `beforeAll()`, `beforeEach()`, `afterAll()` and `afterEach()` to add
custom logic that should run before and after tests run. For example, use this
feature to create temporary files before executing tests or clean up acquired
resources after the test finish.

```scala
class MySuite extends munit.FunSuite {
  // Runs once before all tests start.
  override def beforeAll(): Unit = ???
  // Runs before each individual test.
  override def beforeEach(): Unit = ???
  // Runs after each individual test.
  override def afterEach(context: AfterEach): Unit = ???
  // Runs once after all tests have completed.
  override def afterAll(context: AfterAll): Unit = ???
}
```

### Skip test based on dynamic conditions

Use `assume(condition, explanation)` to skip tests when some conditions do not
hold. For example, use `assume` to conditionally run tests based on the
operating system or the Scala compiler version.

```scala
import scala.util.Properties
  test("paths") {
    assume(Properties.isLinux, "this test runs only on Linux")
    assume(Properties.versionNumberString.startsWith("2.13"), "this test runs only on Scala 2.13")
  }
```

### Failing tests

Use `.fail` to mark a test case that is expected to fail.

```scala
  test("issue-456".fail) {
    // Reproduce reported bug
  }
```

A failed test only succeeds if the test body fails. If the test body succeeds,
the test fails.

### Running individual test

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

Use `testOnly -- $GLOB` to filter a fully qualified test name from the command
line.

```sh
# sbt shell
> testOnly -- *issue-456
```

Use `testOnly -- --only=$TEST_FILTER` to filter an individual test name from the
command line.

```sh
# sbt shell
> testOnly -- --only=issue-456
```

### Ignore tests

Use the `@Ignore` annotation to skip all tests in a test suite.

```scala
@munit.Ignore
class MySuite extends munit.FunSuite {
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

Override `munitIgnore: Boolean` to skip a test suite based on a dynamic
condition.

```scala
class MyWindowsSuite extends munit.FunSuite {
  override def munitIgnore: Boolean = !scala.util.Properties.isWin
  test("windows-only") {
    // only runs on Windows
  }
}
```

### Using JUnit categories

Use `@Category(...)` to group tests suites together.

```scala
package myapp
import org.junit.experimental.categories.Category

class Slow extends munit.Tag("Slow")
class Fast extends munit.Tag("Fast")

@Category(Array(classOf[Slow]))
class MySlowSuite extends munit.FunSuite {
  test("slow") {
    Thread.sleep(1000)
  }
  // ...
}
@Category(Array(classOf[Slow], classOf[Fast]))
class MySlowFastSuite extends munit.FunSuite {
  // ...
}
@Category(Array(classOf[Fast]))
class MyFastSuite extends munit.FunSuite {
  // ...
}
```

Next, use `--include-category=$CATEGORY` and `--exclude-category=$CATEGORY` to
determine what test suites to run from the command line.

```sh
# matches: MySlowSuite, MySlowFastSuite
> testOnly -- --include-category=myapp.Slow

# matches: MySlowSuite
> testOnly -- --include-category=myapp.Slow --exclude-category=myapp.Fast
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

### Searching for failed tests in large log files

Test results are formatted in a specific way to make it easy to search for them
in a large log file.

| Test    | Prefix  |
| ------- | ------- |
| Success | `+`     |
| Failed  | `==> X` |
| Ignored | `==> i` |
| Skipped | `==> s` |

Knowing these prefixes may come in handy for example when browsing test logs in
a browser. Search for "==> X" to quickly navigate to the failed tests.

### Running tests in IntelliJ

MUnit test suites can be executed from in IntelliJ like normal test suites.

![Running MUnit from IntelliJ](https://i.imgur.com/oAA2ZeQ.png)

It's expected that it's not possible to run individual test cases from IntelliJ
since it does not understand the structure of the `test("name") {...}` syntax.
As a workaround, use the `.only` marker to run only a single test from IntelliJ.

```diff
- test("name") {
+ test("name".only) {
    // ...
  }
```

## Troubleshooting

### Invalid test class

If you define a test suite as an `object` instead of `class` you get the
following error:

```sh
==> X munit.BasicSuite.initializationError  0.003s org.junit.runners.model.InvalidTestClassError: Invalid test class 'munit.BasicSuite':
  1. Test class should have exactly one public constructor
  2. No runnable methods
```

To fix the problem, use `class` instead of `object`

```diff
- object MySuite extends munit.FunSuite { ... }
+ class MySuite extends munit.FunSuite { ... }
```

## Tests as values

MUnit test cases are represented with the type `munit.Test` and can be
manipulated as a normal data structure. Feel free to tweak and extend how you
generate `munit.Test` to suit your needs.

### Extend `Suit`

Extend the base class `munit.Suite` to customize exactly what `Seq[Test]` you
want to run.

```scala
class MyCustomSuite extends munit.Suite {
  // The type returned by bodies of test cases.
  // Is defined as `Any` in `munit.FunSuite` but it's abstract in `munit.Suite`
  override type TestValue = Future[String]
  override def munitTests() = List(
    new Test(
      "name",
      // compile error if it's not a Future[String]
      body = () => Future.successful("Hello world!"),
      tags = Set.empty[Tag],
      location = Location.generate
    )
  )
}
```

The abstract `munit.Suite` class only includes the before/after APIs and not
other methods like `assert` or `test()`.

### Customize evaluation of tests

Override `munitRunTest()` to extend the default behavior for how test bodies are
evaluated. For example, use this feature to implement a `Rerun(N)` modifier to
evaluate the body multiple times.

```scala
import scala.util.Properties
case class Rerun(count: Int) extends Tag("Rerun")
class MyWindowsSuite extends munit.FunSuite {
  override def munitRunTest(options: TestOptions, body: => Any): Any = {
    val rerunCount = options.tags.collectFirst {
      case Rerun(n) => n
    }.getOrElse(1)
    1.to(rerunCount).map(_ => super.munitRunTest(options, body))
  }
  test("files", Rerun(10)) {
    println("Hello") // will run 10 times
  }
  test("files") {
    // will run once, like normal
  }
}
```

### Filter tests based on dynamic conditions

Override `munitTests()` to customize what tests get executed. For example, use
this feature to skip tests based on a dynamic condition.

```scala
import scala.util.Properties
case object Windows extends munit.Tag("Windows")
class MyWindowsSuite extends munit.FunSuite {
  override def munitTests(): Any = {
    val default = super.munitTests()
    if (!Properties.isWin) default
    else default.filter(_.tags.contains(Windows))
  }
  test("files", Windows) {
    // will only run in Windows
  }
  test("files") {
    // will run like normal
  }
}
```

## Limitations

**JVM-only**: MUnit is currently only published for the JVM. MUnit uses a
[JUnit testing interface](https://github.com/olafurpg/junit-interface) for sbt
that's written in Java so that would need to be changed in order to add Scala.js
and Scala Native support. Feel free to open an issue if you would like to
contribute cross-platform support.

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

## Do we really need another testing library?

MUnit is built on the idea that >90% of what a JVM testing library needs is
already provided by JUnit. However, the default JUnit testing syntax is based on
annotations and does not feel idiomatic when used from Scala. MUnit tries to
fill in the gap by providing a small Scala API on top of JUnit.

## Stability

MUnit is a new library with no stability guarantees. It's expected that new
releases, including patch releases, will have binary and source breaking
changes.
