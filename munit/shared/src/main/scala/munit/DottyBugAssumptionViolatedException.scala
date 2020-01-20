package munit

// FIXME(gabro): Constructing a `org.junit.AssumptionViolatedException` causes Dotty to crash
// so we use our own Exception class to work around it.
// See https://github.com/lampepfl/dotty/issues/7990
class DottyBugAssumptionViolatedException(message: String)
    extends RuntimeException
