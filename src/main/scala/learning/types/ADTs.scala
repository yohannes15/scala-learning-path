package learning.types

/**********************************************************************
***********************************************************************
Algebraic Data Types (ADTs)
------------------------------------
The enum concept is general enough to also support ADTs and their
generalized versions (GADTs). Below is an example that shows how an
`Option` type can be represented as an ADT.

Example creates:
- an Option enum with a covariant type parameter `T` consisting of
  two cases, `Some` and `None`
- `Some` is parameterized with a value parameter x: T. This is
  shorthand for writing a `case` class that extends Option
- `None` is not parameterized, treated as a normal `enum` value

**********************************************************************
**********************************************************************/

// +T = covariant: Option[String] is a subtype of Option[Any]
enum Option[+T]:
    // Shorthand for: case class Some(x: T) extends Option[T]
    case Some(x: T)
    // Singleton with no fields. Inferred as: case object None extends Option[Nothing]
    // Option[Nothing] is a subtype of Option[T] for any T, due to covariance
    case None

    // Pattern match on `this` is exhaustive — the compiler knows all cases
    def isDefined: Boolean = this match
        case None    => false
        case Some(_) => true

object Option:
    // Smart constructor: wraps non-null values in Some, maps null to None
    // T >: Null constrains T to be a nullable (reference) type
    def apply[T >: Null](x: T): Option[T] =
        if (x == null) None else Some(x)

/* long hand version — extends is optional
enum Option[+T]:
    case Some(x: T) extends Option[T]
    case None extends Option[Nothing]
*/

def optionAsEnumExample() =
    val res1: Option[String]  = Option.Some("Hello")
    val res2: Option[Nothing] = Option.None
    println(res1) // Some(Hello)
    println(res2) // None

/*
Enumerations and ADTs share the same syntactic construct, so they can be
seen simply as two ends of a spectrum, and it's perfectly possible to
construct hybrids. For instance, the code below gives an implementation
of Color, either with three enum values or with a parameterized case
that takes an RGB value:
 */

// The enum constructor declares a shared `rgb` field every case must supply.
// This is a hybrid: three fixed singleton cases + one dynamic ADT case.
enum ADTColor(val rgb: Int):
    // Singleton cases: no runtime fields, each hardcodes its rgb value
    case Red   extends ADTColor(0xFF0000)
    case Green extends ADTColor(0x00FF00)
    case Blue  extends ADTColor(0x0000FF)
    // ADT case: accepts a runtime argument and forwards it as the rgb value
    case Mix(mix: Int) extends ADTColor(mix)

/*******************************************************************
Recursive Enumerations
-------------------------------------------------------------------
Enumerations can be recursive using the same two-part pattern:
  1. Base case  — no self-reference, terminates recursion
  2. Recursive case — holds data AND a reference back to the same type
*******************************************************************/

// Peano encoding of natural numbers: integers built purely from structure
// Zero is the base case; Succ wraps another Nat, adding 1 each time
enum Nat:
    case Zero          // represents 0
    case Succ(n: Nat)  // represents n + 1; e.g. Succ(Succ(Zero)) == 2

// The same recursive pattern applied to a generic collection
// Nil terminates the list; Cons prepends one element to an existing List[A]
enum MyList[+A]:
    case Nil                              // empty list — base case
    case Cons(head: A, tail: MyList[A])  // e.g. Cons(1, Cons(2, Cons(3, Nil))) == [1,2,3]

def adtExample() =
    println(ADTColor.Red.rgb)           // 16711680 — fixed at compile time
    println(ADTColor.Mix(0xFF0000).rgb) // 16711680 — supplied at runtime

    import Nat.*
    val zero  = Zero                    // 0
    val one   = Succ(Zero)              // 1
    val two   = Succ(Succ(Zero))        // 2
    val three = Succ(Succ(Succ(Zero)))  // 3
    println(zero)   // Zero
    println(one)    // Succ(Zero)
    println(two)    // Succ(Succ(Zero))
    println(three)  // Succ(Succ(Succ(Zero)))

    import MyList.*
    val empty = Nil
    val nums  = Cons(1, Cons(2, Cons(3, Nil)))
    val strs  = Cons("a", Cons("b", Nil))
    println(empty) // Nil
    println(nums)  // Cons(1,Cons(2,Cons(3,Nil)))
    println(strs)  // Cons(a,Cons(b,Nil))

    // pattern matching is exhaustive across all recursive cases
    def natToInt(n: Nat): Int = n match
        case Zero    => 0
        case Succ(n) => 1 + natToInt(n)

    def listHead[A](l: MyList[A]): Option[A] = l match
        case Nil        => Option.None
        case Cons(h, _) => Option.Some(h)

    println(natToInt(three))   // 3
    println(listHead(nums))    // Some(1)
    println(listHead(empty))   // None
