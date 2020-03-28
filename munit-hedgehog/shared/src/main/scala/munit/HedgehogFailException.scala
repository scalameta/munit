package munit

import scala.util.control.NoStackTrace

import hedgehog.core.Report
import hedgehog.core.{Failed, GaveUp, OK}
import hedgehog.runner.Test

class HedgehogFailException(message: String, val report: Report, val seed: Long)
    extends Exception(message)
    with NoStackTrace

object HedgehogFailException {

  def fromReport(report: Report, seed: Long): Option[HedgehogFailException] = {
    report.status match {
      case OK =>
        None
      case Failed(shrinks, log) =>
        val coverage = Test.renderCoverage(report.coverage, report.tests)
        val message = render(
          s"Falsified after ${report.tests.value} passed tests and ${shrinks.value} shrinks using seed ${seed}",
          log.map(Test.renderLog) ++ coverage
        )
        Some(new HedgehogFailException(message, report, seed))
      case GaveUp =>
        val coverage = Test.renderCoverage(report.coverage, report.tests)
        val message = render(
          s"Gave up after ${report.tests.value} passed tests using seed value $seed. ${report.discards.value} were discarded",
          coverage
        )
        Some(new HedgehogFailException(message, report, seed))
    }
  }

  private def render(msg: String, extras: List[String]): String =
    (msg :: extras.map(e => "> " + e)).mkString("\n")
}
