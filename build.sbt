import scala.collection.mutable

import sbtcrossproject.CrossPlugin.autoImport.crossProject

def previousVersion = "1.0.0-RC1"

def scala213 = "2.13.18"

def scala212 = "2.12.21"

def scala3 = "3.3.8"

def scala3next = "3.8.4"

def junitVersion = "4.13.2"
def portableScalaReflectVersion = "1.1.3"
def gcp = "com.google.cloud" % "google-cloud-storage" % "2.71.0"

inThisBuild {
  List(
    // version is set dynamically by sbt-dynver, but let's adjust it
    version := {
      val curVersion = version.value
      def dynVer(out: sbtdynver.GitDescribeOutput): String = {
        def tagVersion = out.ref.dropPrefix
        if (out.isCleanAfterTag) tagVersion
        else if (System.getenv("CI") == null) s"$tagVersion-next-SNAPSHOT" // modified for local builds
        else if (out.commitSuffix.distance == 0) tagVersion
        else if (sys.props.contains("backport.release")) tagVersion
        else curVersion
      }
      dynverGitDescribeOutput.value.mkVersion(dynVer, curVersion)
    },
    organization := "org.scalameta",
    homepage := Some(url("https://github.com/scalameta/munit")),
    licenses :=
      List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(Developer(
      "olafurpg",
      "Ólafur Páll Geirsson",
      "olafurpg@gmail.com",
      url("https://geirsson.com"),
    )),
    scalaVersion := scala213,
    useSuperShell := false,
    // the bundled google-java-format needs 21; pick one our floor JDK can run
    javafmtFormatterCompatibleJavaVersion := 17,
  )
}

addCommandAlias(
  "scalafixAll",
  s"; ++$scala212 ; scalafixEnable ; all scalafix test:scalafix",
)
addCommandAlias(
  "scalafixCheckAll",
  s"; ++$scala212 ;  scalafixEnable ; scalafix --check ; test:scalafix --check",
)
addCommandAlias(
  "checkDiscoveryNative",
  s"; +testsNative/run ; ++$scala3next! ; testsNative/run",
)
addCommandAlias(
  "checkDiscoveryJS",
  s"; +testsJS/run ; ++$scala3next! ; testsJS/run",
)
addCommandAlias(
  "preparePR",
  "; scalafmtSbt; reload; +scalafmt; +Test/scalafmt ; javafmt ; scalafixCheckAll",
)
val isPreScala213 = Set[Option[(Long, Long)]](Some((2, 11)), Some((2, 12)))
val scala2Versions = List(scala213, scala212)

val scala3Versions = List(scala3)
val allScalaVersions = scala2Versions ++ scala3Versions

def isScala2(v: Option[(Long, Long)]): Boolean = v.exists(_._1 == 2)
val isScala3Setting = Def
  .setting(isScala3(CrossVersion.partialVersion(scalaVersion.value)))

def isScala3(v: Option[(Long, Long)]): Boolean = v.exists(_._1 == 3)

val unpublished = publish / skip := true

// NOTE(olafur): disable Scala.js and Native settings for IntelliJ.
val skipIdeaSetting =
  SettingKey[Boolean]("ide-skip-project", rank = KeyRanks.Invisible)
def onOtherPlatform(except: AutoPlugin*): Project => Project =
  _.disablePlugins(MimaPlugin +: except: _*).settings(skipIdeaSetting := true)
val sharedJSConfigure: Project => Project = onOtherPlatform()
val sharedNativeConfigure: Project => Project = onOtherPlatform(ScalafixPlugin)

val mimaEnable = Def.settings(
  mimaBinaryIssueFilters +=
    _root_.munit.build.Mima.languageAgnosticCompatibilityPolicy,
  mimaPreviousArtifacts := Set(
    if (crossPaths.value) "org.scalameta" %% moduleName.value % previousVersion
    else "org.scalameta" % moduleName.value % previousVersion
  ),
)

val sharedJVMSettings = Def
  .settings(crossScalaVersions := allScalaVersions, mimaEnable)
