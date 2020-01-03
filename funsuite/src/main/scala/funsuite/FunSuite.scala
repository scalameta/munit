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

class FunSuite extends Assertions with TestOptionsConversions {

  private[funsuite] val tests = mutable.ArrayBuffer.empty[Test]

  def funsuiteFlaky(
      options: TestOptions,
      body: => Any
  ): Any = {
    try body
    catch {
      case NonFatal(_) =>
        println(s"retrying flaky test ${options.name}")
        body
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

  def funsuiteExecuteBody(
      options: TestOptions,
      body: => Any
  ): Any = {
    if (options.tags(Tag.ExpectFailure)) {
      funsuiteExpectFailure(options, body)
    } else if (options.tags(Tag.Flaky)) {
      funsuiteFlaky(options, body)
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
      () => funsuiteExecuteBody(options, StackMarker.dropOutside(body)),
      options.tags.toSet,
      loc
    )
  }
}
