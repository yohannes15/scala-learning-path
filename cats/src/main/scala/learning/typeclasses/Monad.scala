package learning.typeclasses

import scala.annotation.tailrec

/** In Cats, [[cats.Monad]] **extends** [[cats.Applicative]] (and [[cats.FlatMap]]):
  * you keep **`pure`** / applicative combining, and add **sequential** chaining via
  * **`flatMap`**, plus **joining** nested effects via **`flatten`**. See
  * [Monad](https://typelevel.org/cats/typeclasses/monad.html) on the Typelevel site.
  *
  * Intuition: a monad is an applicative where you can also **merge** one extra
  * layer of the same `F` — turn `F[F[A]]` into `F[A]`. That operation is usually
  * called **`flatten`** (or `join`), like `Option` / `List` / `Vector` in the
  * standard library.
  */
def flattenExamplesStdLib(): Unit =
  println(Option(Option(1)).flatten) // Some(1): nested Some collapsed
  println(Option(None).flatten) // None: outer Some, inner None → None
  println(List(List(1), List(2, 3)).flatten) // List(1, 2, 3)

/** A `given` `Monad[Option]` built from `Applicative[Option]` (Cats doc pattern).
  *
  * **`pure`** — wrap a normal value: `a` → `Some(a)` here. Same idea as
  * [[cats.Applicative]].
  *
  * **`flatMap`** — “I have an `Option[A]`. If it’s a `Some`, I get an `A` and may
  * want **another** `Option` that depends on that `A`.” The step you pass in has
  * type `A => Option[B]`, not `A => B`. So the next success/failure can depend on
  * the value you found.
  *
  * Compare to **`map`**: `map` needs `A => B` (one plain result). **`flatMap`**
  * needs `A => Option[B]` (a *new* optional result). Example: `Some(1).flatMap(x =>
  * if x > 0 then Some(x * 2) else None)` — the function itself returns an
  * `Option`; `flatMap` stitches the layers together.
  *
  * Implementation below: `map` with that function gives `Option[Option[B]]`;
  * **`flatten`** turns that into `Option[B]`. In Scala, **`for`** with `<-` on
  * `Option`/`List`/… is rewritten to **`flatMap`** (and `map` for `yield`).
  *
  * *More technical (general `F[_]` and Cats):*
  *   - `flatMap` is also called **`bind`** in other FP libraries. Abstractly:
  *     `F[A]` and `(A => F[B])` → `F[B]`. The next effect can **depend on** the
  *     value inside `fa`, so steps are **sequential**. That differs from
  *     applicative **`mapN` / `map2`**, where several `F` values are fixed first
  *     and combined without “later depends on earlier” in the same way.
  *   - Same work as **map then join one level**: `fa.flatMap(f) == fa.map(f).flatten`
  *     when `f : A => F[B]`.
  *   - In Cats, **`flatMap` + `pure`** are usually the primitives; **`map`** and
  *     **`flatten`** follow. For example `flatten : F[F[A]] => F[A]` can be written
  *     as `ffa.flatMap(identity)` (you rarely implement only `flatten`).
  *
  * *`tailRecM` (required by Cats in addition to `flatMap` and `pure`):*
  *   Monadic recursion written with plain `flatMap` is easy to get wrong on the
  *   JVM — it can **overflow the stack**. Cats therefore requires **`tailRecM`**
  *   on every [[cats.Monad]]: it encodes **stack-safe** recursion in the monad
  *   (see Phil Freeman, *Stack Safety for Free*). Library code in Cats that needs
  *   monadic recursion goes through `tailRecM`. The `Option` implementation below
  *   is **`@tailrec`** so the loop does not grow the call stack.
  *
  *   See [tailRecM](https://typelevel.org/cats/typeclasses/monad.html#tailrecm)
  *   in the Cats Monad docs.
  *
  *   *Stack safety (summary from the same Cats section):*
  *   - A lawful **`tailRecM`** lets you loop until **`Right`** without blowing the
  *     stack, no matter how many steps. Recursive **`flatMap`** can be refactored
  *     to **`tailRecM`**; if your **`FlatMap`/`Monad`** laws hold, you do not
  *     hand-manage stack safety at every call site.
  *   - Writing a **lawful** `tailRecM` is not always obvious: **`Future`** may be
  *     fine with a simple version; **`Option`**, **`Try`**, … usually need a true
  *     tail call (often **`@tailrec`**); **collections** need other patterns (see
  *     Cats’ **`List`**). When stuck, find a Cats instance for a similar type.
  *   - If a lawful `tailRecM` is **impractical or impossible**, Cats suggests
  *     reporting it; for tests you can use **`MonadTests[F].stackUnsafeMonad[A,B,C]`**
  *     instead of **`MonadTests[F].monad[A,B,C]`** to check the rest of the monad
  *     laws **without** the stack-safety obligation on `tailRecM`.
  */

