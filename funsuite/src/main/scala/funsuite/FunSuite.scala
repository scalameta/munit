package funsuite

import scala.collection.mutable
import funsuite.internal.StackMarker
import java.nio.file.Path
import funsuite.internal.Lines
import fansi.Str
import java.nio.file.Paths
import java.nio.file.Files
import scala.util.control.NonFatal
import scala.collection.JavaConverters._
import fansi.Reversed
import org.junit.Assert
import scala.util.Try
import fansi.Color
import scala.util.Failure
import scala.util.Success

abstract class FunSuite
    extends Suite
    with Assertions
    with TestOptionsConversions {

  val funsuiteTestsBuffer = mutable.ArrayBuffer.empty[Test]

  def funsuiteTests(): Seq[Test] = {
    val onlyTests = funsuiteTestsBuffer.filter(_.tags(Only))
    if (onlyTests.nonEmpty) onlyTests.toSeq
    else funsuiteTestsBuffer.toSeq
  }

  def test(name: String, tag: Tag*)(
      body: => Any
  )(implicit loc: Location): Unit = {
    test(new TestOptions(name, tag.toSet, loc))(body)
  }

  def test(options: TestOptions)(
      body: => Any
  )(implicit loc: Location): Unit = {
    funsuiteTestsBuffer += new Test(
      options.name,
      () => funsuiteRunTest(options, StackMarker.dropOutside(body)),
      options.tags.toSet,
      loc
    )
  }

  def isCI: Boolean = "true" == System.getenv("CI")

  def isFlakyFailureOk: Boolean = "true" == System.getenv("FUNSUITE_FLAKY_OK")

  def funsuiteFlaky(
      options: TestOptions,
      body: => Any
  ): Any = {
    val result = Try(body)
    result match {
      case Success(value) => value
      case Failure(exception) =>
        if (isFlakyFailureOk) {
          new FlakyFailure(exception)
        } else {
          throw exception
        }
    }
  }

  def funsuiteExpectFailure(
      options: TestOptions,
      body: => Any
  ): Any = {
    val result = scala.util.Try(body)
    if (result.isSuccess) {
      fail(
        locatedDetails(options.loc, "expected failure but test passed").render
      )
    }
  }

  def funsuiteRunTest(
      options: TestOptions,
      body: => Any
  ): Any = {
    if (options.tags(Fail)) {
      funsuiteExpectFailure(options, body)
    } else if (options.tags(Flaky)) {
      funsuiteFlaky(options, body)
    } else if (options.tags(Ignore)) {
      Ignore
    } else {
      body
    }
  }

}
