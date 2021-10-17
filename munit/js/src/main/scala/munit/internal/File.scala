package munit.internal

import java.net.URI
import munit.internal.JSIO

// obtained implementation by experimentation on the JDK.
class File(path: String) {
  def this(parent: String, child: String) =
    this(parent + File.separator + child)
  def this(parent: File, child: String) =
    this(parent.getPath, child)
  def this(uri: URI) =
    this(
      if (uri.getScheme != "file") {
        throw new IllegalArgumentException("URI scheme is not \"file\"")
      } else {
        uri.getPath
      }
    )
  def toPath: Path =
    Path(path)
  def toURI: URI = {
    val file = getAbsoluteFile.toString
    val uripath =
      if (file.startsWith("/")) file
      else "/" + file.replace(File.separator, "/")
    val withslash =
      if (isDirectory && !uripath.endsWith("/")) uripath + "/" else uripath
    new URI("file", null, withslash, null)
  }
  def getAbsoluteFile: File =
    toPath.toAbsolutePath.toFile
  def getAbsolutePath: String =
    getAbsoluteFile.toString
  def getParentFile: File =
    toPath.getParent.toFile
  def mkdirs(): Unit =
    throw new UnsupportedOperationException(
      "mkdirs() is not supported in Scala.js"
    )
  def getPath: String =
    path
  def exists(): Boolean =
    JSIO.exists(path)
  def isFile: Boolean =
    JSIO.isFile(path)
  def isDirectory: Boolean =
    JSIO.isDirectory(path)
  override def toString: String =
    path
}

object File {
  def listRoots(): Array[File] = Array(
    new File(
      JSIO.path match {
        case Some(p) => p.parse(p.resolve()).root.asInstanceOf[String]
        case None    => "/"
      }
      // if (JSIO.isNode) JSPath.parse(JSPath.resolve()).root
      // else "/"
    )
  )

  def separatorChar: Char =
    separator.charAt(0)

  def separator: String =
    JSIO.path match {
      case Some(p) => p.sep.asInstanceOf[String]
      case None    => "/"
    }

  def pathSeparator: String =
    JSIO.path match {
      case Some(p) => p.delimeter.asInstanceOf[String]
      case None    => ":"
    }
}
