package munit

import cats.effect.{ContextShift, IO}

abstract class CatsEffectSuite extends FunSuite {

  def munitContextShift: ContextShift[IO] =
    IO.contextShift(munitExecutionContext)

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms ++ List(munitIOTransform)

  final def munitIOTransform: ValueTransform =
    new ValueTransform("IO", { case e: IO[_] => e.unsafeToFuture() })

}
