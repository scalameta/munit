import scala.collection.mutable

import Extensions._

def previousVersion = "1.0.0-RC1"

def scala213 = "2.13.18"

def scala212 = "2.12.21"

def scala3 = "3.3.8"

def scala3next = "3.8.4"

def junitVersion = "4.13.2"

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

// `++` cannot select a matrix row, so the 2.12 cells are named explicitly;
// plugin pins 2.12 itself, so the old `++` prefix is redundant.
def scalafixTargets = List(munitDiff, munit, tests)
  .flatMap(m => List(m.jvm(scala212), m.js(scala212))) ++ List(plugin, docs)
def scalafixOn(args: String) = onEach(scalafixTargets, s"scalafix $args") +
  onEach(scalafixTargets, s"Test/scalafix $args")
addCommandAlias("scalafixAll", "; scalafixEnable" + scalafixOn(""))
addCommandAlias("scalafixCheckAll", "; scalafixEnable" + scalafixOn("--check"))
addCommandAlias("testJVM", onEach(tests.jvm.get, "testFull"))
addCommandAlias("testJS", onEach(allScalaVersions.map(tests.js(_)), "testFull"))
addCommandAlias(
  "testNative",
  onEach(allScalaVersions.map(tests.native(_)), "testFull"),
)
addCommandAlias("checkDiscoveryNative", onEach(tests.native.get, "run"))
addCommandAlias("checkDiscoveryJS", onEach(tests.js.get, "run"))
addCommandAlias(
  "preparePR",
  "; scalafmtSbt; reload; scalafmt; Test/scalafmt ; javafmt ; scalafixCheckAll",
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
val skipIdeaSetting = SettingKey[Boolean]("ide-skip-project")
  .withRank(KeyRanks.Invisible)
def onOtherPlatform(except: AutoPlugin*): Project => Project =
  _.disablePlugins(MimaPlugin +: except: _*).settings(skipIdeaSetting := true)
val onJS: Project => Project = onOtherPlatform()
val onNative: Project => Project = onOtherPlatform(ScalafixPlugin)

val mimaEnable = Def.settings(
  mimaBinaryIssueFilters +=
    _root_.munit.build.Mima.languageAgnosticCompatibilityPolicy,
  mimaPreviousArtifacts := Set(
    if (crossPaths.value) "org.scalameta" %% moduleName.value % previousVersion
    else "org.scalameta" % moduleName.value % previousVersion
  ),
)

// The matrix supplies the Scala versions, so crossScalaVersions is gone; what
// is left per platform is mima and the IntelliJ/scalafix opt-outs.
// The discovery guards also check the *next* Scala on JS and Native, which
// needs a row of its own. Its binary version is 3, like scala3, so it would
// collide on both cell id and artifact name: give it an explicit axis, and
// never publish it.
def nextRow = List(VirtualAxis.scalaPartialVersion(scala3next))

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

val scalaReflect = Def.setting {
  val ver = if (isScala3Setting.value) scala213 else scalaVersion.value
  "org.scala-lang" % "scala-reflect" % ver % Provided
}

val munitSettings = Def.settings(
  sharedSettings,
  unmanagedMainSources("munit", "shared"),
  libraryDependencies ++= List(
    scalaReflect.value,
    ("org.portable-scala" %% "portable-scala-reflect" % "1.1.3")
      .cross(CrossVersion.for3Use2_13),
  ),
)

val munitOnJVM: Project => Project = _.dependsOn(junit).settings(
  mimaEnable,
  unmanagedMainSources("munit", "jvm"),
  libraryDependencies ++= List("junit" % "junit" % junitVersion),
)

val munitOnNative: Project => Project = onNative.settings(
  unmanagedMainSources("munit", "native", "js-native"),
  libraryDependencies ++=
    List("org.scala-native" %% "test-interface-sbt-defs" % nativeVersion),
)

val munitOnJS: Project => Project = onJS.settings(
  unmanagedMainSources("munit", "js", "js-native"),
  libraryDependencies ++= {
    // Published with a plain binary suffix only
    val binary = if (isScala3Setting.value) "2.13" else scalaBinaryVersion.value
    def dep(name: String) = "org.scala-js" % s"${name}_$binary" % scalaJSVersion
    List(dep("scalajs-test-interface"), dep("scalajs-junit-test-runtime"))
  },
)

lazy val munit = projectMatrix.in(file("munit")).dependsOn(munitDiff)
  .settings(munitSettings).jvmPlatform(allScalaVersions, Nil, munitOnJVM)
  .jsPlatform(allScalaVersions, Nil, munitOnJS)
  .jsPlatform(List(scala3next), nextRow, munitOnJS.settings(unpublished))
  .nativePlatform(allScalaVersions, Nil, munitOnNative)
  .nativePlatform(List(scala3next), nextRow, munitOnNative.settings(unpublished))

lazy val munitJVM = munit.jvm(scala213)

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

val munitDiffSettings = Def.settings(
  moduleName := "munit-diff",
  sharedSettings,
  unmanagedMainSources("munit-diff", "shared"),
  libraryDependencies ++= List(scalaReflect.value),
)

lazy val munitDiff = projectMatrix.in(file("munit-diff"))
  .settings(munitDiffSettings).jvmPlatform(allScalaVersions, mimaEnable)
  .jsPlatform(allScalaVersions, Nil, onJS)
  .jsPlatform(List(scala3next), nextRow, onJS.settings(unpublished))
  .nativePlatform(allScalaVersions, Nil, onNative)
  .nativePlatform(List(scala3next), nextRow, onNative.settings(unpublished))

val testsSettings = Def.settings(
  sharedSettings,
  buildInfoPackage := "munit",
  buildInfoKeys := Seq[BuildInfoKey](
    "sourceDirectory" -> src("tests", "shared", "main").value.getAbsolutePath,
    scalaVersion,
  ),
  unmanagedSources("tests", "shared"),
  unpublished,
)

val testsJVMSettings = Def.settings(
  unmanagedSources("tests", "jvm"),
  Test / fork := true,
  Test / parallelExecution := true,
  Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "+b"),
)

