package munit.build

import com.typesafe.tools.mima.core.*

// More details about Mima:
// https://github.com/typesafehub/migration-manager/wiki/sbt-plugin#basic-usage
object Mima {
  val languageAgnosticCompatibilityPolicy: ProblemFilter = (problem: Problem) => {
    val fullName = problem match {
      case problem: TemplateProblem =>
        val ref = problem.ref
        ref.fullName
      case problem: MemberProblem =>
        val ref = problem.ref
        ref.fullName
    }

    !fullName.startsWith("munit.internal.")
  }
}