object OptionMonad:
  given monadForOption(using app: cats.Applicative[Option]): cats.Monad[Option] =
    new cats.Monad[Option]:
      def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
        app.map(fa)(f).flatten // map with `f : A => Option[B]` gives `Option[Option[B]]`; then flatten
      def pure[A](a: A): Option[A] = app.pure(a)

      @tailrec
      def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
        f(a) match
          case None             => None // base case
          case Some(Left(next)) => tailRecM(next)(f) // continue the recursion
          case Some(Right(b))   => Some(b) // recursion done

      def tailRecMOverFlows[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
        flatMap(f(a)) {
            case Right(b) => pure(b)
            case Left(nextA) => tailRecM(nextA)(f)
        }

  def optionMonadExample(): Unit =
    println(monadForOption.flatMap(Some(1))(x => Some(x + 1))) // Some(2)
    println(
      monadForOption.tailRecM(10)(n =>
        if n == 0 then Some(Right("done")) else Some(Left(n - 1))
      )
    )

  /**
    * Monad provides the ability to choose later operations in a sequence based on
    * the results of earlier ones. This is embodied in `ifM`, which lifts an if
    * statement into the monadic contexxt
    */
  def ifMExample() = 
    import cats.Monad
    import cats.syntax.all.*
    val res = Monad[List].ifM(List(true, false, true))(
      ifTrue = List(1, 2),
      ifFalse = List(3, 4)
    )
    println(res) // List(1, 2, 3, 4, 1, 2)

  /**
    * Composition and monad transformers (hand-rolled `OptionT`)
    * ------------------------------------------------------------
    *
    * *Learning note:* This section is **optional on first read**. Getting comfortable
    * with `map`, `flatMap`, and `for` on `Option`, `List`, and `Future` matters more
    * early on; return here when [[cats.Monad]] feels familiar.
    *
    * *What to remember about `OptionT` and `tailRecM` here:*
    *   - **`OptionT`** is the usual pattern for **`Option` inside another effect** — values look
    *     like `F[Option[A]]` (e.g. each element of a `List` may be present or absent).
    *     The wrapper lets you chain with `map` / `flatMap` as if `Option` lived inside
    *     `F`. In real projects, use [[cats.data.OptionT]] instead of hand-rolling this.
    *   - **`tailRecM`** shows up because Cats requires every `Monad` to support
    *     **stack-safe** recursion; this instance delegates to the outer `F`’s
    *     `tailRecM`. Most application code does not implement this — only when you
    *     define new `Monad` instances yourself.
    *
    * Functors and applicatives compose in a fairly uniform way; monads do not: in
    * general there is no monad for `M[N[_]]` given arbitrary monads `M` and `N`.
    * Many **practical** combinations still work if you fix the inner monad and spell
    * out how `pure`, `flatMap`, and `tailRecM` should behave for `M` “wrapping” `N`.
    *
    * Here the **inner** effect is `Option` and the **outer** is any monad `F` with a
    * [[cats.Monad]] instance. Values have the shape `F[Option[A]]`: an `F`-ful of
    * optional results (e.g. `List(Some(1), None, Some(3))` — each position may carry
    * a value or not). `OptionT[F, A]` is a thin wrapper so you can write `map` /
    * `flatMap` as if `Option` lived inside `F`.
    *
    * This pattern is a **monad transformer**. The code below follows the
    * [Composition](https://typelevel.org/cats/typeclasses/monad.html#composition)
    * section of the Cats `Monad` docs. [[cats.data.OptionT]] in the library is the
    * same idea plus many extra methods (`fold`, `getOrElse`, subtypes, …).
    *
    * *`pure`* — Lift a plain `a` into “always present”: `F.pure(Some(a))`.
    *
    * *`flatMap`* — For each `F`, look at the inner `Option`. `None` short-circuits to
    * `F.pure(None)` without calling `f`. `Some(a)` runs the next step and keeps a
    * single `F[Option[B]]` (one layer of `F`, one of `Option`).
    *
    * *`tailRecM`* — Recursive programs must use the outer `F`’s stack-safe `tailRecM`.
    * The step function returns `OptionT[F, Either[A, B]]`, i.e. `F[Option[Either[A, B]]]`.
    * We translate that to `F[Either[A, Option[B]]]` so `F.tailRecM` can drive the
    * loop: `None` means “done, with no value” (`Right(None)`); `Some(Left(a))` means
    * “continue with state `a`”; `Some(Right(b))` means “done with `Some(b)`”, encoded
    * as `Right(Some(b))`. Here `b0` is an `Either[A, B]`; `b0.map(Some(_))` uses
    * `Either`’s `map` on the right branch only (`Left` unchanged, `Right(b)` becomes
    * `Right(Some(b))`).
    */
  def compositionAndTransformationExample(): Unit =
    import cats.Monad
    import cats.syntax.all.*

    /** `F` on the outside, `Option` on the inside — see scaladoc above. */
    case class OptionT[F[_], A](value: F[Option[A]])

    given optionTMonad[F[_]](using F: Monad[F]): Monad[[X] =>> OptionT[F, X]] =
      new Monad[[X] =>> OptionT[F, X]]:
        def pure[A](a: A): OptionT[F, A] =
          OptionT(F.pure(Some(a))) // present at every “slot” inside F

        def flatMap[A, B](fa: OptionT[F, A])(f: A => OptionT[F, B]): OptionT[F, B] =
          OptionT(
            F.flatMap(fa.value) {
              case None => F.pure(None) // inner None: skip f, stay absent
              case Some(a) => f(a).value // run the next OptionT step
            }
          )

        def tailRecM[A, B](a: A)(f: A => OptionT[F, Either[A, B]]): OptionT[F, B] =
          OptionT(
            // Delegate recursion to F; peel Option by mapping to Either[A, Option[B]]
            F.tailRecM(a)(a0 =>
              F.map(f(a0).value)(
                {
                  // No inner Either: finished with “missing” in the Option layer
                  case None => Either.right[A, Option[B]](None)
                  // b0: Either[A, B] — pass Left(a) through, wrap Right(b) as Right(Some(b))
                  case Some(b0) => b0.map(Some(_))
                }
              )
            )
          )

    // List is the outer F: three optional “cells”; map doubles only Somes
    val x: OptionT[List, Int] = OptionT(List(Some(1), None, Some(3)))
    val y = x.map(_ * 2)
    println(y.value) // List(Some(2), None, Some(6))

/** Run: `sbt "cats/runMain learning.typeclasses.monadExamples"` */
@main def monadExamples(): Unit =
  OptionMonad.optionMonadExample()
  OptionMonad.ifMExample()
  OptionMonad.compositionAndTransformationExample()