val testsOnNative: Project => Project = onNative
  .settings(unmanagedSources("tests", "native"))

val testsOnJS: Project => Project = onJS.settings(
  unmanagedSources("tests", "js"),
  Compile / mainClass := Some("munit.ReflectiveInstantiationCheck"),
  scalaJSUseMainModuleInitializer := true,
  jsEnv := Def.uncached {
    val log = sLog.value
    if (System.getenv("MUNIT_JS_ENV") == "jsdom") {
      log.info("Testing in JSDOMNodeJSEnv")
      new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
    } else {
      log.info("Testing in NodeJSEnv")
      new org.scalajs.jsenv.nodejs.NodeJSEnv
    }
  },
)

lazy val tests = projectMatrix.in(file("tests")).dependsOn(munit)
  .enablePlugins(BuildInfoPlugin).settings(testsSettings)
  .nativePlatform(allScalaVersions, Nil, testsOnNative)
  .nativePlatform(List(scala3next), nextRow, testsOnNative)
  .jsPlatform(allScalaVersions, Nil, testsOnJS)
  .jsPlatform(List(scala3next), nextRow, testsOnJS)
  .jvmPlatform(allScalaVersions, Nil, _.settings(testsJVMSettings))
  .disablePlugins(MimaPlugin)

// A matrix cell per Scala version replaces `+`, so the platform-wide commands
// are aliases over the cells rather than one cross-built project.
def onEach(ps: Seq[Project], task: String) = ps.map(p => s"${p.id}/$task")
  .mkString("; ", "; ", "")

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
  munitDiff.projectRefs ++ munit.projectRefs ++ tests.projectRefs ++
    Seq[ProjectReference](junit, plugin, docs): _*
).settings(
  unpublished,
  mimaPreviousArtifacts := Set.empty,
  crossScalaVersions := List(),
)

Global / excludeLintKeys ++= Set(mimaPreviousArtifacts)

def srcWithRoot(root: File, dir: String, cfg: String) = root / dir / "src" / cfg

def src(name: String, dir: String, cfg: String) = Def
  .setting[File](srcWithRoot((ThisBuild / baseDirectory).value / name, dir, cfg))

// crossProject's layout, wired by hand: a matrix has one base directory, so
// each cell names the trees it shares. Absent directories are harmless.
def roots(name: String, cfg: String, dirs: String*) = Def.setting[Seq[File]] {
  val variants = new mutable.ListBuffer[String]()
  variants += "scala"
  variants += "java"
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, minor)) =>
      variants += "scala-2"
      variants += "scala-2." + minor
      if (minor < 13) variants += "scala-pre-2.13"
    case Some((3, _)) => variants += "scala-3"
    case _ =>
  }
  val root = (ThisBuild / baseDirectory).value / name
  for (dir <- dirs; base = srcWithRoot(root, dir, cfg); variant <- variants)
    yield base / variant
}

def unmanagedMainSources(name: String, dirs: String*) = Def.settings(
  Compile / unmanagedSourceDirectories ++= roots(name, "main", dirs: _*).value
)

def unmanagedTestSources(name: String, dirs: String*) = Def.settings(
  Test / unmanagedSourceDirectories ++= roots(name, "test", dirs: _*).value
)

def unmanagedSources(name: String, dirs: String*) = Def.settings(
  unmanagedMainSources(name, dirs: _*),
  unmanagedTestSources(name, dirs: _*),
)
