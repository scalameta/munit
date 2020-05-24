package munit

class NestedSuite extends BaseSuite {
  describe("primitives") {
    describe("positive") {
      test("positive1") {}
      test("positive2") {}
    }
    describe("negative") {
      test("negative1") {}
      test("negative2") {}
    }
  }

}
