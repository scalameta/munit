package munit.internal.io

import scala.scalajs.js
import scala.util.Try

object JSIO {

  private def require(module: String): Option[js.Dynamic] = Try(
    js.Dynamic.global.require(module)
  ) // Node.js
    .orElse( // JSDOM
      Try(js.Dynamic.global.Node.constructor("return require")()(module))
    ).toOption
  val process: Option[js.Dynamic] = require("process")
  val path: Option[js.Dynamic] = require("path")
  val fs: Option[js.Dynamic] = require("fs")

  def cwd(): String = process match {
    case Some(p) => p.cwd().asInstanceOf[String]
    case None => "/"
  }

  def exists(path: String): Boolean = fs match {
    case Some(f) => f.existsSync(path).asInstanceOf[Boolean]
    case None => false
  }

  def isFile(path: String): Boolean = exists(path) &&
    (fs match {
      case Some(f) => f.lstatSync(path).isFile().asInstanceOf[Boolean]
      case None => false
    })

  def isDirectory(path: String): Boolean = exists(path) &&
    (fs match {
      case Some(f) => f.lstatSync(path).isDirectory().asInstanceOf[Boolean]
      case None => false
    })
}
