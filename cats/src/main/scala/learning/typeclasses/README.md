# Typeclasses

## Why this is useful

Type classes are a powerful tool used in functional programming to enable ad-hoc polymorphism, more commonly known as overloading. Where many object-oriented languages leverage subtyping for polymorphic code, functional programming tends towards a combination of parametric polymorphism (think type parameters, like Java generics) and ad-hoc polymorphism.

```scala
def sumInts(list: List[Int]): Int = list.foldRight(0)(_ + _)

def concatStrings(list: List[String]): String = list.foldRight("")(_ ++ _)

def unionSets[A] (list: List[Set[A]]): Set[A] = list.foldRight(Set.empty[A])(_ union _)
```

The above snippets show code that sums a list of integers, concatenates a list of strings, and unions a list of sets.
All of these follow the same pattern: 
    - an initial value (0, "", Set.empty[A])
    - a combining function (+, ++, union)

We'd like to abstract over this so we can write the function once instead of once for every type so we pull out the
necessary pieces into an interface (type class)

```scala
// The name Monoid is taken from abstract algebra, which specifies precisely this behavior
// A monoid is a set with a binary operation that is associative and has an identity element.
// A binary operation is a fn that takes two elements of the set and returns an element of the set.
// In this case, the set is the type A, the binary operation is combine, and the identity element is empty.
// The combine function is associative, meaning that the order of operations does not matter.
trait Monoid[A]:
    def empty: A // the identity element
    def combine(x: A, y: A): A // the associative binary operation

// Implementation for Int
val intAdditionMonoid: Monoid[Int] = new Monoid[Int]:
    def empty: Int = 0
    def combine(x: Int, y: Int): Int = x + y

// function against the interface to combine all elements in a list
def combineAll[A] (list: List[A], monoid: Monoid[A]): A = 
  list.foldRight(monoid.empty)(monoid.combine)
```

## Type classes vs. subtyping

The definition above takes an actual monoid argument instead of doing the usual object-oriented practice
of using subtype constraints. 

```scala
// Subtyping at the call site — same “where does empty come from?” issue
def combineAll[A <: Monoid[A]] (list: List[A]): A =
  list match
    case Nil =>
      ??? // no `A` in scope: cannot call `.empty` or build a witness from types alone
    case nonEmpty =>
      // any element can serve as the receiver for `combine` if the operation ignores `this`
      nonEmpty.reduce((x, y) => x.combine(x, y))
```

This has a subtle difference with the earlier explicit example. In order to seed the `foldRight` with the
empty value, we need to get a hold of it given only the type `A`. Taking `Monoid[A]` as an argument gives
us this by calling the appropriate `empty` method on it.

With the OO / subtype sketch, `empty` is something you’d take from an `A` (a receiver for `empty` / 
`combine`), but you only get those from the list. If the list is empty, you have no values to work with 
and therefore can’t get the empty value. Not to mention the oddity of getting a constant value from a 
non-static object.

So you’ve constrained the type in a way that doesn’t automatically give you a value-free way to produce 
the identity element. The identity isn’t something you should have to get from an element; it’s a property
of the type’s monoid structure, not of a particular value.

### Another example

For another motiviating difference, consider the simple pair type.

```scala
final case class Pair[A, B] (first: A, second: B)
```

Defining a `Monoid` for `Pair` => `Monoid[Pair[A, B]]` depends on the ability to define a `Monoid[A]` and a 
`Monoid[B]`, where the definition is point-wise, i.e. the first element of the first pair combines with the 
first element of the second pair and the second element of the pair combines with the second element of the
second pair. With subtyping such a constraint would be encoded as something like 

```scala
/// Subtyping
final case class Pair[A <: Monoid[A], B <: Monoid[B]] (first: A, second: B) extends Monoid[Pair[A, B]]:
  def empty: Pair[A, B] = ???
  def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] = ???
```

Not only is the type signature of `Pair` now messy but it also forces all instances of `Pair` to have a `Monoid`
instance, whereas `Pair` should be able to carry any types it wants and if the types happens to have a `Monoid`
instance then so would it. 

