package learning.typeclasses

import cats.ApplicativeError
import cats.Monad
import cats.syntax.all.*

/** The Definition for MonadError extends Monad and provides error handling
  * methods like ensure, ensureOr, adaptError, rethrow. Refer to Monad.scala for
  * reference. Monad provdes flatMap for ex
  */

trait MonadError[F[_], E] extends ApplicativeError[F, E] with Monad[F]:
  def ensure[A](fa: F[A])(error: => E)(predicate: A => Boolean): F[A]
  def ensureOr[A](fa: F[A])(error: A => E)(predicate: A => Boolean): F[A]
  def adaptError[A](fa: F[A])(pf: PartialFunction[E, E]): F[A]
  def rethrow[A, EE <: E](fa: F[Either[EE, A]]): F[A]

/** Use Case Given a method that accepts a tuple of coordinates, it finds the
  * closest city. For this example we will hard-code "Minneapolis, MN," but you
  * can imagine for the sake of this example, you would either consult a
  * database or a web service.
  */
def getCityClosestToCoordinate[F[_]](x: (Int, Int))(using
    ae: ApplicativeError[F, String]
): F[String] =
  ae.pure("Minneapolis, MN")

/** Next, let's follow up with another method, getTemperatureByCity, that given
  * a city, possibly a city that was just discovered by its coordinates, we get
  * the temperature for that city. Here, for the sake of demonstration, we are
  * hardcoding a temperature of 78°F.
  */
def getTemperatureByCity[F[_]](city: String)(using
    ae: ApplicativeError[F, String]
): F[Int] =
  ae.pure(78)

/** With the methods that we will compose in place let's create a method that
  * will compose the above methods using a for comprehension which interprets to
  * a flatMap-map combination.
  *
  * getTemperatureByCoordinates's parameterized type [F[_]:MonadError[*[_],
  * String] injects F[_] into MonadError[*[_], String]; thus if the "error type"
  * you wish to use is Either[String, *], the Either would be placed in the hole
  * of MonadError, in this case, MonadError[Either[String, *], String]
  *
  * getTemperatureByCoordinates accepts a Tuple2 of Int and Int and returns F,
  * which represents our MonadError, which can be a type like Either or
  * Validated. In the method, since getCityClosestToCoordinate and
  * getTemperatureByCity both return potential error types and they are monadic,
  * we can compose them with a for comprehension.
  *
  * {{{
  *    def getTemperatureByCoordinates[F[_]: cats.MonadError[*[_], String]](x: (
  *      Int,
  *      Int
  *    )): F[Int] =
  *      for
  *        c <- getCityClosestToCoordinate[F](x)
  *        t <- getTemperatureByCity[F](c)
  *      yield t
  * }}}
  *
  * With TypeLevel Cats, how you structure your methods is up to you: if you
  * wanted to create getTemperatureByCoordinates without a Scala context bound
  * for MonadError, but create an implicit/using parameter for your MonadError
  * you can have access to some additional methods.
  *
  * In the following example, we create an implicit MonadError parameter and
  * call it me. Using the me reference, we can call any one of its specialized
  * methods, like raiseError, to raise an error representation when things go
  * wrong.
  */
def getTemperatureByCoordinates[F[_]](x: (Int, Int))(using
    me: cats.MonadError[F, String]
): F[Int] =
  if (x._1 < 0 || x._2 < 0) me.raiseError("Invalid Coordinates")
  else
    for
      c <- getCityClosestToCoordinate[F](x)
      t <- getTemperatureByCity[F](c)
    yield t

/** We can call getTemperatureByCoordinates with the following sample, which
  * will return 78.
  */
@main def monadErrorExample() =
  type MyEither[A] = Either[String, A]
  val res = getTemperatureByCoordinates[MyEither]((44, 93))
  val res2 = getTemperatureByCoordinates((-1, 12))
  println(s"getTemperatureByCoordinates result: $res")
  println(s"getTemperatureByCoordinates error result: $res2")
