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

class FunSuite extends Assertions with TestOptionsConversions {

  private[funsuite] val tests = mutable.ArrayBuffer.empty[Test]

  def funsuiteName: String = this.getClass().getCanonicalName()

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
    if (options.tags(ExpectFailure)) {
      funsuiteExpectFailure(options, body)
    } else if (options.tags(Flaky)) {
      funsuiteFlaky(options, body)
    } else if (options.tags(Ignore)) {
      Ignore
    } else {
      body
    }
  }

  def beforeAll(context: BeforeAll): Unit = ()
  def afterAll(context: AfterAll): Unit = ()

  def beforeEach(context: BeforeEach): Unit = ()
  def afterEach(context: AfterEach): Unit = ()

  def test(name: String, tag: Tag*)(
      body: => Any
  )(implicit loc: Location): Unit = {
    test(new TestOptions(name, tag.toSet, loc))(body)
  }

  def test(options: TestOptions)(
      body: => Any
  )(implicit loc: Location): Unit = {
    tests += new Test(
      options.name,
      () => funsuiteRunTest(options, StackMarker.dropOutside(body)),
      options.tags.toSet,
      loc
    )
  }
}
