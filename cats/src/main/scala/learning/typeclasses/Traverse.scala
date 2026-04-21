package learning.typeclasses

import cats.{Applicative, Functor, Monoid}
import tree.Tree
import cats.data.Const

/** Notes on [[cats.Traverse]]: top-level [[traverse]] on `List`, then [[LearningTraverse]]
  * (instances, sequencing, functor via `Id`, stub for Foldable).
  */

/** Polymorphic `List` `traverse` — same as in `Applicative.scala`. */
def traverse[F[_]: Applicative, A, B](as: List[A])(f: A => F[B]): F[List[B]] =
  as.foldRight(Applicative[F].pure(List.empty[B])) { (a: A, acc: F[List[B]]) =>
    Applicative[F].map2(f(a), acc)(_ :: _)
  }

object LearningTraverse:

  /** Gentle intro: **Traverse** (before you go deep)
    *
    * Given a function which returns a G effect, thread this effect
    * through the running of this function on all the values in F,
    * returning an F[B] in a G context. The function f is applied to each value in F,
    * and the result is a new F[B] in a G context.
    *
    * In short, Given a collection of data F[A], for each value apply the function
    * f which returns an effectful value. The result of traverse is the composition
    * of all these effectful values.
    *
    * You have a **structure** of `A`s (a `List`, a `Tree`, …) and a function
    * `f : A => G[B]`: for each element, `f` **returns a `B` wrapped in `G`** (same
    * wrapper every time). Here **`G`** might be `Option`, `Validated`, a task type, …
    * You want **one** outer `G` around the whole rebuilt structure —
    * e.g. `G[List[B]]` or `G[Tree[B]]` — instead of manually nesting `G` at each step.
    * That combined walk is **`traverse`**. It only needs **`Applicative[G]`** (not
    * always `Monad`): the `G` results are combined in a fixed shape using `map2`, `map3`, …
    *
    * *Why it is useful:*
    *
    * Use **`traverse`** when each **`f(a)` is a `G[B]`** (optional
    * result, async call, validation, …) — not a plain `B` — and you want **one** outer
    * `G` for the whole structure. All that really matters is the **`A => G[B]`** type;
    * `traverse` avoids hand-written loops, nested `flatMap`, or building `List[G[B]]`
    * and fixing the shape yourself.
    *
    * Think of it as generalising `Future.traverse` / `Future.sequence` to any
    * applicative `G`. This trait is the abstraction: **any** container `F` that can
    * expose `traverse` in terms of an applicative `G`. The full Cats type class is
    * [[cats.Traverse]], with laws and many instances.
    *
    * In Cats, `Traverse` also extends [[cats.Functor]] and [[cats.Foldable]]; add
    * Foldable at the bottom of this object when you are ready.
    */
  trait Traverse[F[_]]: // full Cats: also Functor + Foldable
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

  /** No second algorithm: this **only** calls [[tree.Tree#traverse]] so `Tree` fits the
    * same `Traverse[F]` shape as `List`. Generic code takes `(using ev: Traverse[F])` and
    * calls `ev.traverse(fa)(f)`.
    */
  given traverseForTree: Traverse[Tree] = new Traverse[Tree]:
    def traverse[G[_]: Applicative, A, B](fa: Tree[A])(f: A => G[B]): G[Tree[B]] =
      fa.traverse(f)

  /** Shows: `t.traverse(f)` and `traverseForTree.traverse(t)(f)` do the same work. */
  def treeMethodVsTypeclassExample(): Unit =
    import cats.syntax.all.*

    val t: Tree[Int] =
      Tree.Branch(
        10,
        Tree.Branch(4, Tree.Empty, Tree.Empty),
        Tree.Branch(7, Tree.Empty, Tree.Empty)
      )

    def halfIfEven(n: Int): Option[Int] = if n % 2 == 0 then Some(n / 2) else None

    // 1) Data-type API: you know you have a Tree
    val direct: Option[Tree[Int]] = t.traverse(halfIfEven)

    // 2) Type-class API: same result — call the named `given` instance explicitly
    val viaTypeclass: Option[Tree[Int]] = traverseForTree.traverse(t)(halfIfEven)

    println(s"direct (None because 10 and 7 are odd): $direct")
    println(s"via Traverse[Tree] (same): $viaTypeclass")

    val t2: Tree[Int] =
      Tree.Branch(
        8,
        Tree.Branch(4, Tree.Empty, Tree.Empty),
        Tree.Empty
      )
    println(s"all even → Some tree: ${t2.traverse(halfIfEven)}")

  /** `sequence` / `traverse(identity)` — turn `List[Option[Int]]` into `Option[List[Int]]`, etc.
    *
    * *Starting shape:* **`List[Option[Int]]`** — each cell is optional.
    * *Goal:* **`Option[List[Int]]`** — one `Option` for the whole list (`None` if any cell fails).
    * *Inside out:* outer `List` / inner `Option` becomes outer `Option` / inner `List`.
    *
    * *What you often want:* **`Option[List[Int]]`** — **one** `Option` that says whether
    * the **whole** list succeeded. If any element is `None`, the whole thing is `None`
    * (for `Option` this **short-circuits**; other `G`s behave differently, e.g.
    * `Validated` can accumulate errors).
    *
    * *Inside out:* that swap of wrappers is what people mean by turning the structure
    * “inside out”: outer `List` / inner `Option` becomes outer `Option` / inner `List`.
    *
    * *Why `identity`:* `identity` is polymorphic (`X => X` for whatever `X` you need).
    * Here `fa` is `List[A]` with **`A = Option[Int]`** (the element type is the whole
    * optional value, not bare `Int`). `traverse` wants `f: A => G[B]`; picking
    * `G = Option` and `B = Int` gives `f: Option[Int] => Option[Int]`, which is exactly
    * **`identity`** — not `Int => Int`, which would be wrong for `List[Option[Int]]`.
    * So `traverse` does not “map ints”; it **merges** the `Option` at each cell.
    * `xs.traverse(identity)` means “sequence the list of effects”.
    *
    * *`sequence`:* Cats defines **`sequence`** (syntax) as exactly that common case:
    * `fa.sequence` ≈ `fa.traverse(identity)` when the element type is `G[A]` for some
    * applicative `G`.
    *
    * *Traverse vs map-then-sequence:* for any `traverse`, **`fa.traverse(f)`** is the
    * same idea as **`fa.map(f).sequence`** — first attach an effect per element with
    * `f`, then flip the whole structure. So you usually write **`traverse`** directly
    * instead of `map` + `sequence`.
    */
  def noteOnSequencing(): Unit =
    import cats.syntax.all.*

    // Each position is already optional: List( layer )[ Option( layer )[ Int ] ]
    val effectfuls: List[Option[Int]] = List(Some(1), Some(2), None)

    // `identity` here is Option[Int] => Option[Int] (element type A = Option[Int]).
    // traverse(identity): no extra mapping — just combine the Option at each index.
    // Third element is None → whole result None (short-circuit).
    val traversed: Option[List[Int]] = effectfuls.traverse(identity)
    println(s"effectfuls.traverse(identity) → $traversed  // None because of the single None")
    // Same behaviour: sequence is defined as this “merge inner effects” pattern.
    val sequenced: Option[List[Int]] = effectfuls.sequence
    println(s"effectfuls.sequence → $sequenced  // identical to traverse(identity) here")
    // No None element whole sequence effects are good and traversed
    val allPresent: List[Option[Int]] = List(Some(1), Some(2), Some(3))
    println(s"all Some → inside-out succeeds: ${allPresent.sequence}  // Some(List(1, 2, 3))")

