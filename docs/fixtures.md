---
id: fixtures
title: Using fixtures
---

## Local fixtures without mutable state

Implement the `Fixture[T]` trait to configure an environment for each test case.

```scala mdoc:reset
import java.nio.file._
import munit._
class FilesSuite extends FunSuite {
  val files = new Fixture[Path] {
    def beforeEach(context: BeforeEachFixture): Path = {
      // A failure here fails the test.
      Files.createTempFile("files", context.options.name)
    }
    def afterEach(context: AfterEachFixture): Unit = {
      // Always runs even if test fails.
      Files.deleteIfExists(context.argument)
    }
  }

  files.test("exists") { file =>
    // `file` is the temporary file that was created for this test case.
    assert(Files.exists(file))
  }
}
```

Use the `Fixture.map2` combinator to merge two fixtures.

```scala mdoc
class Files2Suite extends FilesSuite {
  val files2 = Fixture.map2(files, files)
  files2.test("not same") { case (file1, file2) =>
    assertNotEquals(file1, file2)
  }
}
```

In Dotty, it's possible to drop `case` from `case (file1, file2) =>` so the
following syntax is valid.

```scala
files2.test("not same") { (file1, file2) =>
  assertNotEquals(file1, file2)
}
```

## Local fixtures with mutable state

Override `beforeEach()` and `afterEach()` to add custom logic that should run
before and after each tests case. For example, use this feature to create
temporary files before executing tests or clean up acquired resources after the
test finish.

```scala mdoc:reset
import java.nio.file._
class MySuite extends munit.FunSuite {
  var path: Path = null

  // Runs before each individual test.
  override def beforeEach(context: BeforeEach): Unit = {
    path = Files.createTempFile("MySuite", context.test.name)
  }

  // Runs after each individual test.
  override def afterEach(context: AfterEach): Unit = {
    Files.deleteIfExists(path)
  }

  test("test1") {
    // ...
    path // will be deleted after this test case finishes
  }
  test("test2") {
    // ...
    path // not the same `path` as in "test1"
  }
}
```

## Global fixtures with mutable state

Override `beforeAll()` and `afterAll()` to add custom logic that should run
before all test cases start runniing and after all tests cases have finished
running. For example, use this feature to establish a database connection that
should be reused between test cases.

```scala mdoc:reset
import java.sql.Connection
import java.sql.DriverManager
class MySuite extends munit.FunSuite {
  var db: Connection = null

  // Runs once before all tests start.
  override def beforeAll(): Unit = {
    // start in-memory database connection.
    db = DriverManager.getConnection("jdbc:h2:mem:", "sa", null)
  }

  // Runs once after all tests have completed, regardless if tests passed or failed.
  override def afterAll(): Unit = {
    db.close()
  }
}
```

## Avoid stateful operations in the class constructor

Test classes may sometimes get initialized even if no tests run so it's best to
avoid declaring fixture in the class constructor instead of `beforeAll()`.

For example, IDEs like IntelliJ may load the class to discover the names of the
test cases that are available.

```scala mdoc:reset
import java.sql.DriverManager
class MySuite extends munit.FunSuite {
  // Don't do this, because the class may get initialized even if no tests run.
  val db = DriverManager.getConnection("jdbc:h2:mem:", "sa", null)

  override def afterAll(): Unit = {
    // May never get called, resulting in connection leaking.
    db.close()
  }
}
```
