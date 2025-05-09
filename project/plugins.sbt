addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.3")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.7.1")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.19.0")
libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.7")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.8.0")

libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "2.48.2"
