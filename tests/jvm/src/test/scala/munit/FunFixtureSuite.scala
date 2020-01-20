package munit

import java.nio.file.Path
import java.nio.file.Files

class FunFixtureSuite extends FunSuite {
  val files = new FunFixture[Path](
    setup = { test =>
      Files.createTempFile("tmp", test.name)
    },
    teardown = { file =>
      Files.deleteIfExists(file)
    }
  )

  files.test("basic") { file =>
    require(Files.isRegularFile(file), s"Files.isRegularFile($file)")
  }

}