val sharedJSSettings = Def.settings(crossScalaVersions := allScalaVersions)
val sharedNativeSettings = Def.settings(crossScalaVersions := allScalaVersions)

val sharedSettings = List(
  javacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, minor)) if minor >= 8 => Seq("--release", "17")
      case _ => Seq("--release", "8")
    }
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) => List(
          "-Yrangepos",
          "-target:jvm-1.8",
          "-Xexperimental",
          "-Ywarn-unused-import",
        )
      case Some((2, _)) => List(
          "-Yrangepos",
          "-release:8",
          // -Xlint is unusable because of
          // https://github.com/scala/bug/issues/10448
          "-Ywarn-unused:imports",
        )
      case Some((3, minor)) => List(
          "-language:implicitConversions",
          "-release",
          if (minor >= 8) "17" else "8",
        )
      case _ => Nil
    }
  },
  Test / scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, minor)) => List("-pagewidth", "80")
      case _ => Nil
    }
  },
)

lazy val junit = project.in(file("junit-interface")).settings(
  mimaEnable,
  moduleName := "junit-interface",
  description := "A Java implementation of sbt's test interface for JUnit 4",
  autoScalaLibrary := false,
  crossPaths := false,
  sbtPlugin := false,
  crossScalaVersions := List(allScalaVersions.head),
  libraryDependencies ++= List(
    "junit" % "junit" % junitVersion,
    "org.scala-sbt" % "test-interface" % "1.0",
  ),
  Compile / javacOptions ++= List("--release", "8"),
)

lazy val munit = crossProject(JSPlatform, JVMPlatform, NativePlatform).settings(
  sharedSettings,
  unmanagedMainSources("munit", "shared"),
  libraryDependencies ++= List("org.scala-lang" % "scala-reflect" % {
    if (isScala3Setting.value) scala213 else scalaVersion.value
  } % Provided),
).nativeConfigure(sharedNativeConfigure).nativeSettings(
  sharedNativeSettings,
  libraryDependencies ++= List(
    "org.scala-native" %%% "test-interface-sbt-defs" % nativeVersion,
    ("org.portable-scala" %%% "portable-scala-reflect" %
      portableScalaReflectVersion).cross(CrossVersion.for3Use2_13),
  ),
).jsConfigure(sharedJSConfigure).jsSettings(
  sharedJSSettings,
  libraryDependencies ++= List(
    ("org.scala-js" %% "scalajs-test-interface" % scalaJSVersion)
      .cross(CrossVersion.for3Use2_13),
    ("org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion)
      .cross(CrossVersion.for3Use2_13),
    ("org.portable-scala" %%% "portable-scala-reflect" %
      portableScalaReflectVersion).cross(CrossVersion.for3Use2_13),
  ),
).jvmSettings(
  sharedJVMSettings,
  libraryDependencies ++= List(
    "junit" % "junit" % junitVersion,
    ("org.portable-scala" %%% "portable-scala-reflect" %
      portableScalaReflectVersion).cross(CrossVersion.for3Use2_13),
  ),
).jvmConfigure(_.dependsOn(junit)).dependsOn(munitDiff)

lazy val munitJVM = munit.jvm
lazy val munitJS = munit.js
lazy val munitNative = munit.native

lazy val plugin = project.in(file("munit-sbt")).enablePlugins(BuildInfoPlugin)
  .settings(
    sharedSettings,
    moduleName := "sbt-munit",
    sbtPlugin := true,
    scalaVersion := scala212,
    // the floor a user of this plugin must be on, not the sbt we build with
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.9.0"
        case _ => "2.0.0"
      }
    },
    buildInfoPackage := "munit.sbtmunit",
    buildInfoKeys := Seq[BuildInfoKey]("munitVersion" -> version.value),
    crossScalaVersions := List(scala212, scala3next),
    libraryDependencies ++= List(gcp),
  ).disablePlugins(MimaPlugin)

