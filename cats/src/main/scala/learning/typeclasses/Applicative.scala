package learning.typeclasses
import cats.Functor

object LearningApplicative:
  /** If we view Functor as the ability to work with a single effect,
    * Applicative encodes working with multiple independent effects. Functor is
    * not enough when you have *two* independent effectful values and want to
    * use both.
    *
    * Example: `Option[Int]` and `Option[Char]` — you need to combine them into
    * `Option[(Int, Char)]` when both succeed. `map` only lets you transform
    * *inside* one `F[_]`; `Applicative` adds `pure` (lift a plain value) and
    * either `ap` or `product` so you can combine *several* `F` values in
    * parallel (each may fail or carry context independently).
    *
    * This trait is the usual Cats-style API: `ap` + `pure`, with `map` derived.
    *
    *   - `pure(a)` — put a bare `a` inside `F` (e.g. `Some(a)`, `Right(a)`,
    *     singleton list).
    *   - `ap(ff)(fa)` — **Names:** `ff` = “`F` of **f**unction” (`F[A => B]`);
    *     `fa` = “`F` of **a**rgument” (`F[A]`). Same pattern as writing
    *     `function(argument)` in plain code — both slots are just wrapped in
    *     `F`. **What it does:** combine those two effects so that, when `F`
    *     allows it, the function inside `ff` is applied to the value inside
    *     `fa`, yielding `F[B]`. If either effect “fails” in the sense of that
    *     type (`None`, `Left`, etc.), the result usually fails too — you do not
    *     get a bare `B` out until both sides succeed.
    *
    * **Why not `map` alone?** `map` applies a *pure* `A => B` to `F[A]`. Here
    * the *function* itself lives in `F` (`F[A => B]`). You need `ap` to apply a
    * function that is still behind the same effect as the argument. (You can
    * get `F[A => B]` via `map` from other values, but then you still need `ap`
    * to finish the job.)
    *
    * Concrete (`Option`): let `ff: Option[Int => String] = Some(_.toString)`
    * and `fa: Option[Int] = Some(42)`. Then `ap(ff)(fa) == Some("42")`. If
    * either is `None`, the result is `None` — you cannot apply because one
    * effect already failed. (Same idea for `Either`: both must be `Right`.)
    *   - `map` — not a new primitive here: it is defined as `ap(pure(f))(fa)`.
    *     You lift the ordinary function `f` with `pure(f)` to get `F[A => B]`,
    *     then `ap` applies it to `fa`. So for this trait you implement `ap` and
    *     `pure`; `map` comes for free and stays consistent with functor laws as
    *     long as `ap`/`pure` obey applicative laws.
    *
    * Laws (informal):
    *
    *   - *Associativity (of tupling via product):* nesting pairs left vs right
    *     gives the same structure up to reshuffling tuples — `((a,b),c)` vs
    *     `(a,(b,c))`.
    *   - *Left identity:* tupling with "unit" `()` on the left, then taking the
    *     second part, gives back `fa` (up to isomorphism).
    *     `pure(()).product(fa).map(_._2) == fa`.
    *   - *Right identity:* same with unit on the right and `._1`.
    *     `fa.product(pure(())).map(_._1) == fa`.
    *
    * Precise statements (as in Cats docs) use `~` for "same shape"; with `map`
    * you can write actual equalities, e.g. for associativity:
    * `fa.product(fb).product(fc) == fa.product(fb.product(fc)).map { case (a, (b, c)) => ((a, b), c) }`.
    */
  trait Applicative[F[_]] extends Functor[F]:
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
    def pure[A](a: A): F[A]
    def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)

  /** Same abstraction as [[Applicative]], but the "combine two `F` values"
    * primitive is `product` instead of `ap`.
    *
    *   - `product(fa, fb)` — if both succeed, you get both results as a pair
    *     inside `F`; if either fails (for `Option`, `Either`, etc.), the
    *     combined effect fails in the usual way for that type.
    *
    * Why two formulations? They are equivalent: you can build `ap` from
    * `product` + `map`, or `product` from `ap` + `map`. Cats often shows
    * `product` first because it matches the intuition "pair up two independent
    * computations." Here `map` stays abstract — you implement it for each `F`
    * (unlike the `ap`-based trait where `map` has a default from `ap` +
    * `pure`).
    */
  trait ApplicativeWP[F[_]] extends Functor[F] {
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
    def pure[A](a: A): F[A]
  }

  /** Right-biased `Either`: fix the *left* type `L` (e.g. error message type),
    * vary the *right*.
    *
    * Typeclass `ApplicativeWP` wants `F[_]` — one type parameter. `Either` has
    * two (`L` and `A`), so we use a *type lambda*: `[[X] =>> Either[L, X]]`
    * means "the constructor that maps `X` to `Either[L, X]`" — `L` is fixed for
    * this instance, `X` is the hole `F` abstracts over.
    *
    * `product`:
    *   - Both `Right` → `Right` of the pair.
    *   - Any `Left` → short-circuit: the *first* `Left` encountered wins (left
    *     argument checked before the right), and its `L` is kept. That matches
    *     "fail fast" and the usual `Either` applicative behavior.
    *
    * `pure` — success path only: wrap in `Right`.
    *
    * `map` — functorial map on the right; `Left` unchanged.
    */
  given applicativeForEither[L]: ApplicativeWP[[X] =>> Either[L, X]] =
    new ApplicativeWP[[X] =>> Either[L, X]]:
      def product[A, B](fa: Either[L, A], fb: Either[L, B]): Either[L, (A, B)] =
        (fa, fb) match
          case (Right(a), Right(b)) => Right((a, b))
          case (Left(l), _)         => Left(l)
          case (_, Left(l))         => Left(l)

      def pure[A](a: A): Either[L, A] = Right(a)

      def map[A, B](fa: Either[L, A])(f: A => B): Either[L, B] = fa match {
        case Right(a) => Right(f(a))
        case Left(l)  => Left(l)
      }

