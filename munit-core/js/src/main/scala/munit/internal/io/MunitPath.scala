package munit.internal.io

import java.net.URI
import java.util

import scala.collection.JavaConverters._

// Rough implementation of java.nio.Path, should work similarly for the happy
// path but has undefined behavior for error handling.
case class MunitPath(filename: String) {
  private[this] val escapedSeparator =
    java.util.regex.Pattern.quote(File.separator)

  private def adjustIndex(idx: Int): Int =
    if (isAbsolute) idx + 1 else idx
  def subpath(beginIndex: Int, endIndex: Int): MunitPath =
    MunitPath(
      filename
        .split(escapedSeparator)
        .slice(adjustIndex(beginIndex), adjustIndex(endIndex))
        .mkString
    )
  def toFile: File =
    new File(filename)
  def isAbsolute: Boolean = JSIO.path match {
    case Some(path) => path.isAbsolute(filename).asInstanceOf[Boolean]
    case None       => filename.startsWith(File.separator)
  }
  def getName(index: Int): MunitPath =
    MunitPath(
      filename
        .split(escapedSeparator)
        .lift(adjustIndex(index))
        .getOrElse(throw new IllegalArgumentException)
    )
  def getParent: MunitPath =
    JSIO.path match {
      case Some(path) =>
        MunitPath(path.dirname(filename).asInstanceOf[String])
      case None =>
        throw new UnsupportedOperationException(
          "Path.getParent() is only supported in Node.js"
        )
    }

  def toAbsolutePath: MunitPath =
    if (isAbsolute) this
    else MunitPath.workingDirectory.resolve(this)
  def relativize(other: MunitPath): MunitPath =
    JSIO.path match {
      case Some(path) =>
        MunitPath(
          path.relative(filename, other.toString()).asInstanceOf[String]
        )
      case None =>
        throw new UnsupportedOperationException(
          "Path.relativize() is only supported in Node.js"
        )
    }
  def getNameCount: Int = {
    val strippeddrive =
      if ((filename.length > 1) && (filename(1) == ':')) filename.substring(2)
      else filename
    val (first, remaining) =
      strippeddrive.split(escapedSeparator + "+").span(_.isEmpty)
    if (remaining.isEmpty) first.length
    else remaining.length
  }
  def toUri: URI = toFile.toURI
  def getFileName(): MunitPath =
    JSIO.path match {
      case Some(path) =>
        MunitPath(path.basename(filename).asInstanceOf[String])
      case None =>
        throw new UnsupportedOperationException(
          "Path.getFileName() is only supported in Node.js"
        )
    }
  def getRoot: MunitPath =
    if (!isAbsolute) null
    else MunitPath(File.separator)
  def normalize(): MunitPath =
    JSIO.path match {
      case Some(path) =>
        MunitPath(path.normalize(filename).asInstanceOf[String])
      case None =>
        throw new UnsupportedOperationException(
          "Path.normalize() is only supported in Node.js"
        )
    }
  def endsWith(other: MunitPath): Boolean =
    endsWith(other.toString)
  def endsWith(other: String): Boolean =
    paths(filename).endsWith(paths(other))
  // JSPath.resolve(relpath, relpath) produces an absolute path from cwd.
  // This method turns the generated absolute path back into a relative path.
  private def adjustResolvedPath(resolved: MunitPath): MunitPath =
    if (isAbsolute) resolved
    else MunitPath.workingDirectory.relativize(resolved)
  def resolveSibling(other: MunitPath): MunitPath =
    resolveSibling(other.toString)
  def resolveSibling(other: String): MunitPath =
    JSIO.path match {
      case Some(path) =>
        adjustResolvedPath(
          MunitPath(
            path
              .resolve(path.dirname(filename).asInstanceOf[String], other)
              .asInstanceOf[String]
          )
        )
      case None =>
        throw new UnsupportedOperationException(
          "Path.normalize() is only supported in Node.js"
        )
    }
  def resolve(other: MunitPath): MunitPath =
    resolve(other.toString)
  def resolve(other: String): MunitPath =
    JSIO.path match {
      case Some(path) =>
        adjustResolvedPath(
          MunitPath(path.resolve(filename, other).asInstanceOf[String])
        )
      case None =>
        throw new UnsupportedOperationException(
          "Path.normalize() is only supported in Node.js"
        )
    }
  def startsWith(other: MunitPath): Boolean =
    startsWith(other.toString)
  def startsWith(other: String): Boolean =
    paths(filename).startsWith(paths(other))
  private def paths(name: String) =
    name.split(escapedSeparator)
  override def toString: String =
    filename
  def iterator(): util.Iterator[MunitPath] =
    filename
      .split(File.separator)
      .iterator
      .map(name => MunitPath(name): MunitPath)
      .asJava
}

object MunitPath {
  def workingDirectory: MunitPath =
    MunitPath(JSIO.cwd())
}
