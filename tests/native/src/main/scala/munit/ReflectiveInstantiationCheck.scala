package munit

import munit.internal.PlatformCompat

import scala.scalanative.libc.stdlib
import scala.scalanative.reflect.Reflect

object ReflectiveInstantiationCheck {
  def main(args: Array[String]): Unit = {
    val guardFqcn = classOf[GuardSuite].getName
    if (Reflect.lookupInstantiatableClass(guardFqcn).isEmpty)
      fail(s"$guardFqcn is not reflectively instantiable")
    val successes = ReflectiveInstantiationProbe
      .countSuccesses(guardFqcn, PlatformCompat.getThisClassLoader)
    if (successes != 1) fail(s"expected 1 success event, got $successes")
  }

  private def fail(message: String): Nothing = {
    Console.err.println(s"FAIL: $message")
    stdlib.exit(1)
    throw new AssertionError(s"FAIL: $message")
  }
}
