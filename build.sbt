def scala212 = "2.12.10"
def scala213 = "2.13.1"
inThisBuild(
  List(
    scalaVersion := scala213,
    crossScalaVersions := List(scala213, scala212),
    fork := true,
    testFrameworks := List(
      new TestFramework("com.geirsson.junit.JUnitFramework")
    )
  )
)

skip in publish := true

lazy val funsuite = project
  .settings(
    libraryDependencies ++= List(
      "junit" % "junit" % "4.13",
      "com.geirsson" % "junit-interface" % "0.11.1-scalatest",
      "com.lihaoyi" %% "sourcecode" % "0.1.9",
      "com.lihaoyi" %% "fansi" % "0.2.7"
    )
  )
