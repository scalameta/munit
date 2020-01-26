package munit

import sbt._
import sbt.Keys._
import sbt.plugins._
import java.nio.file.Paths
import java.nio.file.Files
import java.{util => ju}
import java.time.format.DateTimeFormatter
import java.time.LocalDate

object MUnitPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = JvmPlugin
  object autoImport {
    val munitRepository: SettingKey[Option[String]] =
      settingKey[Option[String]](
        "The repository of this project, for example GitHub URL."
      )
    val munitId: SettingKey[Option[String]] =
      settingKey[Option[String]]("Unique identifier for this test run.")
    val munitReportListener: SettingKey[Option[MUnitReportListener]] =
      settingKey[Option[MUnitReportListener]](
        "The listener to handle reports."
      )
  }
  import autoImport._

  override val globalSettings: List[Setting[_ <: Option[Object]]] = List(
    munitReportListener := {
      for {
        credentials <- Option(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))
        path = Paths.get(credentials).toAbsolutePath()
        if Files.exists(path) || {
          Option(System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON")) match {
            case Some(base64) =>
              val json = ju.Base64.getDecoder().decode(base64)
              Files.createDirectories(path.getParent())
              Files.write(path, json)
              true
            case None =>
              false
          }
        }
      } yield new MUnitGcpListener()
    },
    munitRepository := Option(System.getenv("GITHUB_REPOSITORY"))
  )

  override val projectSettings: Seq[Def.Setting[_]] = List(
    munitId := {
      for {
        ref <- Option(System.getenv("GITHUB_REF"))
        sha <- Option(System.getenv("GITHUB_SHA"))
        date = DateTimeFormatter.ISO_DATE.format(LocalDate.now())
        project = thisProject.value.id
        scala = scalaVersion.value
        jvm = System.getProperty("java.version", "unknown")
      } yield s"${date}/$ref/$sha/$project/$scala/$jvm"
    },
    testListeners ++= {
      for {
        runId <- munitId.value.toList
        repository <- munitRepository.value.toList
        listener <- munitReportListener.value.toList
      } yield new MUnitTestsListener(
        listener,
        repository,
        runId,
        scalaVersion.value,
        thisProject.value.id
      )
    }
  )
}
