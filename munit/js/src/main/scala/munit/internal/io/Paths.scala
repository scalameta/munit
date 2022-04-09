package munit.internal.io

import java.net.URI

object Paths {
  // NOTE: We can't use Scala-style varargs since those have a different jvm
  // signature than Java-style varargs. The boot classpath contains nio.file.Path
  // so call-sites to `get` will resolve to the original java.nio.file.Paths.get,
  // which results in a Scala.js linking error when using Scala varargs.
  def get(first: String, more: Array[String] = Array.empty): MunitPath = {
    val path =
      if (more.isEmpty) first
      else first + File.separator + more.mkString(File.separator)
    MunitPath(path)
  }

  def get(uri: URI): MunitPath = {
    if (uri.getScheme != "file")
      throw new IllegalArgumentException("only file: URIs are supported")
    val uripath = uri.getPath
    val parts = uripath.split('/').toList
    val (leading, trailing) = parts.span(_ == "")
    trailing match {
      case drive :: path if (drive.length == 2 && drive(1) == ':') =>
        MunitPath(trailing.mkString("\\"))
      case _ => MunitPath(uripath)
    }
  }
}
