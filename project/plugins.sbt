val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.0.0")

addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.1.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.0")
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.3.4")
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.3.4")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.12")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.1")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.0-M2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "1.103.0"

unmanagedSourceDirectories.in(Compile) +=
  baseDirectory.in(ThisBuild).value.getParentFile /
    "munit-sbt" / "src" / "main" / "scala"
