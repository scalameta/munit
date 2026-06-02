package munit

import org.portablescala.reflect.annotation.EnableReflectiveInstantiation

// This annotation is needed to ensure that munit.Suite subclasses can be
// reflectively classloaded.
@EnableReflectiveInstantiation
trait PlatformSuite
