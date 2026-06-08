package munit

import munit.internal.PlatformCompat

import scala.scalanative.reflect.Reflect

class ReflectiveInstantiationSuite extends FunSuite {
  private val guardFqcn = classOf[GuardSuite].getName

  test("FunSuite subclass is reflectively instantiable")(assert(
    Reflect.lookupInstantiatableClass(guardFqcn).isDefined,
    clue = s"$guardFqcn was not registered for reflective instantiation",
  ))

  test("framework discovers and runs inherited FunSuite tests")(assertEquals(
    ReflectiveInstantiationProbe
      .countSuccesses(guardFqcn, PlatformCompat.getThisClassLoader),
    1,
  ))
}
