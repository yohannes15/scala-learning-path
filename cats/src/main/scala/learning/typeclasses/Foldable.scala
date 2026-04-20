package learning.typeclasses

object LearningFoldable:
  import cats.Eval 
  /**
    * Foldable type class instances can be defined for data structures that can be 
    * folded to a summary value. 
    * 
    * Foldable[F] is implemented in terms of two basic methods:
    *
    * foldLeft(fa, b)(f) eagerly performs a left-associative fold over fa.
    * 
    * foldRight(fa, b)(f) lazily performs a right-associative fold over fa.
    *
    * Looking at Foldable examples below we can note that when defining some new data
    * structure, if we can define a `foldLeft` and `foldRight` we are able to provide 
    * many other useful operations, if not always the most efficient implementations,
    * over the structure without further implementation.
    */
  trait Foldable[F[_]]:
    /**
      * Walks the structure `fa` from the “left” (first element / outer layer
      * first, depending on `F`), threading an accumulator of type `B`.
      * 
      * Foldable[F] means: “for that structure, we know how to visit every `A`
      * in a fixed order and combine them with a seed `B` and a step `f`.
      *
      * **Left-associative** means grouping is `f(f(f(b, a1), a2), a3)` — the
      * same order as `List`'s `foldLeft`: combine seed with first `A`, then
      * combine that result with the next `A`, and so on. That matters when `f`
      * is not associative (e.g. subtracting numbers) or when you rely on
      * short-circuiting behavior you encode in `f`.
      * 
      * foldLeft is left-to-right, typically eager traversal: accumulator after 
      * each step is f(previousB, nextA)
      *
      * **Eager** here means the instance typically runs the combination as it
      * goes (no implicit laziness in the traversal itself). Contrast with a
      * right fold that can be lazy in the tail when `F` supports it.
      *
      * One lawful `F` is enough to define many derived folds (`foldMap`,
      * `find`, `exists`, …) — often expressed in terms of `foldLeft` or
      * `foldRight` in libraries like Cats.
      *
      * @param fa  structure holding `A` values
      * @param b   initial accumulator (often called “zero” or “z”)
      * @param f   combine current accumulator with each `A`
      * @return    final `B` after every `A` has been absorbed (or `b` if empty)
      */
    def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B

    /**
      * Walks the structure `fa` from the “right” (last element / inner layer
      * first in list-like shapes), threading an accumulator of type `B`.
      *
      * **Right-associative** means grouping is `f(a1, f(a2, f(a3, b)))` — the
      * same order as `List`'s `foldRight`: peel off the “rightmost” contribution
      * first, but combine so the seed `b` sits at the far right of the nested
      * calls. Order matters when `f` is not associative.
      *
      * **`Eval[B]`** (instead of plain `B`) lets the step `f` defer work: the
      * second argument is a *description* of the rest of the fold, not necessarily
      * a computed value yet. That supports **lazy** right folds (e.g. infinite
      * streams only consume what you `.value`) and **stack-safe** composition
      * when implemented with `Eval.defer` / `flatMap`, matching Cats'
      * `Foldable`.
      *
      * For many finite structures, `foldLeft` and `foldRight` agree on results
      * when `f` encodes an associative monoid; they differ in evaluation order,
      * laziness, and what laws you want instances to satisfy.
      * 
      * This will prevent operations which are lazy in their right hand argument
      * to traverse the entire structure unnecessarily. 
      * 
      * Unfortunately, since foldRight is defined on many collections - this 
      * extension clashes with the operation defined in Foldable. To get past 
      * this and make sure you're getting the lazy foldRight defined in Foldable, 
      * there's an alias foldr:
      *
      * @param fa  structure holding `A` values to visit
      * @param lb  lazy/eval-wrapped initial accumulator for the “after all `A`s” case
      * @param f   combine each `A` with the (possibly deferred) fold of the rest
      * @return    `Eval` of the final `B` when forced (e.g. `.value`)
      */
    def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]

    /**
      * This is eager and bad. Can fail on examples like
      * val allFalse = LazyList.continually(false) // an infinite list of false values
      * and if you wanted to reduce this to a single false value using the logical and (&&).
      * You intuitively know that the result of this operation should be false. It is not
      * necessary to consider the entire list in order to determine this result, you only
      * need to consider the first value. Using foldRight from the standard library will
      * try to consider the entire list, and thus will eventually cause an out-of-memory error:

        // beware! throws OutOfMemoryError, which is irrecoverable
        allFalse.foldRight(true)(_ && _)
      * With the lazy foldRight on Foldable, the calculation ends after looking at one value
      * @param fa structure holding `A` values to visit
      * @param b initial accumulator for the “after all `A`s” case
      * @param f combine each `A` with the (possibly deferred) fold of the rest
      * @return
      */
    def foldRightEAGER[A, B](fa: F[A], b: B)(f: (A, B) => B): B = ???

