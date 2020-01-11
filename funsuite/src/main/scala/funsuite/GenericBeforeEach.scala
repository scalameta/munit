package funsuite

class GenericBeforeEach[T](
    val test: GenericTest[T]
)

class GenericAfterEach[T](
    val test: GenericTest[T]
)
