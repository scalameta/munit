package munit

import com.google.cloud.storage.StorageOptions
import com.google.cloud.storage.BucketInfo
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import com.google.cloud.storage.Bucket.BlobTargetOption
import com.google.cloud.storage.Storage.PredefinedAcl
import com.google.cloud.storage.StorageException
import sbt.util.Logger

class MUnitGcpListener(
    val bucketName: String = "munit-test-reports",
    val maxRetries: Int = 100,
    logger: Logger = sbt.ConsoleLogger()
) extends MUnitReportListener {
  private lazy val storage = StorageOptions.getDefaultInstance().getService()
  private lazy val bucket = Option(storage.get(bucketName)).getOrElse {
    storage.create(BucketInfo.of(bucketName))
  }
  def onReport(report: MUnitTestReport.TestReport): Unit = synchronized {
    val suffixes = Stream
      .from(0)
      .map {
        case 0 => ".json"
        case n => s"-$n.json"
      }
      .take(maxRetries)
    val bytes = new Gson().toJson(report).getBytes(StandardCharsets.UTF_8)
    val success = suffixes.find { suffix =>
      val name = s"${report.repository}/${report.runId}$suffix"
      try {
        val blob = bucket.create(
          name,
          bytes,
          BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ),
          BlobTargetOption.doesNotExist()
        )
        logger.info(
          s"uploaded test report: gs://${blob.getBucket()}/${blob.getBlobId().getName()}"
        )
        true
      } catch {
        case _: StorageException =>
          false
      }
    }
    if (success.isEmpty) {
      logger.error(s"warn: failed to upload report after $maxRetries retries.")
    }
  }
}
