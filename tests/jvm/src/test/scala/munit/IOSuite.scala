package munit

import cats.effect.IO

import scala.concurrent.duration._

class IOSuite extends CatsEffectSuite {

  test("nested fail".fail) {
    IO {
      IO.sleep(2.millis)(munitTimer)
        .flatMap { _ =>
          IO {
            IO.sleep(2.millis)(munitTimer)
              .flatMap { _ =>
                IO {
                  IO.sleep(2.millis)(munitTimer)
                    .map(_ => assertEquals(false, true))
                }
              }
          }
        }
    }
  }
  test("nested success") {
    IO {
      IO.sleep(2.millis)(munitTimer)
        .flatMap { _ =>
          IO {
            IO.sleep(2.millis)(munitTimer)
              .flatMap { _ =>
                IO {
                  IO.sleep(2.millis)(munitTimer)
                    .map(_ => assertEquals(true, true))
                }
              }
          }
        }
    }
  }
}
