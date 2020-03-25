---
author: Gabriele Petronella
title: Using ScalaCheck with MUnit
authorURL: https://twitter.com/gabro27
authorImageURL: https://github.com/gabro.png
---

Property-based testing is a popular testing style and its most widely used
implementation for Scala is the [ScalaCheck](https://www.scalacheck.org)
library.

Starting with version 0.7.0, MUnit introduces a dedicated integration for
ScalaCheck, which we'll explore in this blog post.

<!-- truncate -->

Once you've
[setup the `munit-scalacheck` module](/munit/docs/integrations/scalacheck.html)
you can extend the `ScalaCheckSuite` trait and start writing your properties,
for example:

```scala
import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class IntegerSuite extends ScalaCheckSuite {

  property("addition is commutative") {
    forAll { (n1: Int, n2: Int) =>
      n1 + n2 == n2 + n1
    }
  }

  property("0 is the identity of addition") {
    forAll { (n: Int) =>
      n + 0 == n
    }
  }

}
```

You can define properties using the familiar features of ScalaCheck, including
labels (`:|` and `|:`), conditional properties (`==>`) and custom generators.

The integration with MUnit also grants you the ability to use
[assertions](/munit/docs/assertions.html). This is especially useful when
testing multiple conditions of a property.

For example, using the ScalaCheck API you may write:

```scala
property("integer identities") {
  forAll { (n: Int) =>
    (n + 0 == n) :| "0 is the addition identity" &&
    (n * 1 == n) :| "1 is the multiplication identity"
  }
}
```

This is a boolean expression, which uses labels to better identify which part of
the expression fails.

For example, here's what happens if we change the second check to make it fail:

![scalacheck api fail](https://user-images.githubusercontent.com/691940/77507758-52eb5f80-6e69-11ea-9d01-a15bd7c79ba8.png)

For longer expressions, however, this may become inconvenient and may choose to
use a MUnit assertions instead:

```scala
property("integer identities") {
  forAll { (n: Int) =>
    assertEquals(n + 0, n, "0 is the addition identity")
    assertEquals(n * 1, n, "1 is the multiplication identity")
  }
}
```

Here's what happens when we introduce the same error as before:

![assertion api fail](https://user-images.githubusercontent.com/691940/77507636-1b7cb300-6e69-11ea-8325-63469d830d7d.png)

Using assertions for property checks has a few advantages:

- the expression is easier to read and write, since it doesn't need to be a long
  boolean expression concatenated with `&&`

- when one of the condition fails, MUnit will report an error highlighting the
  exact line that failed. Note that this makes the use of labels less necessary,
  since it's already clear which is the offending check, so you could
  potentially avoid them if you prefer.

## Conclusions

Writing property-based tests using MUnit and ScalaCheck is easy to setup and it
doesn't require to learn a new API.

You can optionally use MUnit assertions to break down complicated property
checks and get better error reporting.

Happy (property-based) testing!
