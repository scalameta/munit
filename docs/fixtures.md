---
id: fixtures
title: Using fixtures
---

Test fixtures are the environments in which tests run. Fixtures allow you to
acquire resources during setup and clean up resources after the tests finish
running.

## Functional test-local fixtures

Functional test-local fixtures allow you to write test cases with simple
setup/teardown methods to initialize resources before a test case and clean up
resources after a test case.

```scala mdoc:reset
import java.nio.file._
class FunFixtureSuite extends munit.FunSuite {
  val files = FunFixture[Path](
    setup = { test =>
      Files.createTempFile("tmp", test.name)
    },
    teardown = { file =>
      // Always gets called, even if test failed.
      Files.deleteIfExists(file)
    }
  )

  files.test("basic") { file =>
    assert(Files.isRegularFile(file), s"Files.isRegularFile($file)")
  }
}
```

```scala mdoc:invisible
val tests = new FunFixtureSuite()
import tests._
```

Use `FunFixture.map2` to compose multiple fixtures into a single fixture.

```scala mdoc
// Fixture with access to two temporary files.
val files2 = FunFixture.map2(files, files)
files2.test("two") {
  case (file1, file2) =>
    assertNotEquals(file1, file2)
    assert(Files.isRegularFile(file1), s"Files.isRegularFile($file1)")
    assert(Files.isRegularFile(file2), s"Files.isRegularFile($file2)")
}
```

Functional test-local fixtures are desirable since they are easy to reason
about. Try to use functional test-local fixtures when possible, and only resort
to reusable or ad-hoc fixtures when necessary.

## Reusable test-local fixtures

Reusable test-local fixtures are more powerful than functional test-local
fixtures because they can declare custom logic that gets evaluated before each
local test case and get torn down after each test case. These increased
capabilities come at the price of ergonomics of the API.

Override the `beforeEach()`, `afterEach()` and `munitFixtures` methods in the 
`Fixture[T]` trait to configure a reusable test-local fixture.

```scala mdoc:reset
import java.nio.file._
import munit._
class FilesSuite extends FunSuite {
  val file = new Fixture[Path]("files") {
    var file: Path = null
    def apply() = file
    override def beforeEach(context: BeforeEach): Unit = {
      file = Files.createTempFile("files", context.test.name)
    }
    override def afterEach(context: AfterEach): Unit = {
      // Always gets called, even if test failed.
      Files.deleteIfExists(file)
    }
  }
  override def munitFixtures = List(file)

  test("exists") {
    // `file` is the temporary file that was created for this test case.
    assert(Files.exists(file()))
  }
}
```

Use `AsyncFixture[T]` for fixtures that need to be loaded asynchronously. 
Override the `munitAsyncFixtures` method to register async fixtures.

```scala mdoc:reset
import java.nio.file._
import munit._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
class AsyncFilesSuite extends FunSuite {
  val file = new AsyncFixture[Path]("files") {
    var file: Path = null
    def apply() = file
    override def beforeEach(context: BeforeEach): Future[Unit] = Future {
      file = Files.createTempFile("files", context.test.name)
    }
    override def afterEach(context: AfterEach): Future[Unit] = Future {
      // Always gets called, even if test failed.
      Files.deleteIfExists(file)
    }
  }
  override def munitAsyncFixtures = List(file)

  test("exists") {
    // `file` is the temporary file that was created for this test case.
    assert(Files.exists(file()))
  }
}
```

## Reusable suite-local fixtures

Reusable suite-local fixtures work the same as reusable test-local fixtures but
they override the `beforeAll()` and `afterAll()` methods instead of
`beforeEach()` and `afterEach()`.

```scala mdoc:reset
import java.sql.Connection
import java.sql.DriverManager
class MySuite extends munit.FunSuite {
  val db = new Fixture[Connection]("database") {
    private var connection: Connection = null
    def apply() = connection
    override def beforeAll(): Unit = {
      connection = DriverManager.getConnection("jdbc:h2:mem:", "sa", null)
    }
    override def afterAll(): Unit = {
      connection.close()
    }
  }
  override def munitFixtures = List(db)

  test("test1") {
    db() // database connection has been initialized
  }
  test("test2") {
    // ...
    db() // the same `db` instance as in "test1"
  }
}
```

Use `AsyncFixture[T]` for fixtures that need to be loaded asynchronously. 
Override the `munitAsyncFixtures` method to register async fixtures.

```scala mdoc:reset
import java.sql.Connection
import java.sql.DriverManager
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
class AsyncFixtureMySuite extends munit.FunSuite {
  val db = new AsyncFixture[Connection]("database") {
    private var connection: Connection = null
    def apply() = connection
    override def beforeAll(): Future[Unit] = Future {
      connection = DriverManager.getConnection("jdbc:h2:mem:", "sa", null)
    }
    override def afterAll(): Future[Unit] = Future {
      connection.close()
    }
  }
  override def munitAsyncFixtures = List(db)

  test("test1") {
    db() // database connection has been initialized
  }
  test("test2") {
    // ...
    db() // the same `db` instance as in "test1"
  }
}
```

## Ad-hoc test-local fixtures

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

## Ad-hoc suite-local fixtures

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
