package munit

class BeforeAll private[munit] ()
class AfterAll private[munit] ()

class BeforeEach(
    val test: Test
)

class AfterEach(
    val test: Test
)
