package funsuite

/** The base class for all test suites.
  * Extend this class if you don't need the functionality in FunSuite.
  */
abstract class Suite {

  /** The base class for all test suites */
  def funsuiteTests(): Seq[Test]

  /** Runs once before all test cases.
    * An error in this method aborts the test suite.
    */
  def beforeAll(context: BeforeAll): Unit = ()

  /** Runs once after all test cases. */
  def afterAll(context: AfterAll): Unit = ()

  /** Runs before each individual test case.
    * An error in this method aborts the test case.
    */
  def beforeEach(context: BeforeEach): Unit = ()

  /** Runs after each individual test case. */
  def afterEach(context: AfterEach): Unit = ()

}
