package learning.typeclasses


/**
  * If a type A can form a Semigroup it has an associative binary operation.
  * Associativity means the following equality must hold for any choice of x, y, and z.
  * combine(x, combine(y, z)) = combine(combine(x, y), z).
  * `trait Semigroup[A] { def combine(x: A, y: A): A}`.
  * Cats provides many Semigroup instances out of the box such as 
  * `Int (+)` and `String (++)`

*/
@main def semiGroupExample() =
    import cats.Semigroup
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
    And instances for type constructors that depend on (one of) their type parameters having 
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
