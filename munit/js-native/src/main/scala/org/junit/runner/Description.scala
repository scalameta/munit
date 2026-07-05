package org.junit.runner

import java.lang.annotation.Annotation

class Description(
    cls: Option[Class[_]] = None,
    methodName: Option[String] = None,
    annotations: List[Annotation] = Nil,
    children: List[Description] = Nil,
) {
  def addChild(description: Description): Description =
    new Description(cls, methodName, annotations, description :: children)
  // Empty for a suite-level description (no method); the reporter renders such an
  // event with the fully-qualified suite name instead.
  def getMethodName: String = methodName.getOrElse("")
  def getTestClass: Option[Class[_]] = cls
  def getAnnotations: List[Annotation] = annotations
}

object Description {
  def createSuiteDescription(cls: Class[_]): Description =
    new Description(cls = Some(cls))
  def createTestDescription(
      cls: Class[_],
      name: String,
      annotation: Annotation*
  ): Description = new Description(
    cls = Some(cls),
    methodName = Some(name),
    annotations = annotation.toList,
  )
  def createTestDescription(cls: Class[_], name: String): Description =
    new Description(cls = Some(cls), methodName = Some(name))
}
