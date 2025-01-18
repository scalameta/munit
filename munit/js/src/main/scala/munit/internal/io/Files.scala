package munit.internal.io

import java.nio.charset.StandardCharsets
import java.{util => ju}

import scala.collection.JavaConverters._
import scala.scalajs.js

object Files {
  def readAllLines(path: MunitPath): ju.List[String] = {
    val bytes = readAllBytes(path)
    val text = new String(bytes, StandardCharsets.UTF_8)
    text.linesIterator.toSeq.asJava
  }
  def readAllBytes(path: MunitPath): Array[Byte] = {
    val jsArray = JSIO.fs match {
      case Some(fs) => fs.readFileSync(path.toString).asInstanceOf[js.Array[Int]]
      case None => new js.Array[Int](0)
    }
    val len = jsArray.length
    val result = new Array[Byte](len)
    var curr = 0
    while (curr < len) {
      result(curr) = jsArray(curr).toByte
      curr += 1
    }
    result
  }
  def exists(path: MunitPath): Boolean = JSIO.exists(path.toString)
}
