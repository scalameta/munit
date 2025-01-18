package munit.sbtmunit

import sbt.Keys._
import sbt._

import MUnitPlugin.autoImport._

object MUnitReportPlugin extends AutoPlugin {
  override def requires = MUnitPlugin
  override val projectSettings: List[Setting[_ <: Seq[Object]]] = List(
    libraryDependencies ++= {
      if ("unknown" == BuildInfo.munitVersion) Nil
      else List("org.scalameta" %% "munit-docs" % BuildInfo.munitVersion)
    },
    Compile / resourceGenerators += Def.task[List[File]] {
      val out = (Compile / managedResourceDirectories).value.head /
        "munit.properties"
      val props = new java.util.Properties()
      munitRepository.value.foreach(repo => props.put("munitRepository", repo))
      munitBucketName.value.foreach(repo => props.put("munitBucketName", repo))
      IO.write(props, "MUnit properties", out)
      List(out)
    },
  )
}
