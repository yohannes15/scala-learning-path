package learning.datatypes

/**
  * The identity monad can be seen as the monad that encodes the 
  * effect of having no effect. plain pure values are values of Id.
  * It is encoded as:

        `type Id[A] = A`

  * That is to say that the type `Id[A]` is just a synonym for `A`.
  * We can freely treat values of type `A` as values of type `Id[A]`
  * and vice versa.
  * 
  * Using this type declaration, we can treat our `Id` type constructor
  * as a Monad and as a Comonad. The `pure` method, which has type
  * `A => Id[A]` just becomes the identity function. The map method from
  * Functor just becomes function application:
  *
  * For Monad, `flatMap` on `Id` is also just ordinary function application:
  * there is no nested structure to flatten. That is why `Id` is a good
  * mental model: the monad laws hold, but every step is "do nothing extra".
  *
*/
object LearningId:
    import cats.{Id, Functor}
    def equalityOfIdAndPureValues() =
        val x: Id[Int] = 1 // x: Id[Int] = 1
        val y = 1 // y: Int = 1
        println(s"id and pure values are equal: ${x == y}")

    def idIsFunctor() = 
        val x: Id[Int] = 10
        println(Functor[Id].map(x)(_ + 1)) // Id[Int] = 11

    /* 
    Compare the signatures of `map` and `flatMap` and `coflatMap`.
    You'll notice that in the `flatMap` signature, since `Id[B]` is the same as
    `B` for all `B`, we can rewrite the type of the f parameter to be `A => B` 
    instead of `A => Id[B]`, and this makes the signatures of the two functions
    the same, and, in fact, they can have the same implementation, meaning that 
    for Id, flatMap and coflatMap are also just function application like map for
    Id!

    In short:
        Implementations (all are “apply f to the bare value” because Id[A] is just A):
    */
    def map[A, B](fa: Id[A])(f: A => B): Id[B] = f(fa)
    def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)
    def coflatMap[A, B](a: Id[A])(f: Id[A] => B): Id[B] = f(a)

    def mapAndFlatMapOnId() =
        import cats.{Comonad, Monad}

        val one: Int = 1
        // they are identical for Id
        println(Monad[Id].map(one)(_ * 100)) // 100
        println(Monad[Id].flatMap(one)(_ * 100)) // 100; same as map since `Id[B]` is just `B`
        // coflatMap takes a function on the whole `Id[A]` (here, the same as `A => B`)
        println(Comonad[Id].coflatMap(one)(_.toString.length)) // 1, length of "1"


@main def IdExamples(): Unit =
    import LearningId.*
    equalityOfIdAndPureValues()
    idIsFunctor()
    mapAndFlatMapOnId()
