package munit

import org.portablescala.reflect.annotation.EnableReflectiveInstantiation

// This annotation ensures that munit.Suite subclasses can be reflectively
// instantiated on Scala.js and Scala Native (no-op on the JVM).
@EnableReflectiveInstantiation
trait PlatformSuite
