package munit

import org.scalacheck.Prop

class ScalaCheckExceptionFrameworkSuite extends ScalaCheckSuite {
  override val scalaCheckInitialSeed =
    "9WNU_CSZAQwiiPWDlHs4NWI-knIDEKCsgGqdhZgNKnB="

  property("hide my stacks") {
    Prop.forAll { (i: Int) =>
      throw new StackOverflowError()
      Prop(true)
    }
  }
}

// NOTE(olafur): extracted this object into a class to avoid the following error with Dotty
// ==> X munit.FrameworkSuite.initializationError  0.002s java.lang.VerifyError: Bad type on operand stack
// Exception Details:
//   Location:
//     munit/ScalaCheckExceptionFrameworkSuite$.<init>()V @19: invokedynamic
//   Reason:
//     Type uninitializedThis (current frame, stack[0]) is not assignable to 'munit/ScalaCheckExceptionFrameworkSuite$'
//   Current Frame:
//     bci: @19
//     flags: { flagThisUninit }
//     locals: { uninitializedThis, 'java/lang/Class', 'java/lang/String' }
//     stack: { uninitializedThis }
//   Bytecode:
//     0x0000000: 1210 4cb2 0015 b200 1a12 1cb6 0020 b600
//     0x0000010: 234d 2aba 0037 0000 4eb2 003c b600 403a
//     0x0000020: 04b2 003c b600 443a 05b2 003c b600 483a
//     0x0000030: 062a 2b2c 1904 1905 1906 2dbb 004a 5912
//     0x0000040: 4c10 33b7 004f b700 522a b300 54b1
//
// FrameworkSuite.<init>(FrameworkSuite.scala:30)
class ScalaCheckExceptionFrameworkSuites
    extends FrameworkTest(
      classOf[ScalaCheckExceptionFrameworkSuite],
      """|munit.ScalaCheckExceptionFrameworkSuite.$anonfun$new$2(ScalaCheckExceptionFrameworkSuite.scala:11)
         |munit.ScalaCheckExceptionFrameworkSuite.$anonfun$new$2$adapted(ScalaCheckExceptionFrameworkSuite.scala:10)
         |==> failure munit.ScalaCheckExceptionFrameworkSuite.hide my stacks - Failing seed: 9WNU_CSZAQwiiPWDlHs4NWI-knIDEKCsgGqdhZgNKnB=
         |You can reproduce this failure by adding the following override to your suite:
         |
         |  override def scalaCheckInitialSeed = "9WNU_CSZAQwiiPWDlHs4NWI-knIDEKCsgGqdhZgNKnB="
         |
         |Exception raised on property evaluation.
         |> ARG_0: 0
         |> ARG_0_ORIGINAL: -860854860
         |> Exception: java.lang.StackOverflowError: null
         |""".stripMargin,
      tags = Set(OnlyJVM),
      onEvent = { event =>
        if (
          event.throwable().isDefined() &&
          event.throwable().get().getCause() != null
        ) {
          event
            .throwable()
            .get()
            .getCause
            .getStackTrace()
            .take(2)
            .mkString("", "\n", "\n")
        } else {
          ""
        }
      }
    )

object ScalaCheckExceptionFrameworkSuite
    extends ScalaCheckExceptionFrameworkSuites
