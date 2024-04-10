package munit.internal.difflib

trait Equalizer[T] {
  def equals(original: T, revised: T): Boolean
}
object Equalizer {
  def default[T]: Equalizer[T] = new Equalizer[T] {
    override def equals(original: T, revised: T): Boolean = {
      original == revised   //original.equals(revised)
    }
  }
}
