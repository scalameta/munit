package munit

import java.nio.file.Path
import java.nio.file.Paths
import munit.MUnitTestReport.Summary
import sbt.util.Logger
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class MUnitLocalListener(
    path: Path =
      Paths.get(".").toAbsolutePath().normalize().resolve("test-report.json"),
    maxRetries: Int = 100,
    logger: Logger = sbt.ConsoleLogger()
) extends MUnitReportListener {
  def onReport(report: Summary): Unit = {
    val bytes = new Gson().toJson(report).getBytes(StandardCharsets.UTF_8)
    Files.write(
      path,
      bytes,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
    logger.info(s"wrote test report: $path")
  }
}
