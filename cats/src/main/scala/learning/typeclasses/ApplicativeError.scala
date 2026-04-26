package learning.typeclasses

import cats._
import cats.syntax.all._
import cats.Applicative
import cats.data.Validated

/** Applicative Error: Extends `Applicative` to provide handling for types that*
  * represent the quality of an exception or an error, for example,
  * `Either[E, A]`.
  */
object LearningApplicativeError:
  // ApplicativeError Trait
  trait ApplicativeError[F[_], E] extends Applicative[F]:
    def raiseError[A](e: E): F[A]
    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
    def handleError[A](fa: F[A])(f: E => A): F[A]
    def attempt[A](fa: F[A]): F[Either[E, A]]
    // more functions left out

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
  def attemptDivide(x: Int, y: Int): Either[String, Int] =
    if (y == 0) Left("divisor is zero") else Right(x / y)

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
    */
  def attemptDivideApplicativeError[F[_]](x: Int, y: Int)(using
      ae: ApplicativeError[F, String]
  ): F[Int] =
    if (y == 0) ae.raiseError("divisor is zero error") else ae.pure(x / y)

  /** All methods of Applicative are available to use (ap, mapN ...). In below
    * example we use ap and map2.
    */
  def attemptDivideApplicativeErrorWithMap2[F[_]](x: Int, y: Int)(using
      ae: ApplicativeError[F, String]
  ): F[Int] =
    if (y == 0) ae.raiseError("divisor is error")
    else {
      val fa = ae.pure(x)
      val fb = ae.pure(y)
      ae.map2(fa, fb)(_ / _)
    }

  /** Following creates an error representation if the divisior is 0 or 1 with
    * the msg "Bad Math" or "Waste of Time". We then feed the result to handler
    * method, where the result will be pattern matched on the message and
    * provide an alternative outcome. This is the use of handle Error method.
    */
  def attemptDivideApplicativeError2[F[_]](x: Int, y: Int)(using
      ae: ApplicativeError[F, String]
  ): F[Int] =
    if (y == 0) ae.raiseError("Bad Math")
    else if (y == 1) ae.raiseError("Waste of time")
    else ae.pure(x / y)

  def handler[F[_]](f: F[Int])(using ae: ApplicativeError[F, String]): F[Int] =
    ae.handleError(f)({
      case "Bad Math"      => -1
      case "Waste of Time" => -2
      case _               => -3
    })

  /** handleErrorWith is nearly same as handleError but instead of returning a
    * value `A`, we will return F[_]. This provides us with opportunity to make
    * it abstract and reutrn a value from Monoid.empty.
    *
    * @param fa
    * @param F
    * @param M
    * @return
    */
  def handleErrorWith[F[_], M[_], A](fa: F[A])(using
      F: ApplicativeError[F, String],
      M: Monoid[A]
  ): F[A] =
    F.handleErrorWith(fa)(_ => F.pure(M.empty))

  /** There will come a time when your nice code will have to interact with
    * exception throwing code. Handling such situations is easy enough
    */
  def parseInt[F[_]](input: String)(using
      // using cats here so we don't have to implement Either[Throwable, A]
      F: cats.ApplicativeError[F, Throwable]
  ): F[Int] =
    try
      F.pure(input.toInt)
    catch
      case nfe: NumberFormatException => F.raiseError(nfe)

  /** However the above can get tedious pretty quickly, ApplicativeError has a
    * catchOnly method that allows you to pass it a function, along with the
    * type of exception you want to catch and does the above for you
    */
  def parseIntImproved[F[_]](input: String)(using
      F: cats.ApplicativeError[F, Throwable]
  ): F[Int] =
    F.catchOnly[NumberFormatException](input.toInt)
    // F.catchNonFatal(input.toInt)
    // Allows us to catch all (non-fatal) throwables, you can use `catchNonFatal`

@main def applicativeErrorExamples() =
  import LearningApplicativeError.*
  val g: ErrorOr[Int] = attemptDivideApplicativeError(30, 10)
  val g2: ErrorOr[Int] = attemptDivideApplicativeErrorWithMap2(300, 10)
  val g3: ErrorOr[Int] = handler(attemptDivideApplicativeError2(3, 0))
  val g4: ErrorOr[Int] = handleErrorWith(attemptDivideApplicativeError2(3, 0))
  println(s"ApplicativeError ErrorOr Result: $g")
  println(s"ApplicativeError ErrorOr Result: $g2")
  println(s"ApplicativeError into handleError: $g3")
  println(s"ApplicativeError into handleErrorWith: $g4")
  val parseRes1 = parseInt[Either[Throwable, *]]("123")
  val parseRes2 = parseInt[Either[Throwable, *]]("abc")
  println(s"parseInt success result: $parseRes1")
  println(s"parseInt error result: $parseRes2")
  val parseResImproved1 = parseIntImproved[Either[Throwable, *]]("abc")
  println(s"parseIntImproved error result with catchOnly: $parseResImproved1")
