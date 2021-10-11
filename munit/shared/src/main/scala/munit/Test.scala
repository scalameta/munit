package munit

import java.lang.annotation.Annotation
import scala.collection.mutable
import scala.concurrent.Future

/**
 * Metadata about a single test case.
 *
 * @param body the function to be evaluated for this test case.
 * @param tags the annotated tags for this test case.
 * @param location the file and line number where this test was defined.
 */
final class Test(
    val name: String,
    val body: () => Future[Any],
    val tags: Set[Tag],
    val location: Location
) extends Serializable {
  def this(name: String, body: () => Future[Any])(implicit loc: Location) =
    this(name, body, Set.empty, loc)
  def withName(newName: String): Test =
    copy(name = newName)
  def withBody(newBody: () => Future[Any]): Test =
    copy(body = newBody)
  def withTags(newTags: Set[Tag]): Test =
    copy(tags = newTags)
  def tag(newTag: Tag): Test =
    withTags(tags + newTag)
  def withLocation(newLocation: Location): Test =
    copy(location = newLocation)

  def withBodyMap(newBody: Future[Any] => Future[Any]): Test =
    withBody(() => newBody(body()))

  private[this] def copy[A](
      name: String = this.name,
      body: () => Future[Any] = this.body,
      tags: Set[Tag] = this.tags,
      location: Location = this.location
  ): Test = {
    new Test(name, body, tags, location)
  }
  override def toString(): String = s"Test($name, $tags, $location)"
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
