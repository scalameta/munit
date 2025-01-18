package munit

import munit.internal.PlatformCompat

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

abstract class FunSuite extends BaseFunSuite

trait BaseFunSuite
    extends Suite
    with Assertions
    with FunFixtures
    with TestOptionsConversions
    with TestTransforms
    with SuiteTransforms
    with ValueTransforms {
  self =>

  final val munitTestsBuffer: mutable.ListBuffer[Test] = mutable.ListBuffer
    .empty[Test]
  def munitTests(): Seq[Test] = munitSuiteTransform(munitTestsBuffer.toList)

  def test(name: String)(body: => Any)(implicit loc: Location): Unit =
    test(new TestOptions(name))(body)
  def test(options: TestOptions)(body: => Any)(implicit loc: Location): Unit =
    munitTestsBuffer += munitTestTransform(new Test(
      options.name,
      { () =>
        try waitForCompletion(() => munitValueTransform(body))
        catch { case NonFatal(e) => Future.failed(e) }
      },
      options.tags.toSet,
      loc,
    ))

  def munitTimeout: Duration = new FiniteDuration(30, TimeUnit.SECONDS)
  private final def waitForCompletion[T](f: () => Future[T]) = PlatformCompat
    .waitAtMost(f, munitTimeout, munitExecutionContext)

}
