package learning.types

/************************************************
**************************************************
Intersection Types (Scala 3 only)
------------------------------------
Used on types, the `&` operator creates a so called intersection type.
The type `A & B` represents values that are both of the type `A` and of
the type `B` at the same time.

For instance, the following example uses the intersection type

    `Resettable & Growable[String]`

The members of an intersection type A & B are all the members of
A and all the members of B. Therefore, as shown, Resettable &
Growable[String] has member methods `reset` and `add`.

Intersection types can be useful to describe requirements structurally.
That is, in our example f, we directly express that we are happy with
any value for x as long as it's a subtype of both Resettable and Growable.
We did not have to create a nominal helper trait Both[A] like the following:
************************************************/

trait Resettable:
    def reset(): Unit

trait Growable[A]:
    def add(a: A): Unit

trait Both[A] extends Resettable, Growable[A]

// Structural: accepts anything that is both Resettable and Growable[String]
def goodf(x: Resettable & Growable[String]): Unit =
    x.reset()
    x.add("first")

// Nominal: requires the exact named type Both[String]
def badf(x: Both[String]): Unit =
    x.reset()
    x.add("first")

/*
DIFFERENCE BETWEEN THE TWO ALTERNATIVES
-----------------------------------------
There is an important difference between the two alternatives of defining f:
While both allow f to be called with instances of Both, only the former allows
passing instances that are subtypes of Resettable and Growable[String],
but not of Both[String].

Concrete illustration:
  - `NominalBoth` extends the named trait `Both[String]`, so it works with both `goodf` and `badf`.
  - `StructuralOnly` mixes `Resettable` and `Growable[String]` directly but does *not* extend
    `Both[String]`. It is still a `Resettable & Growable[String]`, so `goodf` accepts it;
    `badf` does not, because the parameter type requires the *nominal* type `Both[String]`.

Note that & is commutative: A & B is the same type as B & A.
 */

/** Extends the named `Both[String]` — accepted by both `goodf` and `badf`. */
final class NominalBoth extends Both[String]:
    private var items: List[String] = Nil
    def reset(): Unit = items = Nil
    def add(a: String): Unit = items = items :+ a
    override def toString: String = s"NominalBoth($items)"

/** Mixes the same two capabilities without extending `Both` — structural match, nominal mismatch. */
final class StructuralOnly extends Resettable, Growable[String]:
    private var items: List[String] = Nil
    def reset(): Unit = items = Nil
    def add(a: String): Unit = items = items :+ a
    override def toString: String = s"StructuralOnly($items)"

def intersectionTypesExample(): Unit =
    val fromNominal    = NominalBoth()
    val fromStructural = StructuralOnly()

    goodf(fromNominal)
    goodf(fromStructural)
    println(s"after goodf: $fromNominal, $fromStructural")

    badf(fromNominal)
    // badf(fromStructural)  // does not compile: StructuralOnly is not a subtype of Both[String]

    println("goodf accepts any Resettable & Growable[String]; badf only Both[String]")