We could try bubbling down the constraint into the methods themselves. Before we get to that:

### What is `scala.<:<`?

- `A <:< B` is both a sentence and a type name:
  - Read it as: “`A` is a subtype of `B`” (same idea as `A <: B` in bounds).
  - As a type, `A <:< B` is the type of evidence that this subtyping is true.
- `scala.<:<` is the class in the standard library that implements that evidence. The name looks odd because `<:` 
is already used for bounds, so they used `<:<` for this type.
- At compile time, if the compiler knows `A <: B`, it can provide an implicit value of type `A <:< B`. Then your 
code can treat an `A` as a `B` where that proof is in scope (your `eva`, `evb` parameters are asking for that proof at the call site).
- **Why `eva(first)` looks like a “call”:** `eva` is a normal **value** (passed implicitly or explicitly) whose type
is `A <:< Monoid[A]`. In the standard library, `scala.<:<` **extends** `A => B`, so that value is also a
**function** from `A` to `B`. Writing `eva(first)` applies that function: the compiler treats the result as
a `Monoid[A]`, so you can write `eva(first).empty` even though `first` was only typed as `A` before.

So: `scala.<:<` is not a new operator — it is the library type that represents “proof that `A` is a subtype of `B`,” and `A <:< B` in your method means “this method only typechecks if the compiler can supply that proof.”

```scala
// Require implicit proof that `A <: Monoid[A]` (and same for `B`) at call sites that need it hence
// the implicit parameters.
final case class Pair[A, B] (first: A, second: B) extends Monoid[Pair[A, B]] {
  // Pointwise monoid: use `first` / `second` only as *witnesses* so `eva` / `evb` can treat `A` / `B`
  // as `Monoid[...]` and call `.empty` / `.combine` (identity should not *semantically* depend on them).
  def empty(implicit eva: A <:< Monoid[A], evb: B <:< Monoid[B]): Pair[A, B] =
    Pair(eva(first).empty, evb(second).empty)

  def combine(x: Pair[A, B], y: Pair[A, B])(implicit eva: A <:< Monoid[A], evb: B <:< Monoid[B]): Pair[A, B] =
    Pair(
      eva(x.first).combine(x.first, y.first),
      evb(x.second).combine(x.second, y.second)
    )
}
// error: class Pair needs to be abstract.
// Missing implementations for 2 members of trait Monoid.
//   def combine(x: Pair[A,B], y: Pair[A,B]): Pair[A,B]   // required by trait
//   def empty: Pair[A,B]                                  // required by trait
//
// Bodies above are the natural pointwise definitions, but these methods are *overloads*: they add
// implicit parameters, so they do not implement the trait’s parameter-free `empty` / `combine`.
```

But now these still do not conform to the interface of `Monoid`: the implicit parameters change the method
signatures, so the trait’s members remain unimplemented.

## Implicit derivation

Note that a `Monoid[Pair[A, B]]` is derivable given `Monoid[A]` and `Monoid[B]`:

```scala

final case class Pair[A, B] (first: A, second: B)

def deriveMonoidPair[A, B] (A: Monoid[A], B: Monoid[B]): Monoid[Pair[A, B]] = 
  new Monoid[Pair[A, B]]:
    def empty: Pair[A, B] = 
      Pair(A.empty, B.empty)
    def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] = 
      Pair(A.combine(x.first, y.first), B.combine(x.second, y.second))
```

One of the most powerful features of type classes is the ability to do this kind of derivation automatically.
We can do this through Scala 3's `given` syntax or `implicit` syntax in Scala 2.

