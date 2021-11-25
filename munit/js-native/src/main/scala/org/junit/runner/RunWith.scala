package org.junit.runner

import scala.annotation.Annotation
import scala.annotation.nowarn

@nowarn("msg=used")
class RunWith(cls: Class[_]) extends Annotation
