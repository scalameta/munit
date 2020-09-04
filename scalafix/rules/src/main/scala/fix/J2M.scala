package fix

import scalafix.v1._
import scala.meta._

class J2M extends SemanticRule("J2M") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    // pprint.log(doc.input.syntax)
    val assertEquals = SymbolMatcher.exact(
      "org/junit/Assert#assertEquals(+1).", // JVM
      "org/junit/Assert.assertEquals(+1)." // Scala.js
    )
    doc.tree.collect {
      case Term.Apply(assertEquals(_), expected :: actual :: Nil) =>
        Patch.replaceTree(expected, actual.syntax) +
          Patch.replaceTree(actual, expected.syntax)
    }.asPatch
  }

}
