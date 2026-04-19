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

/** Run: `sbt "cats/runMain learning.typeclasses.monadExamples"` */
@main def monadExamples(): Unit =
  OptionMonad.optionMonadExample()
