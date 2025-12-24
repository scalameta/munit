package munit

import scala.concurrent.{ExecutionContext, Future}

import org.junit.runner.RunWith

/**
 * The base class for all test suites.
 * Extend this class if you don't need the functionality in FunSuite.
 */
@RunWith(classOf[MUnitRunner])
abstract class Suite extends PlatformSuite {

  /** The value produced by test bodies. */
  final type TestValue = Future[Any]
  final type Fixture[T] = munit.Fixture[T]
  final type Test = munit.Test
  final type BeforeEach = munit.BeforeEach
  final type AfterEach = munit.AfterEach

  /** The base class for all test suites */
  def munitTests(): Seq[Test]

  /** Fixtures that can be reused for individual test cases or entire suites. */
  def munitFixtures: Seq[AnyFixture[_]] = Nil

  private val parasiticExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable): Unit = runnable.run()
    def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
  }
  def munitExecutionContext: ExecutionContext = parasiticExecutionContext

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
