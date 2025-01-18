package munit

object ThrottleCpu {
  def run(): Unit = while (true) {
    // Some computationally intensive calculation
    1.to(1000).foreach(i => fib(i))
    println("Loop")
  }

  private final def fib(n: Int): Int =
    if (n < 1) 0 else if (n == 1) n else fib(n - 1) + fib(n - 2)

}
