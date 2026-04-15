package learning.typeclasses
import cats.Semigroup
import cats.syntax.all._

/**
  * If a type A can form a Semigroup it has an associative binary operation.
  * Associativity means the following equality must hold for any choice of x, y, and z.
  * combine(x, combine(y, z)) = combine(combine(x, y), z).
  * Cats provides many Semigroup instances out of the box such as 
  * `Int (+)` and `String (++)`

    trait Semigroup[A] { 
        def combine(x: A, y: A): A
    }

Cats defines the `Semigroup` type class in cats-kernel. The cats package object defines
type aliases to the Semigroup from cats-kernel, so that you can simply import cats.Semigroup.
*/
@main def semiGroupExample() =
    // given intAdditionSemiGroup: Semigroup[Int] = _ + _ 
    val x = 1
    val y = 2
    val z = 3

    println(Semigroup[Int].combine(x, y)) // 3
    println(Semigroup[Int].combine(x, Semigroup[Int].combine(y, z))) // 6
    println(Semigroup[Int].combine(Semigroup[Int].combine(x, y), z)) // 6

    // Infix syntax is also available for types that have a Semigroup instance.
    import cats.syntax.all._
    println(1 |+| 2) // Int = 3

    // Semigroup for Maps is an interesting example
    val map1 = Map("hello" -> 1, "world" -> 1)
    val map2 = Map("hello" -> 2, "cats" -> 3)

    println(Semigroup[Map[String, Int]].combine(map1, map2)) // Map("hello" -> 3, "cats" -> 3, "world" -> 1)
    println(map1 |+| map2)

    println(s"The type for Semigroup[Int] is ${Semigroup[Int]}")
    println(s"The type for Semigroup[String] is ${Semigroup[String]}")

    // Instances for type constructors regardless of their type parameter such as 
    // List (++) and Set (union)...
    println(s"The type for Semigroup[List[Byte]] is ${Semigroup[List[Byte]]}")
    println(s"The type for Semigroup[Set[Int]] is ${Semigroup[Set[Int]]}")

    trait Foo
    println(s"The type for Semigroup[List[Foo]] is ${Semigroup[List[Foo]]}")

    /* 
    And instances for type constructors that depend on their type parameters having 
    instances such as tuples (pointwise combine).
    
    Tuple / product types: `(A, B)` combines *pointwise* — first with first, second with second —
    but only if Cats can find a `Semigroup` for *each* part. Here `List[Foo]` uses list append
    (`++`), and `Int` uses the default additive semigroup (`+`). Cats stitches those into one
    `Semigroup` for the whole pair. The `println` shows that such an instance was found
    
    If you see `Monoid$$anon$...` in that string: in Cats, `Monoid` extends `Semigroup`, and tuple
    instances are often implemented as a full `Monoid` (pointwise `empty` + `combine`) when each
    side has a `Monoid`. Summoning `Semigroup[(A,B)]` still picks that object — it *is* a
    semigroup; the runtime class name just reflects the concrete implementation.*/
    println(s"The type for Semigroup[(List[Foo], Int)] is ${Semigroup[(List[Foo], Int)]}")

    // -------- Merging Maps Example Runs --------
    mergeMapExamples()
    // -------- Associativity example --------
    associativityExample()


/* 
Example usage: Merging maps
----------------------------------
Consider a function that mergs two `Map`s that combines values if they share the same key. It is
straightforward to write these for `Map`s with values of type say Int or List[String], but we 
can write it once and for all for any type with a `Semigroup` instance.

It is interesting to note that the type of `mergeMap` satisfies the type of `Semigroup` specialized
to `Map[K, *]` and is associative - indeed the Semigroup instance for Map uses the same function for
its `combine`.
*/

def optionCombine[A: Semigroup](a: A, opt: Option[A]): A =
    opt.map(_ |+| a).getOrElse(a)

def mergeMap[K, V: Semigroup](lhs: Map[K, V], rhs: Map[K, V]): Map[K, V] = 
    lhs.foldLeft(rhs){
        case (acc, (k, v)) => acc.updated(k, optionCombine(v, acc.get(k)))
    }

def mergeMapExamples() = 
    val m1: Map[Char, Int] = Map('a' -> 1, 'b' -> 2)
    val m2: Map[Char, Int] = Map('b' -> 3, 'c' -> 4)
    val x: Map[Char, Int] = mergeMap(m1, m2) //  Map('b' -> 5, 'c' -> 4, 'a' -> 1)
    println(x)
    val mm1: Map[Int, List[String]] = Map(1 -> List("hello"))
    val mm2: Map[Int, List[String]] = Map(2 -> List("cats"), 1 -> List("world"))
    val y: Map[Int, List[String]] = mergeMap(mm1, mm2) // Map(2 -> List("cats"), 1 -> List("hello", "world"))
    println(y)
    println(Semigroup[Map[Char, Int]].combine(m1, m2) == x) // true
    println(Semigroup[Map[Int, List[String]]].combine(mm1, mm2) == y) // true

/* 
Exploiting laws: associativity
---------------------------------
Since we know `Semigroup#combine `must be associative, we can exploit this when writing code against `Semigroup`. 
For instance, to sum a `List[Int]` we can choose to either `foldLeft` or `foldRight`.
*/

def associativityExample() = 
    val leftwards = List(1, 2, 3).foldLeft(0)(_ |+| _) // 6
    val rightwards = List(1, 2, 3).foldRight(0)(_ |+| _) // 6

    // also allows us to split a list apart and sum the parts in parallel, gathering the results in the end.
    val list = List(1, 2, 3, 4, 5)
    val (left, right) = list.splitAt(2)
    val sumLeft = left.foldLeft(0)(_ |+| _) // 3
    val sumRight = right.foldLeft(0)(_ |+| _) // 12
    val result = sumLeft |+| sumRight // 15
    println(result)

/*
However, given just `Semigroup` we cannot write the above expressions generically. For instance, we quickly run
into issues if we try to write a generic `CombineAll` function

    def combineAll[A: Semigroup](as: List[A]): A =
        as.foldLeft(/* ?? what goes here ?? */)(_ |+| _)

Semigroup isn't powerful enough for us to implement this function - namely, it doesn't give us an `identity` 
or `fallback` value if the list is empty. We need a more powerfully expressive abstraction, which we can find 
in the `Monoid` type class.
*/