// ---------------------------------------------------------------------------
// Traversables are Functors — `map` via `traverse` and `Id`
// ---------------------------------------------------------------------------

/** Every `Traverse` is a [[cats.Functor]] and [[cats.Foldable]] in principle — 
  * you can implement `map` by choosing `G = Id` so `G[B]` carries no more information
  * than `B`. Same with implementing foldMap. we will see below. First Functor.
  *
  * Every `traverse` is a lawful `functor`. By carefully picking the `G` to use in
  * `traverse` we can implement `map`. First lets look at two signatures below.
  *
  * Both have an `F[A]` parameter and a similar `f` parameter. `traverse` expects
  * the return type of `f` to be `G[B]` whereas `map` just wants `B`. Similarly the
  * return type of `traverse` is `G[F[B]]` whereas for `map` its just `F[B]`. This
  * suggests we need to pick a `G` such that `G[A]` communicates exactly as much
  * information as `A`. We can conjure one up by simply wrapping an `A`
  * 
  */
object TraverseIsFunctorAndFoldable:

  final case class Id[A](value: A)
  def traverse[F[_]: Traverse, G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] = ???
  def map[F[_]: Traverse, A, B](fa: F[A])(f: A => B): F[B] = ???

  /**
    * In order to call `traverse` Id needs to be `Applicative` which is straightforward.
    * Note that while Id just wraps an `A`, it still a type constructor which matches the
    * shape required by `Applicative`.
    * 
    * [[cats.Id]] is the same idea: a **type constructor** `Id[_]` with `Id[A]` ≅ `A`,
    * so it can take a `G` slot in `traverse` even though there is no “real” effect.
    */
  given applicativeForId: Applicative[Id] = new Applicative[Id]:
    def pure[A](x: A): Id[A] = Id(x)
    def ap[A, B](ff: Id[A => B])(fa: Id[A]): Id[B] =
      Id(ff.value(fa.value))

  /**
    * Now we can implement map by wrapping and unwrapping Id as necessary.
    * Id is provided in cats as type alias. Id will be covered in `Id.scala`
    */
  trait Traverse[F[_]] extends Functor[F]:
    def traverse[G[_]: Applicative, A, B](fa: F[A])(ff: A => G[B]): G[F[B]]
    def map[A, B](fa: F[A])(f: A => B): F[B] =
      traverse(fa)(a => Id(f(a))).value

  given traverseForList: Traverse[List] = new Traverse[List]:
    def traverse[G[_]: Applicative, A, B](fa: List[A])(f: A => G[B]): G[List[B]] =
      fa.foldRight(Applicative[G].pure(List.empty[B])) { case (a, acc) =>
        Applicative[G].map2(f(a), acc)((x, y) => x :: y)
      }

  
  /** `Foldable` is “things you can fold”; `Traverse` is strictly stronger: you can get
    * `foldMap` (and then `foldRight` / `foldLeft`, though those via traverse are often slow)
    * by choosing the right [[Applicative]] for `traverse`. In Cats, `Traverse` still extends
    * `Foldable` with more direct implementations for folds.
    *
    * *What `foldMap` does:* walk every `a` in `fa`, map with `f : A => B`, then combine all
    * `B`s with [[Monoid]] — one summary value, no intermediate `List[B]` required.
    *
    * *Why `traverse` helps:* `traverse` already walks `F`’s shape and merges effects with
    * `map2` in that shape. Pick `G[X] = Const[B, X]` so the only “effect” is **monoid
    * accumulation** of `B`.
    *
    * *`Const[B, X]`:* “store a `B`, ignore `X`” (phantom `X`). When `B` has a [[Monoid]],
    * `Const[B, *]` is an [[Applicative]]: `pure` uses `empty`, `map2` uses `combine` on the
    * stored `B`s. So traversing with `a => Const(f(a))` combines every `f(a)` the same way
    * `foldMap` would.
    *
    * *Reading the body:*
    *  - `traverse[[X] =>> Const[B, X], ...]` — the applicative used for this walk is `Const[B, *]`.
    *  - `a => Const(f(a))` — each element contributes one `B` inside `Const`.
    *  - result type is roughly `Const[B, F[B]]`; `.getConst` drops the wrapper and returns the accumulated `B`.
    *
    * *Analogy (Functor step):* `Id` carries no extra data → recover `map` from `traverse`.
    * Here `Const[B, *]` carries only a monoid `B` → recover `foldMap` from `traverse`.
    *
    * See `Const` in the datatypes notes; `Const` is phantom in its second parameter and is
    * related to the idea of `Function.const`.
    */
  def foldMap[F[_]: Traverse, A, B: Monoid](fa: F[A])(f: A => B): B =
    val T = summon[Traverse[F]] // same as `(using T: Traverse[F])` at the def parameter list
    T
      .traverse[[X] =>> Const[B, X], A, B](fa)(a => Const(f(a))) // merge via Monoid inside Const
      .getConst // accumulated B

@main def traverseExamples(): Unit =
  import LearningTraverse.*
  import TraverseIsFunctorAndFoldable.{*, given}
  treeMethodVsTypeclassExample()
  noteOnSequencing()
  println(traverseForList.map(List(1, 2, 3))(_ * 10))
  // Map each number to its string, monoid combines with string concat → "1234"
  println(foldMap(List(1, 2, 3, 4))(_.toString))
  // Each element maps to itself; monoid combines with `+` → 10
  println(foldMap(List(1, 2, 3, 4))(identity))


/**
  * 
  * Further reading if interested:
    https://www.cs.ox.ac.uk/jeremy.gibbons/publications/iterator.pdf
  */
