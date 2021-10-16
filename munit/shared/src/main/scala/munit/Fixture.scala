package munit

/**
 * Fixture allows you to acquire resources during setup and clean up resources after the tests finish running.
 *
 * Fixtures can be local to a single test case by overriding `beforeEach` and
 * `afterEach`, or they can be re-used for an entire test suite by extending
 * `beforeAll` and `afterAll`.
 *
 * There is no functional difference between extending `Fixture[T]` or
 * `AnyFixture[T]`. The only difference is that an IDE will auto-complete `Unit`
 * in the result type instead of `Any`.
 *
 * @see https://scalameta.org/munit/docs/fixtures.html
 * @param fixtureName The name of this fixture, used for displaying an error message if
 * `beforeAll()` or `afterAll()` fail.
 */
abstract class Fixture[T](name: String) extends AnyFixture[T](name) {
  override def beforeAll(): Unit = ()
  override def beforeEach(context: BeforeEach): Unit = ()
  override def afterEach(context: AfterEach): Unit = ()
  override def afterAll(): Unit = ()
}
