package munit.internal

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.Future

object PlatformCompat {
  def isIgnoreSuite(cls: Class[_]): Boolean =
    cls.getAnnotationsByType(classOf[munit.IgnoreSuite]).nonEmpty
  def await[T](f: Future[T], timeout: Duration): T = {
    Await.result(f, timeout)
  }
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false
}