object ApplicativeExamples:
  import cats.Applicative

  /** Mirrors the Cats doc idea: from `product` + `map` you can combine three
    * (or more) independent `F` values into one `F` of a flat tuple.
    *
    * '''Context bound `F[_]: Applicative`:'''
    *   - `F[_]` declares `F` as a type constructor (e.g. `Option`, `List`) —
    *     one type parameter that `product3` will abstract over.
    *   - `: Applicative` is a '''context bound''': it means “there must be a
    *     [[Applicative]] instance for `F` available where `product3` is
    *     called.” The compiler fills this from '''givens''' / '''implicit'''
    *     resolution (same mechanism).
    *   - Rough equivalent with an explicit parameter:
    *     {{{
    *     def product3[F[_], A, B, C](fa: F[A], fb: F[B], fc: F[C])(using ev: Applicative[F]): F[(A, B, C)]
    *     }}}
    *     Then you would use `ev` instead of summoning `Applicative[F]`.
    *
    * `val F = Applicative[F]` — explicitly '''summon''' that instance (retrieve
    * the `Applicative` dictionary for `F`) so you can call `product` and `map`
    * on it.
    */
  def product3[F[_]: Applicative, A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C]
  ): F[(A, B, C)] =
    val F = Applicative[F]
    val fabc = F.product(F.product(fa, fb), fc)
    F.map(fabc) { case ((a, b), c) => (a, b, c) }

  /** `someInt` is `Option[Int]`. For each `i` inside, build a *function*
    * waiting for `Char`: `i => (c: Char) => f(i, c)` has type
    * `Int => (Char => Double)`
    *
    * After `map`, you get `Option[Char => Double]`: e.g. `Some(5)` becomes
    * `Some(h)` where `h(c) = f(5, c)`. You still do *not* have `Option[Double]`
    * — the `Char` is not applied yet, and this `map` never sees `char`.
    * Combining with `char` needs `ap`, not another `map`.
    *
    * We have an Option[Char => Double] and an Option[Char] to which we want to
    * apply the function to, but map doesn't give us enough power to do that.
    * Hence, the need for ap.
    */
  def tryingToComposeTwoEffectfulValuesWithMap() =
    val f: (Int, Char) => Double = (i, c) => (i + c).toDouble
    val someInt: Option[Int] = Some(5)
    val char: Option[Char] = Some('a')
    val composed: Option[Char => Double] =
      someInt.map(i => (c: Char) => f(i, c))
    println(composed)

  /** Applicative composition: if `F` and `G` have [[Applicative]] instances, so
    * does the nested shape `F[G[·]]` (e.g. `Future` of `Option`).
    *
    * Two equivalent styles appear below — same idea, different API surface.
    */
  def applicativesCompose() =
    import cats.data.Nested
    import cats.syntax.all._
    import scala.concurrent.Future
    import scala.concurrent.Await
    import scala.concurrent.duration.*
    // `Future` combinators need an implicit `ExecutionContext` in scope.
    import scala.concurrent.ExecutionContext.Implicits.global

    // Two independent values: each is already “async optional” — a future that may hold an option.
    val x: Future[Option[Int]] = Future.successful(Some(5))
    val y: Future[Option[Char]] = Future.successful(Some('a'))

    // `Applicative[Future].compose[Option]` — build the instance for `Future[Option[A]]` from
    // `Future`’s and `Option`’s applicatives. Then `map2` runs both effects and applies `_ + _`
    // to the inner successes. `5 + 'a'` is `Int + Char` → `Int` (char code of `'a'` is 97; 5+97=102).
    val composed = Applicative[Future].compose[Option].map2(x, y)(_ + _)
    // Await (fine for a tiny demo) so the value is ready when we stringify.
    println(s"composed applicative ${Await.result(composed, 0.5.seconds)}")

    // Same composed applicative, but you ask for it via `Nested` — a zero-cost wrapper that
    // tells Cats which instance to use when several could apply. The last type arg of
    // `Nested[Future, Option, X]` is written as a type lambda in Scala 3 (`*` in Cats docs).
    // Here X is only a name for that remaining parameter. You could write
    // [SumValue] =>> Nested[Future, Option, SumValue] — same meaning.
    val nested =
      Applicative[[X] =>> Nested[Future, Option, X]]
        .map2(Nested(x), Nested(y))(_ + _)

    println(
      s"nested composed applicative ${Await.result(nested.value, 0.5.seconds)}"
    ) // Some(102);

/** Run: `sbt "cats/runMain learning.typeclasses.applicativeExample"` */
@main def applicativeExample(): Unit =
  import ApplicativeExamples.*
  val allOk = product3(Option(1), Option("hi"), Option(true))
  // Any `None` → whole thing short-circuits to `None` (independent effects, applicative style).
  val lost = product3(Some(1), None, Some(true))
  println(allOk) // Some((1,hi,true))
  println(lost) // None
  tryingToComposeTwoEffectfulValuesWithMap()
  applicativesCompose()
