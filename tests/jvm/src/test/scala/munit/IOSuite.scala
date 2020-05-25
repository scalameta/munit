package munit

import cats.effect.IO

class IOSuite extends CatsEffectSuite {
  test("nested".fail) {
    IO {
      Thread.sleep(2)
      IO {
        Thread.sleep(2)
        IO {
          Thread.sleep(2)
          ???
        }
      }
    }
  }
  test("nested success") {
    IO {
      Thread.sleep(2)
      IO {
        Thread.sleep(2)
        IO {
          Thread.sleep(2)
          assertEquals(true, true)
        }
      }
    }
  }
}