lazy val munitDiff = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("munit-diff")).settings(
    moduleName := "munit-diff",
    sharedSettings,
    libraryDependencies ++= List("org.scala-lang" % "scala-reflect" % {
      if (isScala3Setting.value) scala213 else scalaVersion.value
    } % Provided),
  ).jvmSettings(sharedJVMSettings).nativeConfigure(sharedNativeConfigure)
  .nativeSettings(sharedNativeSettings).jsConfigure(sharedJSConfigure)
  .jsSettings(sharedJSSettings)

lazy val tests = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .dependsOn(munit).enablePlugins(BuildInfoPlugin).settings(
    sharedSettings,
    buildInfoPackage := "munit",
    buildInfoKeys := Seq[BuildInfoKey](
      "sourceDirectory" -> src("tests", "shared", "main").value.getAbsolutePath,
      scalaVersion,
    ),
    unmanagedTestSources("tests", "shared"),
    unpublished,
  ).nativeConfigure(sharedNativeConfigure).nativeSettings(sharedNativeSettings)
  .jsConfigure(sharedJSConfigure).jsSettings(
    sharedJSSettings,
    Compile / mainClass := Some("munit.ReflectiveInstantiationCheck"),
    scalaJSUseMainModuleInitializer := true,
    jsEnv := {
      val log = sLog.value
      if (Option(System.getenv("MUNIT_JS_ENV")).contains("jsdom")) {
        log.info("Testing in JSDOMNodeJSEnv")
        new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
      } else {
        log.info("Testing in NodeJSEnv")
        new org.scalajs.jsenv.nodejs.NodeJSEnv
      }
    },
  ).jvmSettings(
    sharedJVMSettings,
    fork := true,
    Test / parallelExecution := true,
    Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "+b"),
  ).disablePlugins(MimaPlugin)
lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js
lazy val testsNative = tests.native

lazy val docs = project.in(file("munit-docs")).dependsOn(munitJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin).disablePlugins(MimaPlugin)
  .settings(
    sharedSettings,
    unpublished,
    moduleName := "munit-docs",
    libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "1.3.0",
    crossScalaVersions := List(scala213, scala212),
    Test / skip := true,
    mdocOut := (ThisBuild / baseDirectory).value / "website" / "target" / "docs",
    mdocExtraArguments := List("--no-link-hygiene"),
    mdocVariables := Map(
      "VERSION" -> version.value.replaceFirst("\\+.*", ""),
      "STABLE_VERSION" -> "1.0.4",
    ),
    fork := false,
  )

// Aggregate only: it builds nothing, so it has no artifact to publish and
// nothing to check for binary compatibility.
lazy val root = project.in(file(".")).withId("munit-root").aggregate(
  junit,
  plugin,
  docs,
  munitJVM,
  munitJS,
  munitNative,
  munitDiff.jvm,
  munitDiff.js,
  munitDiff.native,
  testsJVM,
  testsJS,
  testsNative,
).settings(
  unpublished,
  mimaPreviousArtifacts := Set.empty,
  crossScalaVersions := List(),
)

Global / excludeLintKeys ++= Set(mimaPreviousArtifacts)

def src(name: String, dir: String, cfg: String) = Def
  .setting[File]((ThisBuild / baseDirectory).value / name / dir / "src" / cfg)

def roots(name: String, cfg: String, dirs: String*) = Def.setting[Seq[File]] {
  val root = (ThisBuild / baseDirectory).value / name
  val variants = mutable.ListBuffer.empty[String]
  val partialVersion = CrossVersion.partialVersion(scalaVersion.value)
  if (isScala2(partialVersion)) {
    variants += "scala-2"
    if (isPreScala213(partialVersion)) variants += "scala-pre-2.13"
  }
  for (dir <- dirs.toList; variant <- variants)
    yield root / dir / "src" / cfg / variant
}

def unmanagedMainSources(name: String, dirs: String*) = Def.settings(
  Compile / unmanagedSourceDirectories ++= roots(name, "main", dirs: _*).value
)

def unmanagedTestSources(name: String, dirs: String*) = Def.settings(
  Test / unmanagedSourceDirectories ++= roots(name, "test", dirs: _*).value
)
