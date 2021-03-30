addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.19")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.4")
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.5.4")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.27")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.5.0")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")

libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "1.113.13"

unmanagedSourceDirectories.in(Compile) +=
  baseDirectory.in(ThisBuild).value.getParentFile /
    "munit-sbt" / "src" / "main" / "scala"
