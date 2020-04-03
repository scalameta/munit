package munit

class FunFixtureSuite extends FunSuite {
  var tearDownName = ""
  val files = new FunFixture[String](
    setup = { test => test.name + "-setup" },
    teardown = { name => tearDownName = name }
  )

  override def afterAll(): Unit = {
    assertEquals(tearDownName, "basic-setup")
  }

  files.test("basic") { name => assertEquals(name, "basic-setup") }

}
