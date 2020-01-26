package munit.sbtmunit

import munit.sbtmunit.MUnitTestReport.Summary

trait MUnitReportListener {
  def onReport(report: Summary): Unit
}
