def scala212 = "2.12.10"
def scala213 = "2.13.1"
inThisBuild(
  List(
    scalaVersion := scala213,
    crossScalaVersions := List(scala213, scala212),
    fork := true,
    testFrameworks := List(
      new TestFramework("funsuite.Framework")
    ),
    resolvers += Resolver.sonatypeRepo("public")
  )
)

skip in publish := true

lazy val funsuite = project
  .settings(
    libraryDependencies ++= List(
      "junit" % "junit" % "4.13",
      "com.geirsson" % "junit-interface" % "0.11.2",
      "com.lihaoyi" %% "sourcecode" % "0.1.9",
      "com.lihaoyi" %% "fansi" % "0.2.7",
      "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0",
      "com.lihaoyi" %% "pprint" % "0.5.6"
    )
  )
