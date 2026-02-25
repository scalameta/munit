---
id: troubleshooting
title: Troubleshooting
---

## Print full stack traces

By default, MUnit trims exception stack traces to avoid printing redundant
information to the console. Use the `-F` test framework flag to print full stack
traces, which can be helpful when debugging a cryptic error.

In sbt, test framework flags can be passed to the `testOnly` task.

```sh
$ sbt
> myproject/testOnly -- -F
```

## Control test logging verbosity

Use the `--log=LEVEL` test framework flag to control which test output is
printed. The default is `info`.

- `--log=error` prints failing tests only
- `--log=warn` also prints other non-successful (such as ignored/skipped) tests
- `--log=info` (default) also prints suite-started and test-succeeded events
- `--log=debug` also prints test-started as well as entire-run started/finished events
  - this is equivalent to specifying `--verbose`

This behavior is the same regardless of logger configuration (`--logger=sbt`,
`--logger=buffered`, or `-b` for non-buffered output).

```sh
$ sbt
> myproject/testOnly -- --log=ignored
```

To apply this to all test runs in sbt:

```scala
Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "--log=failure")
```

## Invalid test class

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
