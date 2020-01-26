package munit.sbtmunit

import com.google.cloud.storage.StorageOptions
import com.google.cloud.storage.BucketInfo
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import com.google.cloud.storage.Bucket.BlobTargetOption
import com.google.cloud.storage.Storage.PredefinedAcl
import com.google.cloud.storage.StorageException
import sbt.util.Logger
import munit.sbtmunit.MUnitTestReport.Summary

class MUnitGcpListener(
    val reportName: String,
    val bucketName: String,
    val maxRetries: Int = 100,
    logger: Logger = sbt.ConsoleLogger()
) extends MUnitReportListener {
  private lazy val storage = StorageOptions.getDefaultInstance().getService()
  private lazy val bucket = Option(storage.get(bucketName)).getOrElse {
    storage.create(BucketInfo.of(bucketName))
  }
  def onReport(report: Summary): Unit = synchronized {
    val suffixes = Stream
      .from(0)
      .map {
        case 0 => ".json"
        case n => s"-$n.json"
      }
      .take(maxRetries)
    val bytes = new Gson().toJson(report).getBytes(StandardCharsets.UTF_8)
    val success = suffixes.find { suffix =>
      val name = s"${report.repository}/${reportName}$suffix"
      try {
        val blob = bucket.create(
          name,
          bytes,
          "application/json",
          BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ),
          BlobTargetOption.doesNotExist()
        )
        logger.info(
          s"uploaded test report: gs://${blob.getBucket()}/${blob.getBlobId().getName()}"
        )
        true
      } catch {
        case e: StorageException
            if Option(e.getMessage()).contains("Precondition Failed") =>
          false
      }
    }
    if (success.isEmpty) {
      logger.error(s"warn: failed to upload report after $maxRetries retries.")
    }
  }
}
