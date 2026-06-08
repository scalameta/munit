package munit

import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

// This annotation is needed to ensure that munit.Suite subclasses can be
// reflectively classloaded. On Scala 2.13 Native, the portable-scala-reflect
// annotation is not inherited from traits, so we use the Scala Native one.
@EnableReflectiveInstantiation
trait PlatformSuite