```scala

import cats.Monoid

final case class Pair[A, B] (first: A, second: B)


object Pair:
  // scala3 given
  given monoidPair[A: Monoid, B: Monoid]: Monoid[Pair[A, B]] =
    new Monoid[Pair[A, B]]:
      def empty: Pair[A, B] = Pair(Monoid[A].empty, Monoid[B].empty)
      def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] =
        Pair(Monoid[A].combine(x.first, y.first), Monoid[B].combine(x.second, y.second))

  // scala2 implicit
  implicit def monoidPair[A, B] (implicit A: Monoid[A], B: Monoid[B]): Monoid[Pair[A, B]] =
    new Monoid[Pair[A, B]]:
      def empty: Pair[A, B] = Pair(A.empty, B.empty)
      def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] =
        Pair(A.combine(x.first, y.first), B.combine(x.second, y.second))
```

We can also change any functions that have a `Monoid` constraint on the type parameter to take the
argument implicitly, and any instances of the type class to be implicit. 

```scala
// scala3 given
def combineAll[A] (list: List[A])(using A: Monoid[A]): A =
  list.foldRight(A.empty)(A.combine)

// scala3 given syntactic sugar
def combineAll[A : Monoid] (list: List[A]): A =
  list.foldRight(Monoid[A].empty)(Monoid[A].combine)

// scala2 implicit
def combineAll2[A] (list: List[A])(implicit A: Monoid[A]): A =
  list.foldRight(A.empty)(A.combine)

// Now we can also combineAll a list of `Pairs` as long as Pair's type parameters themselves 
// have `Monoid` instances.
val stringMonoid: Monoid[String] = new Monoid[String]:
  def empty: String = ""
  def combine(x: String, y: String): String = x ++ y

combineAll(List(Pair(1, "hello"), Pair(2, " "), Pair(3, "world")))
// res2: Demo.Pair[Int, String] = Pair(first = 6, second = "hello world")
```

### A note on syntax

In many cases, including the combineAll function above, the implicit arguments can be written with syntactic sugar.

```scala
def combineAll[A : Monoid] (list: List[A]): A = ???
```

This is syntactic sugar for:

```scala
def combineAll[A] (list: List[A])(using A: Monoid[A]): A =
  list.foldRight(A.empty)(A.combine)
```

While it is convenient, it does come with a cost for the implementer:

```scala

import cats.Monoid

// Defined in the standard library, shown for illustration purposes
// Implicitly looks in implicit scope for a value of type `A` and just hands it back
def implicitly[A] (implicit ev: A): A = ev

def combineAll[A : Monoid] (list: List[A]): A =
  list.foldRight(implicitly[Monoid[A]].empty)(implicitly[Monoid[A]].combine)
```

For this reason, many libraries that provide type classes provide a utility method on the companion object of 
the type class, usually under the name apply, that skirts the need to call implicitly everywhere.

```scala
object Monoid {
  def apply[A : Monoid]: Monoid[A] = implicitly[Monoid[A]]
}

def combineAll[A : Monoid] (list: List[A]): A =
  list.foldRight(Monoid[A].empty)(Monoid[A].combine)

```

## Laws

Conceptually, all type classes come with laws. These laws constrain implementations for a given type and can be 
used to reason about generic code. 

For instance, the Monoid type class requires that `combine` be associative and `empty` be an identity element 
for combine. That means the following equalities should hold for any choice of x, y, and z.

```scala
combine(x, combine(y, z)) = combine(combine(x, y), z)
combine(x, empty) = combine(empty, x) = x
```

With these laws in place, functions parametrized over a `Monoid` can leverage them for say, performance reasons.
A function that collapses a `List[A]` into a single `A` can do so with `foldLeft` or `foldRight` since `combine` 
is assumed to be associative, or it can break apart the list into smaller lists and collapse in parallel, such as

```scala

val list = List(1, 2, 3, 4, 5)
val (left, right) = list.splitAt(2)

// Imagine the following operations run in parallel
val sumLeft = combineAll(left) // 3
val sumRight = combineAll(right) // 12

// Now gather the results
val result = Monoid[Int].combine(sumLeft, sumRight) // 15
```

This is a simple example of how laws can be used to reason about generic code. In practice, the laws are often 
more complex and require a deeper understanding of the type class in question. `Cats` provides laws for type 
classes via the `kernel-laws` and `laws` modules which makes law checking type class instances easy.
