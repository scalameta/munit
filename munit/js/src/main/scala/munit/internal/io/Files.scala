package munit.internal.io

import scala.scalajs.js
import java.{util => ju}
import java.nio.charset.StandardCharsets

import scala.collection.JavaConverters._

object Files {
  def readAllLines(path: Path): ju.List[String] = {
    val bytes = readAllBytes(path)
    val text = new String(bytes, StandardCharsets.UTF_8)
    text.linesIterator.toSeq.asJava
  }
  def readAllBytes(path: Path): Array[Byte] = {
    val jsArray = JSIO.fs match {
      case Some(fs) =>
        fs.readFileSync(path.toString).asInstanceOf[js.Array[Int]]
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
}
