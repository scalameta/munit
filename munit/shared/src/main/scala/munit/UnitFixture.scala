package munit

/**
 * Extend this class if you want to create a Fixture where all methods have `Unit` as the result type.
 */
abstract class UnitFixture[T](name: String) extends Fixture[T](name) {
  override def beforeAll(): Unit = ()
  override def beforeEach(context: BeforeEach): Unit = ()
  override def afterEach(context: AfterEach): Unit = ()
  override def afterAll(): Unit = ()
}
