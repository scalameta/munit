package munit.internal.junitinterface

import java.lang.annotation.Annotation

class Indent(val value: Int) extends Annotation {
  def annotationType(): Class[_ <: java.lang.annotation.Annotation] =
    classOf[Indent]
}
