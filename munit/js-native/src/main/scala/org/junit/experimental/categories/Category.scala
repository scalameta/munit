package org.junit.experimental.categories

import scala.annotation.Annotation
import scala.annotation.nowarn

@nowarn("msg=used")
class Category(classes: Array[Class[_]]) extends Annotation
