package munit

import org.junit.runner.RunWith

import scala.concurrent.{ExecutionContext, Future}

/**
 * The base class for all test suites.
 * Extend this class if you don't need the functionality in FunSuite.
 */
@RunWith(classOf[MUnitRunner])
abstract class Suite extends PlatformSuite {

  /** The value produced by test bodies. */
  type TestValue
  final type Test = GenericTest[TestValue]
  final type BeforeEach = GenericBeforeEach[TestValue]
  final type AfterEach = GenericAfterEach[TestValue]

  /** The base class for all test suites */
  def munitTests(): Seq[Test]

  /** Functional fixtures that can be reused for individual test cases or entire suites. */
  def munitFixtures: Seq[Fixture[_]] = Nil

  /** Functional asynchronous fixtures that can be reused for individual test cases or entire suites. */
  def munitAsyncFixtures: Seq[AsyncFixture[_]] = Nil

  private val parasiticExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable): Unit = runnable.run()
    def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
  }
  def munitExecutionContext: ExecutionContext = parasiticExecutionContext

  /**
   * @param fixtureName The name of this fixture, used for displaying an error message if
   * `beforeAll()` or `afterAll()` fail.
   */
  abstract class Fixture[T](val fixtureName: String) {

    /** The value produced by this suite-local fixture that can be reused for all test cases. */
    def apply(): T

    /** Runs once before the test suite starts */
    def beforeAll(): Unit = ()

    /**
     * Runs before each individual test case.
     * An error in this method aborts the test case.
     */
    def beforeEach(context: BeforeEach): Unit = ()

    /** Runs after each individual test case. */
    def afterEach(context: AfterEach): Unit = ()

    /** Runs once after the test suite has finished, regardless if the tests failed or not. */
    def afterAll(): Unit = ()

  }

  /**
   * @param fixtureName The name of this fixture, used for displaying an error message if
   * `beforeAll()` or `afterAll()` fail.
   */
  abstract class AsyncFixture[T](val fixtureName: String) {

    /** The value produced by this suite-local fixture that can be reused for all test cases. */
    def apply(): T

    /** Runs once before the test suite starts */
    def beforeAll(): Future[Unit] = Future.successful(())

    /**
     * Runs before each individual test case.
     * An error in this method aborts the test case.
     */
    def beforeEach(context: BeforeEach): Future[Unit] = Future.successful(())

    /** Runs after each individual test case. */
    def afterEach(context: AfterEach): Future[Unit] = Future.successful(())

    /** Runs once after the test suite has finished, regardless if the tests failed or not. */
    def afterAll(): Future[Unit] = Future.successful(())

  }

  /**
   * Runs once before all test cases and before all suite-local fixtures are setup.
   * An error in this method aborts the test suite.
   */
  def beforeAll(): Unit = ()

  /** Runs once after all test cases and after all suite-local fixtures have been tear down. */
  def afterAll(): Unit = ()

  /**
   * Runs before each individual test case.
   * An error in this method aborts the test case.
   */
  def beforeEach(context: BeforeEach): Unit = ()

  /** Runs after each individual test case. */
  def afterEach(context: AfterEach): Unit = ()

}
