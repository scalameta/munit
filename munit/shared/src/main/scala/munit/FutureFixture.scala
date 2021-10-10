package munit

import scala.concurrent.Future

/**
 * Extend this class if you want to create a Fixture where all methods have `Future[Any]` as the result type.
 */
abstract class FutureFixture[T](name: String) extends Fixture[Future[T]](name) {
  override def beforeAll(): Future[Any] = Future.successful(())
  override def beforeEach(context: BeforeEach): Future[Any] =
    Future.successful(())
  override def afterEach(context: AfterEach): Future[Any] =
    Future.successful(())
  override def afterAll(): Future[Any] = Future.successful(())
}
