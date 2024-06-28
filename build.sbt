import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossPlugin.autoImport.CrossType
import scala.collection.mutable
def previousVersion = "1.0.0-RC1"

def scala213 = "2.13.14"

def scala212 = "2.12.19"

def scala3 = "3.3.3"
def junitVersion = "4.13.2"
def gcp = "com.google.cloud" % "google-cloud-storage" % "2.40.1"
inThisBuild(
  List(
    version ~= { old =>
      if ("true" == System.getProperty("CI") && old.contains("+0-")) {
        old.replaceAll("\\+0-.*", "")
      } else {
        old
      }
    },
    organization := "org.scalameta",
    homepage := Some(url("https://github.com/scalameta/munit")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "olafurpg",
        "Ólafur Páll Geirsson",
        "olafurpg@gmail.com",
        url("https://geirsson.com")
      )
    ),
    scalaVersion := scala213,
    useSuperShell := false
  )
)

publish / skip := true
mimaPreviousArtifacts := Set.empty
crossScalaVersions := List()
addCommandAlias(
  "scalafixAll",
  s"; ++$scala212 ; scalafixEnable ; all scalafix test:scalafix"
)
addCommandAlias(
  "scalafixCheckAll",
  s"; ++$scala212 ;  scalafixEnable ; scalafix --check ; test:scalafix --check"
)
val isPreScala213 = Set[Option[(Long, Long)]](Some((2, 11)), Some((2, 12)))
val scala2Versions = List(scala213, scala212)

val scala3Versions = List(scala3)
val allScalaVersions = scala2Versions ++ scala3Versions

def isScala2(v: Option[(Long, Long)]): Boolean = v.exists(_._1 == 2)
val isScala3Setting = Def.setting {
  isScala3(CrossVersion.partialVersion(scalaVersion.value))
}

def isScala3(v: Option[(Long, Long)]): Boolean = v.exists(_._1 == 3)

// NOTE(olafur): disable Scala.js and Native settings for IntelliJ.
lazy val skipIdeaSettings =
  SettingKey[Boolean]("ide-skip-project").withRank(KeyRanks.Invisible) := true
lazy val mimaEnable: List[Def.Setting[_]] = List(
  mimaBinaryIssueFilters ++= List.empty,
  mimaPreviousArtifacts := {
    if (crossPaths.value)
      Set("org.scalameta" %% moduleName.value % previousVersion)
    else Set("org.scalameta" % moduleName.value % previousVersion)
  }
)

val sharedJVMSettings: List[Def.Setting[_]] = List(
  crossScalaVersions := allScalaVersions
) ++ mimaEnable

val sharedJSSettings: List[Def.Setting[_]] = List(
  skipIdeaSettings,
  crossScalaVersions := allScalaVersions.filterNot(_.startsWith("0."))
)
val sharedJSConfigure: Project => Project =
  _.disablePlugins(MimaPlugin)

val sharedNativeSettings: List[Def.Setting[_]] = List(
  skipIdeaSettings,
  crossScalaVersions := allScalaVersions
)
val sharedNativeConfigure: Project => Project =
  _.disablePlugins(ScalafixPlugin, MimaPlugin)

val sharedSettings = List(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) =>
        List(
          "-Yrangepos",
          "-Xexperimental",
          "-Ywarn-unused-import",
          "-target:jvm-1.8"
        )
      case Some((major, _)) if major != 2 =>
        List(
          "-language:implicitConversions"
        )
      case _ =>
        List(
          "-target:jvm-1.8",
          "-Yrangepos",
          // -Xlint is unusable because of
          // https://github.com/scala/bug/issues/10448
          "-Ywarn-unused:imports"
        )
    }
  }
)

lazy val junit = project
  .in(file("junit-interface"))
  .settings(
    mimaEnable,
    moduleName := "junit-interface",
    description := "A Java implementation of sbt's test interface for JUnit 4",
    autoScalaLibrary := false,
    crossPaths := false,
    sbtPlugin := false,
    crossScalaVersions := List(allScalaVersions.head),
    libraryDependencies ++= List(
      "junit" % "junit" % junitVersion,
      "org.scala-sbt" % "test-interface" % "1.0"
    ),
    Compile / javacOptions ++= List("-target", "1.8", "-source", "1.8"),
    Compile / doc / javacOptions --= List("-target", "1.8")
  )

