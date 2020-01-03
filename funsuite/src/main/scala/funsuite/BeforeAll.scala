package funsuite

class BeforeAll private[funsuite] ()
class AfterAll private[funsuite] ()

class BeforeEach(
    val test: Test
)

class AfterEach(
    val test: Test
)
