package munit
package scalacheck

import org.scalacheck.Prop
import org.scalacheck.{Test => ScalaCheckTest}
import org.scalacheck.util.Pretty
import scala.concurrent.Future

trait ScalaCheckSuite extends FunSuite {

  def property(
      options: TestOptions
  )(body: => Prop)(implicit loc: Location): Unit = {
    test(options)(body)
  }

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms :+ scalaCheckPropTrasform

  protected def scalaCheckTestParameters = ScalaCheckTest.Parameters.default

  protected def scalaCheckPrettyParameters = Pretty.defaultParams

  private def scalaCheckPropTrasform: ValueTransform =
    new ValueTransform("ScalaCheck Prop", {
      case prop: Prop =>
        val result = ScalaCheckTest.check(scalaCheckTestParameters, prop)
        val prettyResult = Pretty.pretty(result, scalaCheckPrettyParameters)
        if (result.passed) {
          println(prettyResult)
          Future.successful(())
        } else
          Future.failed {
            val e = new Exception("\n" + prettyResult)
            e.setStackTrace(Array.empty)
            e
          }
    })
}
