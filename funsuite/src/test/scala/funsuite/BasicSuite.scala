package funsuite

import org.junit.runner.RunWith

@RunWith(classOf[Runner])
class BasicSuite extends Suite {
  override def beforeAll(context: BeforeAll): Unit = {
    println("beforeAll")
  }
  override def afterAll(context: AfterAll): Unit = {
    println("afterAll")
  }
  override def beforeEach(context: BeforeEach): Unit = {
    println("beforeEach: " + context.test.name)
  }
  override def afterEach(context: AfterEach): Unit = {
    println("afterEach: " + context.test.name)
  }
  test("not-implemented") {
    println("not-implemented")
    Thread.sleep(200)
    ???
  }
  test("pass") {
    Thread.sleep(400)
    println("pass")
    println("pass")
    assert(1 == 1)
  }
}