lazy val munit = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    sharedSettings,
    Compile / unmanagedSourceDirectories ++=
      crossBuildingDirectories("munit", "main").value,
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-reflect" % {
        if (isScala3Setting.value) scala213
        else scalaVersion.value
      } % Provided
    )
  )
  .nativeConfigure(sharedNativeConfigure)
  .nativeSettings(
    sharedNativeSettings,
    libraryDependencies ++= List(
      "org.scala-native" %%% "test-interface" % nativeVersion
    )
  )
  .jsConfigure(sharedJSConfigure)
  .jsSettings(
    sharedJSSettings,
    libraryDependencies ++= List(
      ("org.scala-js" %% "scalajs-test-interface" % scalaJSVersion)
        .cross(CrossVersion.for3Use2_13),
      ("org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion)
        .cross(CrossVersion.for3Use2_13)
    )
  )
  .jvmSettings(
    sharedJVMSettings,
    libraryDependencies ++= List(
      "junit" % "junit" % junitVersion
    )
  )
  .jvmConfigure(_.dependsOn(junit))
  .dependsOn(munitDiff)

lazy val munitJVM = munit.jvm
lazy val munitJS = munit.js
lazy val munitNative = munit.native

lazy val plugin = project
  .in(file("munit-sbt"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    sharedSettings,
    moduleName := "sbt-munit",
    sbtPlugin := true,
    scalaVersion := scala212,
    crossScalaVersions := List(scala212),
    buildInfoPackage := "munit.sbtmunit",
    buildInfoKeys := Seq[BuildInfoKey](
      "munitVersion" -> version.value
    ),
    crossScalaVersions := List(scala212),
    libraryDependencies ++= List(
      gcp
    )
  )
  .disablePlugins(MimaPlugin)

lazy val munitDiff = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("munit-diff"))
  .settings(
    moduleName := "munit-diff",
    sharedSettings,
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-reflect" % {
        if (isScala3Setting.value) scala213
        else scalaVersion.value
      } % Provided
    )
  )
  .jvmSettings(
    sharedJVMSettings
  )
  .nativeConfigure(sharedNativeConfigure)
  .nativeSettings(
    sharedNativeSettings
  )
  .jsConfigure(sharedJSConfigure)
  .jsSettings(sharedJSSettings)

lazy val tests = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .dependsOn(munit)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    sharedSettings,
    buildInfoPackage := "munit",
    buildInfoKeys := Seq[BuildInfoKey](
      "sourceDirectory" ->
        ((ThisBuild / baseDirectory).value / "tests" / "shared" / "src" / "main").getAbsolutePath.toString,
      scalaVersion
    ),
    Test / unmanagedSourceDirectories ++=
      crossBuildingDirectories("tests", "test").value,
    publish / skip := true
  )
  .nativeConfigure(sharedNativeConfigure)
  .nativeSettings(sharedNativeSettings)
  .jsConfigure(sharedJSConfigure)
  .jsSettings(
    sharedJSSettings,
    jsEnv := {
      val log = sLog.value
      if (Option(System.getenv("GITHUB_JOB")).contains("jsdom")) {
        log.info("Testing in JSDOMNodeJSEnv")
        new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
      } else {
        log.info("Testing in NodeJSEnv")
        new org.scalajs.jsenv.nodejs.NodeJSEnv
      }
    }
  )
  .jvmSettings(
    sharedJVMSettings,
    fork := true,
    Test / parallelExecution := true,
    Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "+b")
  )
  .disablePlugins(MimaPlugin)
lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js
lazy val testsNative = tests.native

lazy val docs = project
  .in(file("munit-docs"))
  .dependsOn(munitJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .disablePlugins(MimaPlugin)
  .settings(
    sharedSettings,
    publish / skip := true,
    moduleName := "munit-docs",
    libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "1.0.0",
    crossScalaVersions := List(scala213, scala212),
    test := {},
    mdocOut :=
      (ThisBuild / baseDirectory).value / "website" / "target" / "docs",
    mdocExtraArguments := List("--no-link-hygiene"),
    mdocVariables := Map(
      "VERSION" -> version.value.replaceFirst("\\+.*", ""),
      "STABLE_VERSION" -> "0.7.29"
    ),
    fork := false
  )

Global / excludeLintKeys ++= Set(
  mimaPreviousArtifacts
)
def crossBuildingDirectories(name: String, config: String) =
  Def.setting[Seq[File]] {
    val root = (ThisBuild / baseDirectory).value / name
    val base = root / "shared" / "src" / config
    val result = mutable.ListBuffer.empty[File]
    val partialVersion = CrossVersion.partialVersion(scalaVersion.value)
    if (isPreScala213(partialVersion)) {
      result += base / "scala-pre-2.13"
    }
    if (isScala2(partialVersion)) {
      result += base / "scala-2"
    }
    result.toList
  }
