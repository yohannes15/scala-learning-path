import cats._
import cats.syntax.all._
import cats.Applicative
import cats.data.Validated

/** Applicative Error: Extends `Applicative` to provide handling for types that*
  * represent the quality of an exception or an error, for example,
  * `Either[E, A]`.
  */
object LearningApplicativeError {
  // ApplicativeError Trait
  trait ApplicativeError[F[_], E] extends Applicative[F] {
    def raiseError[A](e: E): F[A]
    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
    def handleError[A](fa: F[A])(f: E => A): F[A]
    def attempt[A](fa: F[A]): F[Either[E, A]]
    // more functions left out
  }

  // Sample implementation of ApplicativeError for Either[String, A] type
  type ErrorOr[A] = Either[String, A]

  given applicativeErrorEither: ApplicativeError[ErrorOr, String] with

    def pure[A](a: A): ErrorOr[A] = Right(a)

    def ap[A, B](ff: ErrorOr[A => B])(fa: ErrorOr[A]): ErrorOr[B] =
      (ff, fa) match
        case (Right(f), Right(a)) => Right(f(a))
        case (Left(e), _)         => Left(e)
        case (_, Left(e))         => Left(e)

    def raiseError[A](e: String): ErrorOr[A] = Left(e)

    def handleErrorWith[A](fa: ErrorOr[A])(
        f: String => ErrorOr[A]
    ): ErrorOr[A] =
      fa match
        case Left(e)  => f(e)
        case Right(a) => Right(a)

    def handleError[A](fa: ErrorOr[A])(f: String => A): ErrorOr[A] =
      fa match
        case Left(e)  => Right(f(e))
        case Right(a) => Right(a)

    def attempt[A](fa: ErrorOr[A]): ErrorOr[Either[String, A]] =
      Right(fa)

  /*
  
   **Use Case:
   We can start with a less abstract way of performing a function. Here we will
   divide one number by another. While this approach is fine, we can abstract away
   the `Either` to support any other kind of "error" type without having to create
   multiple functions with different "container" types.
   */
  def attemptDivide(x: Int, y: Int): Either[String, Int] = {
    if (y == 0) Left("divisor is zero") else Right(x / y)
  }

  /** This method summons `ApplicativeError` to provide behaviour representing
    * an error where the end-user based on type, will get their appropriate
    * response. AE is an Applicative, which means all Applicative functions are
    * available for use. One such method is pure, which will return the F[]
    * representation, where F could respresent `Either`.
    *
    * Another method that you will see is raiseError, which will generate the
    * specific error type depending on what `F[]` represents. If F[] is an
    * Either, then `ae.raiseError` will return `Left`. If F[] represents a
    * Validation, then it will return `Invalid`
    * @param x
    * @param y
    * @param ae
    * @return
    */
  def attemptDivideApplicativeError[F[_]](x: Int, y: Int)(using
      ae: ApplicativeError[F, String]
  ): F[Int] = {
    if (y == 0) ae.raiseError("divisor is zero error") else ae.pure(x / y)
  }

  /** All methods of Applicative are available to use (ap, mapN ...). In below
    * example we use ap and map2.
    */
  def attemptDivideApplicativeErrorWithMap2[F[_]](x: Int, y: Int)(using
      ae: ApplicativeError[F, String]
  ): F[Int] = {
    if (y == 0) ae.raiseError("divisor is error")
    else {
      val fa = ae.pure(x)
      val fb = ae.pure(y)
      ae.map2(fa, fb)(_ / _)
    }
  }
}

@main def applicativeErrorExamples() =
  import LearningApplicativeError.*
  val g: ErrorOr[Int] = attemptDivideApplicativeError[ErrorOr](30, 10)
  val g2: ErrorOr[Int] = attemptDivideApplicativeErrorWithMap2[ErrorOr](300, 10)
  println(s"ApplicativeError ErrorOr Result: $g")
  println(s"ApplicativeError ErrorOr Result: $g2")
