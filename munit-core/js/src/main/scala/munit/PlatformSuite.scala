package munit

import scala.scalajs.reflect.annotation._

// This annotation is needed to ensure that munit.Suite subclasses can be
// reflectively classloaded.
@EnableReflectiveInstantiation
trait PlatformSuite
