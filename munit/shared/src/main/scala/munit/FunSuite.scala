package munit

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.control.NonFatal

abstract class FunSuite
    extends Suite
    with Assertions
    with FunFixtures
    with TestOptionsConversions
    with TestTransforms
    with SuiteTransforms
    with ValueTransforms { self =>

  final type TestValue = Future[Any]

  final val munitTestsBuffer: mutable.ListBuffer[Test] =
    mutable.ListBuffer.empty[Test]
  def munitTests(): Seq[Test] = {
    munitSuiteTransform(munitTestsBuffer.toList)
  }

  def test(name: String)(body: => Any)(implicit loc: Location): Unit = {
    test(new TestOptions(name, Set.empty, loc))(body)
  }
  def test(options: TestOptions)(body: => Any)(implicit loc: Location): Unit = {
    munitTestsBuffer += munitTestTransform(
      new Test(
        options.name, { () =>
          try {
            munitValueTransform(body)
          } catch {
            case NonFatal(e) =>
              Future.failed(e)
          }
        },
        options.tags.toSet,
        loc
      )
    )
  }

}
