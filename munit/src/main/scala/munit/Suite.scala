package munit

import scala.collection.mutable

class Suite {
  private[munit] val tests = mutable.ArrayBuffer.empty[Test]

  def beforeAll(context: BeforeAll): Unit = ()
  def afterAll(context: AfterAll): Unit = ()

  def beforeEach(context: BeforeEach): Unit = ()
  def afterEach(context: AfterEach): Unit = ()

  def test(name: String, tags: Tag*)(body: => Unit): Unit = {
    tests += new Test(name, () => body, tags.toSet)
  }
}
