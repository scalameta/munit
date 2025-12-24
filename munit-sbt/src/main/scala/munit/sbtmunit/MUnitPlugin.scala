package munit.sbtmunit

import sbt.Keys._
import sbt._
import sbt.plugins._

import java.nio.file.{Files, Paths}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.{util => ju}

object MUnitPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = JvmPlugin
  object autoImport {
    val munitBucketName: SettingKey[Option[String]] = settingKey[Option[String]](
      "The Google Cloud Storage bucket name, defaults to 'munit-test-reports'."
    )
    val munitRepository: SettingKey[Option[String]] = settingKey[Option[String]](
      "The repository of this project, for example GitHub URL."
    )
    val munitRef: SettingKey[Option[String]] =
      settingKey[Option[String]]("The git branch or tag reference from where this report was created, for example 'refs/heads/master'.")
    val munitSha: SettingKey[Option[String]] =
      settingKey[Option[String]]("The git commit SHA from where this report was created, for example 'f32a4ab1cf8685f47837f07bb52f515b48fd4ecb'.")
    val munitReportName: SettingKey[Option[String]] =
      settingKey[Option[String]]("The filename for this test report.")
    val munitReportListener: SettingKey[Option[MUnitReportListener]] =
      settingKey[Option[MUnitReportListener]]("The listener to handle reports.")
  }
  import autoImport._

  override val globalSettings: List[Setting[_ <: Option[Object]]] = List(
    munitBucketName := Some("munit-test-reports"),
    munitRepository := Option(System.getenv("GITHUB_REPOSITORY")),
    munitRef := Option(System.getenv("GITHUB_REF")),
    munitSha := Option(System.getenv("GITHUB_SHA")),
  )

  override val projectSettings: Seq[Def.Setting[_]] = List(
    munitReportListener := {
      for {
        credentials <- Option(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))
        if credentials.nonEmpty
        path = Paths.get(credentials).toAbsolutePath()
        if Files.exists(path) || {
          Option(System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON")) match {
            case Some(base64) if base64.nonEmpty =>
              val json = ju.Base64.getDecoder().decode(base64)
              Files.createDirectories(path.getParent())
              Files.write(path, json)
              true
            case _ => false
          }
        }
        bucketName <- munitBucketName.value
        reportName <- munitReportName.value
      } yield new MUnitGcpListener(reportName, bucketName)
    },
    munitReportName := {
      for {
        ref <- munitRef.value
        sha <- munitSha.value
        date = DateTimeFormatter.ISO_DATE.format(LocalDate.now())
        project = thisProject.value.id
        scala = scalaVersion.value
        jvm = System.getProperty("java.version", "unknown")
      } yield s"$date/$ref/$sha/$project/$scala/$jvm"
    },
    testListeners ++= {
      for {
        reportName <- munitReportName.value.toList
        repository <- munitRepository.value.toList
        listener <- munitReportListener.value.toList
        ref <- munitRef.value
        sha <- munitSha.value
      } yield new MUnitTestsListener(
        listener,
        repository,
        reportName,
        ref,
        sha,
        scalaVersion.value,
        thisProject.value.id,
      )
    },
  )
}