/**
  * Consider a simple list like List(1, 2, 3). You could sum the numbers of this
  * list using folds where 0 is the starting value (b) and integer addition (+) 
  * is the combination operation (f). Since foldLeft is left-associative, the
  * execution of this fold would look something like ((0 + 1) + 2) + 3. The execution 
  * of a similar foldRight-based solution would look something like 0 + (1 + (2 + 3)). 
  * In this case, since integer addition is associative, both approaches will
  * yield the same result. However, for non-associative operations, the two  methods 
  * can produce different results.These form the basis for many other operations
  */
@main def foldableExample() =
  import cats.syntax.all._
  import cats.Foldable
  import cats.Eval
  import cats.Later
  import cats.data.Nested

  def printfout(label: String, value: Any): Unit =
    println(s"$label: $value")

  // `fold` combines all elements using the monoid for `A` (here `String`); order follows the instance.
  // Result: "abc"
  printfout("fold", Foldable[List].fold(List("a", "b", "c")))

  // `foldMap` maps each element into some `B`, then combines with `B`'s monoid (digits as strings, then concat).
  // Result: "124"
  printfout("foldMap", Foldable[List].foldMap(List(1, 2, 4))(_.toString))

  // `foldK` needs a monoid for `G[A]` when `F` is `G[?]`; for nested lists it appends inner lists.
  // Result: List(1, 2, 3, 2, 3, 4)
  printfout("foldK", Foldable[List].foldK(List(List(1, 2, 3), List(2, 3, 4))))

  // `reduceLeftToOption` maps the first element, then folds left; empty `F` yields `None`.
  // Result: None
  printfout("reduceLeftToOption (empty)", Foldable[List].reduceLeftToOption(List[Int]())(_.toString)((s, i) => s + i))

  // Same, non-empty: string starts as "1", then appends "2","3","4" left-to-right.
  // Result: Some("1234")
  printfout("reduceLeftToOption", Foldable[List].reduceLeftToOption(List(1, 2, 3, 4))(_.toString)((s, i) => s + i))

  // `reduceRightToOption` uses `Eval`/`Later` so the tail can be lazy; order is right-associative on digits.
  // Result: Some("4321")
  printfout(
    "reduceRightToOption",
    Foldable[List].reduceRightToOption(List(1, 2, 3, 4))(_.toString)((i, s) => Later(s.value + i)).value
  )

  // Empty structure: nothing to reduce.
  // Result: None
  printfout(
    "reduceRightToOption (empty)",
    Foldable[List].reduceRightToOption(List[Int]())(_.toString)((i, s) => Later(s.value + i)).value
  )

  // `find` returns the first element satisfying the predicate, if any.
  // Result: Some(3)
  printfout("find", Foldable[List].find(List(1, 2, 3))(_ > 2))

  // `exists` is true if any element matches.
  // Result: true
  printfout("exists", Foldable[List].exists(List(1, 2, 3))(_ > 2))

  // `forall` is false if any element fails the predicate.
  // Result: false
  printfout("forall (fails one)", Foldable[List].forall(List(1, 2, 3))(_ > 2))

  // `forall` is true when every element passes.
  // Result: true
  printfout("forall (all pass)", Foldable[List].forall(List(1, 2, 3))(_ < 4))

  // `filter_` keeps elements matching the predicate and returns a plain `List` of them.
  // Result: List(1, 2)
  printfout("filter_", Foldable[Vector].filter_(Vector(1, 2, 3))(_ < 3))

  // `isEmpty` mirrors collection emptiness.
  // Result: false
  printfout("isEmpty (list)", Foldable[List].isEmpty(List(1, 2)))

  // For `Option`, only `None` is empty.
  // Result: true
  printfout("isEmpty (None)", Foldable[Option].isEmpty(None))

  // `nonEmpty` is the negation of `isEmpty`.
  // Result: true
  printfout("nonEmpty", Foldable[List].nonEmpty(List(1, 2)))

  // `toList` materializes `F[A]` as a `List[A]` in traversal order.
  // Result: List(1)
  printfout("toList (Some)", Foldable[Option].toList(Option(1)))

  // `None` has no elements to list.
  // Result: List()
  printfout("toList (None)", Foldable[Option].toList(None))

  // Helper for the `traverse_` / `sequence_` examples below.
  def parseInt(s: String): Option[Int] = scala.util.Try(Integer.parseInt(s)).toOption

  // `traverse_` runs an effectful action on each element and discards results; short-circuits on first `None`.
  // Result: Some(()) — all strings parsed.
  printfout("traverse_ (ok)", Foldable[List].traverse_(List("1", "2"))(parseInt))

  // First failure in the applicative effect stops the whole traverse (`None` from `parseInt("A")`).
  // Result: None
  printfout("traverse_ (fail)", Foldable[List].traverse_(List("1", "A"))(parseInt))

  // `sequence_` lifts a structure of effects into one effect; all must succeed.
  // Result: Some(())
  printfout("sequence_ (ok)", Foldable[List].sequence_(List(Option(1), Option(2))))

  // Any `None` in the list makes the whole sequence fail.
  // Result: None
  printfout("sequence_ (fail)", Foldable[List].sequence_(List(Option(1), None)))

  // `forallM` checks an effectful predicate under monad `G`; on `Option`, it stops at the first `Some(false)` (Cats short-circuits there).
  // Here `p(1) = Some(1 % 2 == 0) = Some(false)`, so later elements are never queried.
  // Result: Some(false)
  printfout("forallM", Foldable[List].forallM(List(1, 2, 3))(i => if (i < 2) Some(i % 2 == 0) else None))

  // `existsM` stops at the first `Some(true)`; if `p` ever returns `None`, the whole result is `None` (no true found “inside” the effect).
  // `p(1) = Some(false)` continues; `p(2) = None` aborts the search.
  // Result: None
  printfout("existsM (aborts None)", Foldable[List].existsM(List(1, 2, 3))(i => if (i < 2) Some(i % 2 == 0) else None))

  // Stays in `Some` until `i == 2`: `p(2) = Some(true)`, so exists succeeds without reaching `3`.
  // Result: Some(true)
  printfout("existsM (finds true)", Foldable[List].existsM(List(1, 2, 3))(i => if (i < 3) Some(i % 2 == 0) else None))

  // `sequence_` on `List[Eval[Unit]]` yields one `Eval` that runs the side effects when forced.
  // `Eval` value before forcing: a thunk (shown in REPL as `Now`/`Later` depending on optimization).
  val prints: Eval[Unit] = List(Eval.always(println(1)), Eval.always(println(2))).sequence_
  printfout("prints (Eval before .value)", prints)
  println("--- prints.value (next lines may be 1, 2 from Eval.always) ---")
  prints.value
  printfout("prints.value (return)", "()")

  // `dropWhile_` drops a prefix while the predicate holds; stops at first failure.
  // Result: List(5, 6, 7) — leading evens removed until 5.
  printfout("dropWhile_ (trim leading evens)", Foldable[List].dropWhile_(List[Int](2, 4, 5, 6, 7))(_ % 2 == 0))

  // First element is odd, so nothing is dropped.
  // Result: List(1, 2, 4, 5, 6, 7)
  printfout("dropWhile_ (no trim)", Foldable[List].dropWhile_(List[Int](1, 2, 4, 5, 6, 7))(_ % 2 == 0))

  // `Nested` tags a value as living in two functors at once; all inner options are defined.
  val listOption0 = Nested(List(Option(1), Option(2), Option(3)))
  printfout("listOption0", listOption0)

  // One `None` inside: folding can still combine defined values depending on monoid / instance behavior.
  val listOption1 = Nested(List(Option(1), Option(2), None))
  printfout("listOption1", listOption1)

  type NestedListOption[X] = Nested[List, Option, X]

  // `Foldable` for `NestedListOption` uses the `Int` monoid (addition) over defined values.
  // Result: 6
  printfout("fold (Nested all Some)", Foldable[NestedListOption].fold(listOption0))

  // With a `None` in the nest, Cats' instance sums the present ints (1 + 2).
  // Result: 3
  printfout("fold (Nested with None)", Foldable[NestedListOption].fold(listOption1))

  // Result = false
  val allFalse = LazyList.continually(false)
  printfout("foldRight Lazy", Foldable[LazyList].foldRight(allFalse, Eval.True)((a,b) => if (a) b else Eval.False).value)
  // Result = false
  printfout("foldr (alias for foldRight)", allFalse.foldr(Eval.True)((a,b) => if (a) b else Eval.False).value)
