package munit.internal

trait EqualityCompat {

  /**
   * A Scala 2 "polyfill" for the upcoming multiversal equality in Scala 3
   *
   * http://dotty.epfl.ch/docs/reference/contextual/multiversal-equality.html
   */
  sealed abstract class Eql[A, B]
  implicit def universalEqualityForScala2[A, B]: Eql[A, B] =
    null.asInstanceOf[Eql[A, B]]
}
