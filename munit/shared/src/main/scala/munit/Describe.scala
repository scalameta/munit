package munit

sealed class Describe
    extends Tag("Describe")
    with munit.internal.junitinterface.Describe
object Describe extends Describe {
  override def annotationType(): Class[_ <: java.lang.annotation.Annotation] =
    classOf[Describe]
}
