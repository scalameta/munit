package munit

import munit.internal.PlatformCompat

class PlatformCompatSuite extends FunSuite {

  test("Scala.js: eliminate false branches")(
    // the branch should be eliminated by the Scala.js linker
    // otherwise, the compilation will fail, because TrieMap doesn't exist in Scala.js
    if (PlatformCompat.isJVM)
      assert(collection.concurrent.TrieMap[String, String]().isEmpty)
  )

}
