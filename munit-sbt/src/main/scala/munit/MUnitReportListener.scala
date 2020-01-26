package munit

import munit.MUnitTestReport.Summary

trait MUnitReportListener {
  def onReport(report: Summary): Unit
}
