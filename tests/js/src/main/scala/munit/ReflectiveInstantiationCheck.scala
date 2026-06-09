package munit

import munit.internal.PlatformCompat

import scala.scalajs.reflect.Reflect

object ReflectiveInstantiationCheck {
  def main(args: Array[String]): Unit = {
    val guardFqcn = classOf[GuardSuite].getName
    if (Reflect.lookupInstantiatableClass(guardFqcn).isEmpty)
      fail(s"$guardFqcn is not reflectively instantiable")
    val successes = ReflectiveInstantiationProbe
      .countSuccesses(guardFqcn, PlatformCompat.getThisClassLoader)
    if (successes != 1) fail(s"expected 1 success event, got $successes")
  }

  private def fail(message: String): Nothing =
    throw new AssertionError(s"FAIL: $message")
}
