package munit

import munit.MUnitTestReport.TestReport

trait MUnitReportListener {
  def onReport(report: TestReport): Unit
}
