package munit

import java.lang.annotation.Annotation
import scala.collection.mutable

/**
 * Metadata about a single test case.
 *
 * @param body the function to be evaluated for this test case.
 * @param tags the annotated tags for this test case.
 * @param location the file and line number where this test was defined.
 */
class GenericTest[T](
    val name: String,
    val body: () => T,
    val tags: Set[Tag],
    val location: Location
) extends Serializable {
  def this(name: String, body: () => T)(implicit loc: Location) =
    this(name, body, Set.empty, loc)
  def withName(newName: String): GenericTest[T] =
    copy(name = newName)
  def withBody[A](newBody: () => A): GenericTest[A] =
    copy(body = newBody)
  def withTags(newTags: Set[Tag]): GenericTest[T] =
    copy(tags = newTags)
  def tag(newTag: Tag): GenericTest[T] =
    withTags(tags + newTag)
  def withLocation(newLocation: Location): GenericTest[T] =
    copy(location = newLocation)

  def withBodyMap[A](newBody: T => A): GenericTest[A] =
    withBody[A](() => newBody(body()))

  private[this] def copy[A](
      name: String = this.name,
      body: () => A = this.body,
      tags: Set[Tag] = this.tags,
      location: Location = this.location
  ): GenericTest[A] = {
    new GenericTest(name, body, tags, location)
  }
  override def toString(): String = s"GenericTest($name, $tags, $location)"
  // NOTE(olafur): tests have reference equality because there's no reasonable
  // structural equality that we can use to compare the test body function.
  override def equals(obj: Any): Boolean = this.eq(obj.asInstanceOf[AnyRef])
  override def hashCode(): Int = System.identityHashCode(this)
  def annotations: Array[Annotation] = {
    val buf = new mutable.ArrayBuffer[Annotation](tags.size + 1)
    buf ++= tags
    buf += location
    buf.toArray
  }
}
