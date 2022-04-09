package munit.internal.io

import java.net.URI
import java.util

import scala.collection.JavaConverters._

// Rough implementation of java.nio.Path, should work similarly for the happy
// path but has undefined behavior for error handling.
case class Path(filename: String) {
  private[this] val escapedSeparator =
    java.util.regex.Pattern.quote(File.separator)

  private def adjustIndex(idx: Int): Int =
    if (isAbsolute) idx + 1 else idx
  def subpath(beginIndex: Int, endIndex: Int): Path =
    Path(
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
  def getName(index: Int): Path =
    Path(
      filename
        .split(escapedSeparator)
        .lift(adjustIndex(index))
        .getOrElse(throw new IllegalArgumentException)
    )
  def getParent: Path =
    JSIO.path match {
      case Some(path) =>
        Path(path.dirname(filename).asInstanceOf[String])
      case None =>
        throw new UnsupportedOperationException(
          "Path.getParent() is only supported in Node.js"
        )
    }

  def toAbsolutePath: Path =
    if (isAbsolute) this
    else Path.workingDirectory.resolve(this)
  def relativize(other: Path): Path =
    JSIO.path match {
      case Some(path) =>
        Path(
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
  def getFileName(): Path =
    JSIO.path match {
      case Some(path) =>
        Path(path.basename(filename).asInstanceOf[String])
      case None =>
        throw new UnsupportedOperationException(
          "Path.getFileName() is only supported in Node.js"
        )
    }
  def getRoot: Path =
    if (!isAbsolute) null
    else Path(File.separator)
  def normalize(): Path =
    JSIO.path match {
      case Some(path) =>
        Path(path.normalize(filename).asInstanceOf[String])
      case None =>
        throw new UnsupportedOperationException(
          "Path.normalize() is only supported in Node.js"
        )
    }
  def endsWith(other: Path): Boolean =
    endsWith(other.toString)
  def endsWith(other: String): Boolean =
    paths(filename).endsWith(paths(other))
  // JSPath.resolve(relpath, relpath) produces an absolute path from cwd.
  // This method turns the generated absolute path back into a relative path.
  private def adjustResolvedPath(resolved: Path): Path =
    if (isAbsolute) resolved
    else Path.workingDirectory.relativize(resolved)
  def resolveSibling(other: Path): Path =
    resolveSibling(other.toString)
  def resolveSibling(other: String): Path =
    JSIO.path match {
      case Some(path) =>
        adjustResolvedPath(
          Path(
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
  def resolve(other: Path): Path =
    resolve(other.toString)
  def resolve(other: String): Path =
    JSIO.path match {
      case Some(path) =>
        adjustResolvedPath(
          Path(path.resolve(filename, other).asInstanceOf[String])
        )
      case None =>
        throw new UnsupportedOperationException(
          "Path.normalize() is only supported in Node.js"
        )
    }
  def startsWith(other: Path): Boolean =
    startsWith(other.toString)
  def startsWith(other: String): Boolean =
    paths(filename).startsWith(paths(other))
  private def paths(name: String) =
    name.split(escapedSeparator)
  override def toString: String =
    filename
  def iterator(): util.Iterator[Path] =
    filename
      .split(File.separator)
      .iterator
      .map(name => Path(name): Path)
      .asJava
}

object Path {
  def workingDirectory: Path =
    Path(PlatformPathIO.workingDirectoryString)
}
