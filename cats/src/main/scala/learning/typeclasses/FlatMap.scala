package learning.typeclasses

import scala.annotation.tailrec
import cats.FlatMap

/** A closely related type class is FlatMap which is identical to Monad, minus the pure method. 
  * Indeed in [[cats.Monad]] is a subclass of [[cats.FlatMap]] (from which it gets flatMap) and
  * [[cats.Applicative]] (from which it gets pure).
  * 
  * [[cats.FlatMap]] is “[[cats.Monad]] without `pure`”: you get **`map`**, **`flatMap`**,
  * and **`tailRecM`**, but not **`Applicative`** / **`pure`**. Cats documents it under the
  * [Monad](https://typelevel.org/cats/typeclasses/monad.html) page (*FlatMap — a weakened Monad*)
  * rather than a separate tutorial. Some types have only `FlatMap` (e.g. `Map[K, ·]`).
  *
  * The laws for FlatMap are just the laws of Monad that don't mention pure.
  * One of the motivations for FlatMap's existence is that some types have FlatMap instances 
  * but not Monad - one example is Map[K, *]. Consider the behavior of pure for Map[K, A]. 
  * Given a value of type A, we need to associate some arbitrary K to it but we have no way 
  * of doing that. However, given existing Map[K, A] and Map[K, B] (or Map[K, A => B]), 
  * it is straightforward to pair up (or apply functions to) values with the same key. 
  * Hence Map[K, *] has an FlatMap instance.e`.
  */
object OptionFlatMap:
  import cats.FlatMap
  import scala.annotation.tailrec

  given optionFlatMap: FlatMap[Option] = new FlatMap[Option]:
    def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
    def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa.flatMap(f)

    @tailrec
    def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
      f(a) match
        case None             => None
        case Some(Left(a1))   => tailRecM(a1)(f)
        case Some(Right(b))   => Some(b)

    /** Intentionally **not** tail-recursive: chains `flatMap` and can overflow the stack
      * on deep recursion — contrast with `tailRecM` above. Not part of the `FlatMap` API.
      */
    def tailRecMOverFlows[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
      flatMap(f(a)) {
        case Right(b)    => Some(b)
        case Left(nextA) => tailRecM(nextA)(f)
      }

  def optionFlatMapExample(): Unit =
    println(optionFlatMap.flatMap(Some(1))(x => Some(x + 1))) // Some(2)
    println(
      optionFlatMap.tailRecM(11)(n =>
        if n == 0 then Some(Right("yeah!")) else Some(Left(n - 1))
      )
    )

/** Run: `sbt "cats/runMain learning.typeclasses.flatMapExamples"` */
@main def flatMapExamples(): Unit =
  OptionFlatMap.optionFlatMapExample()
