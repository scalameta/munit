package munit.sbtmunit

import sbt.util.Logger

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import com.google.gson.Gson

class MUnitLocalListener(
    path: Path = Paths.get(".").toAbsolutePath().normalize()
      .resolve("test-report.json"),
    maxRetries: Int = 100,
    logger: Logger = sbt.ConsoleLogger(),
) extends MUnitReportListener {
  def onReport(report: MUnitTestReport.Summary): Unit = {
    val bytes = new Gson().toJson(report).getBytes(StandardCharsets.UTF_8)
    Files.write(
      path,
      bytes,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING,
    )
    logger.info(s"wrote test report: $path")
  }
}
