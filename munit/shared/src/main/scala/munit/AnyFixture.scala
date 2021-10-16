package munit

/**
 * AnyFixture allows you to acquire resources during setup and clean up resources after the tests finish running.
 *
 * Fixtures can be local to a single test case by overriding `beforeEach` and
 * `afterEach`, or they can be re-used for an entire test suite by extending
 * `beforeAll` and `afterAll`.
 *
 * It's preferable to use a sub-class like `munit.Fixture` or
 * `munit.FutureFixture` instead of this class.  Extend this class if you're
 * writing an integration a third-party type like Cats `Resource`.
 *
 * @see https://scalameta.org/munit/docs/fixtures.html
 * @param fixtureName The name of this fixture, used for displaying an error message if
 * `beforeAll()` or `afterAll()` fail.
 */
abstract class AnyFixture[T](val fixtureName: String) {

  /** The value produced by this suite-local fixture that can be reused for all test cases. */
  def apply(): T

  /** Runs once before the test suite starts */
  def beforeAll(): Any = ()

  /**
   * Runs before each individual test case.  An error in this method aborts the test case.
   */
  def beforeEach(context: BeforeEach): Any = ()

  /** Runs after each individual test case. */
  def afterEach(context: AfterEach): Any = ()

  /** Runs once after the test suite has finished, regardless if the tests failed or not. */
  def afterAll(): Any = ()

}
