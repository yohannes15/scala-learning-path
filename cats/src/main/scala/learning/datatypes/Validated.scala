package learning.datatypes

import cats.Apply
import cats.kernel.Semigroup


/**
  * Validated
  * ---------------
  * Imagine you are filling out a web form to signup for an account. You input your 
  * username and password and submit. Response comes back saying your username can't
  * have dashes in it, so you make some changes and resubmit. Can't have special 
  * characters either. Change, resubmit. Passwords need to have at least one capital
  * letter. Change, resubmit. Password needs to have at least one number.
  * 
  * It would be nice to have all of these errors be reported simultaneously. That 
  * the username can't have dashes can be validated separately from it not having 
  * special characters, as well as from the password needing to have certain 
  * requirements. A misspelled (or missing) field in a config can be validated 
  * separately from another field not being well-formed.
  * 
  * Enter Validated!!
  * --------------------
  * Note firsthand that `Validated` is very similar to `Either` because it also has 
  * two possible values: errors on the left side or successful computations on the 
  * right side.
  * 
  * Validated vs Either
  * --------------------
  * We've established that an error-accumulating data type such as Validated can't
  * have a valid Monad instance. Sometimes the task at hand requires error-accumulation.
  * However, sometimes we want a monadic structure that we can use for sequential 
  * validation (such as in a for-comprehension). This leaves us in a bit of a conundrum.
  * 
  * Cats has decided to solve this problem by using separate data structures for 
  * error-accumulation (Validated) and short-circuiting monadic behavior (Either).
  * 
  * If you are trying to decide whether you want to use Validated or Either, a 
  * simple heuristic is to use Validated if you want error-accumulation and to 
  * otherwise use Either.
  * 
  * Validated has helper sequential functions `andThen` and `withEither` discussed
  * below if needed.
  */
object LearningValidated:
  /* 
  In scala 2 use:
  `sealed abstract class Validated[+E, +A] extends Product with Serializable`
  */
  sealed trait Validated[+E, +A]
  /* its projections */
  final case class Valid[+A](a: A) extends Validated[Nothing, A]
  final case class Invalid[+E](e: E) extends Validated[E, Nothing]

  /* following data is for examples demonstrating need for Validated over Either 
  We have our RegistrationData case class that will hold the information the user
  has submitted, alongside the definition of the error model that we'll be using 
  for displaying the possible errors of every field
  */
  final case class RegistrationData(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    age: Int
  )
  /* our error models */
  sealed trait DomainValidation:
    def errorMessage: String

  case object AgeIsInvalid extends DomainValidation:
    def errorMessage: String = "You must be aged 18 and not older than 75 to use our services."

  case object UsernameHasSpecialCharacters extends DomainValidation:
    def errorMessage: String = "Username cannot contain special characters"

  case object PasswordDoesNotMeetCriteria extends DomainValidation:
    def errorMessage: String = "Password must be at least 10 characters long, including an uppercase and a lowercase letter, one number and one special character."

  case object FirstNameHasSpecialCharacters extends DomainValidation:
    def errorMessage: String = "First name cannot contain spaces, numbers or special characters."

  case object LastNameHasSpecialCharacters extends DomainValidation:
    def errorMessage: String = "Last name cannot contain spaces, numbers or special characters."

  /* 
  Either based implementation
  -----------------------------
  The logic of the validation process is as follows: check every individual field based
  on the established rules for each one of them. If the validation is successful, then
  return the field wrapped in a Right instance; If not, then return a `DomainValidation`
  with the respective message, wrapped in a Left instance. Note that we took advantage 
  of the .cond method of Either, that is equivalent to if (cond) Right(v) else Left(e).

  Our service has the `validateForm` method for checking all the fields and, if the 
  process succeeds it will create an instance of RegistrationData, right?

  Well, yes, but the error reporting part will have the downside of showing only the 
  first error. A for-comprehension is fail-fast. 
  */
  sealed trait FormValidator:
    def validateUserName(username: String): Either[DomainValidation, String] = 
      Either.cond(
        username.matches("^[a-zA-Z0-9]+$"),
        username,
        UsernameHasSpecialCharacters
      )
      
    def validatePassword(password: String): Either[DomainValidation, String] = 
      Either.cond(
        password.matches("(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"),
        password,
        PasswordDoesNotMeetCriteria
      )

    def validateFirstName(firstName: String): Either[DomainValidation, String] =
      Either.cond(
        firstName.matches("^[a-zA-Z]+$"),
        firstName,
        FirstNameHasSpecialCharacters
      )

    def validateLastName(lastName: String): Either[DomainValidation, String] =
      Either.cond(
        lastName.matches("^[a-zA-Z]+$"),
        lastName,
        LastNameHasSpecialCharacters
      )

    def validateAge(age: Int): Either[DomainValidation, Int] =
      Either.cond(
        age >= 18 && age <= 75,
        age,
        AgeIsInvalid
      )

    def validateForm(username: String, password: String, firstName: String, lastName: String, age: Int): Either[DomainValidation, RegistrationData] =
      for // A for-comprehension is fail-fast. 
        validatedUserName <- validateUserName(username)
        validatedPassword <- validatePassword(password)
        validatedFirstName <- validateFirstName(firstName)
        validatedLastName <- validateLastName(lastName)
        validatedAge <- validateAge(age)
      yield RegistrationData(validatedUserName, validatedPassword, validatedFirstName, validatedLastName, validatedAge)
  // concerent implementation
  object FormValidator extends FormValidator

  def forComprehensionisFailFast() = 
    val res = FormValidator.validateForm(
      username = "fakeUs3rname",
      password = "password",
      firstName = "John",
      lastName = "Doe",
      age = 15
    )
    println(s"result of fail fast for comprehension: $res")
    println(s"We should have gotten another DomainValidation object denoting the invalid age.\n")
    // Either[DomainValidation, RegistrationData] = Left(value = PasswordDoesNotMeetCriteria)


/* 
An iteration with `Validated`
-------------------------------
Lets try a `Validated` approach. 
*/
object ValidatedApproach:
  import LearningValidated.{DomainValidation, FormValidator, RegistrationData}
  import cats.syntax.all.*
  import cats.data.Validated
  
  def validateUserName(userName: String): Validated[DomainValidation, String] =
    FormValidator.validateUserName(userName).toValidated

  def validatePassword(password: String): Validated[DomainValidation, String] =
    FormValidator.validatePassword(password).toValidated

  def validateFirstName(firstName: String): Validated[DomainValidation, String] =
    FormValidator.validateFirstName(firstName).toValidated

  def validateLastName(lastName: String): Validated[DomainValidation, String] =
    FormValidator.validateLastName(lastName).toValidated

  def validateAge(age: Int): Validated[DomainValidation, Int] =
    FormValidator.validateAge(age).toValidated

  /* So, how do we do things here if Validated isn't a monad like described below? */
  def validateForm(
    username: String, password: String, firstName: String, lastName: String, age: Int
  ): Validated[DomainValidation, RegistrationData] =
    ???
    /* The following code won't compile because without diving into details about monads, 
    a for-comprehension uses the flatMap method for composition. Monads like Either can 
    be composed in that way, but the thing with Validated is that it isn't a monad, but 
    an `Applicative Functor`. That's why you see the message: 
      error: value flatMap is not a member of cats.data.Validated[DomainValidation,String].*/
    // for
    //   validatedUserName <- validateUserName(username)
    //   validatedPassword <- validatePassword(password)
    //   validatedFirstName <- validateFirstName(firstName)
    //   validatedLastName <- validateLastName(lastName)
    //   validatedAge <- validateAge(age)
    // yield RegistrationData(validatedUserName, validatedPassword, validatedFirstName, validatedLastName, validatedAge)


import cats.data.ValidatedNec
import cats.syntax.all.*
/**
  * We have to look into another direction: a for-comprehension plays well in a fail-fast scenario
  * but the structure in our previous example was designed to catch one error at a time, so, 
  * our next step is to tweak the implementation a bit.
  * 
  * Whats changed here?
  * -------------------
  * 1. In this new implementation, we're using a `NonEmptyChain`, a data structure that guarantees
  *    that at least one element will be present. In case that multiple errors arise, you'll get a
  *    chain of DomainValidation.
  * 
  * 2. `ValidatedNec[DomainValidation, A]` == `Validated[NonEmptyChain[DomainValidation], A]`. 
  *    When you use ValidatedNec you're stating that your accumulative structure will be a NonEmptyChain. 
  *    With Validated, you have the choice about which data structure you want for reporting the errors
  * 
  * 3. type alias `ValidationResult` that conveniently expresses the return type of our validation.
  * 4. `.validNec` and `.invalidNec` combinators lets you lift the success or failure in their respective
  *     container (either a Valid or Invalid[NonEmptyChain[A]]).
  * 
  * 5. The applicative syntax `(a, b, c, ...).mapN(...)` provides us a way to accumulatively apply the
  *    validation functions and yield a product with their successful result or the accumulated errors
  *    in the NonEmptyChain. Then, we transform that product with mapN into a valid instance of 
  *    RegistrationData.
*/
sealed trait FormValidatorNec:
  import LearningValidated.{RegistrationData, *}
  type ValidationResult[A] = ValidatedNec[DomainValidation, A]

  private def validateUserName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$"))
    then userName.validNec 
    else UsernameHasSpecialCharacters.invalidNec

  private def validatePassword(password: String): ValidationResult[String] =
    if password.matches("(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")
    then password.validNec
    else PasswordDoesNotMeetCriteria.invalidNec

  private def validateFirstName(firstName: String): ValidationResult[String] =
    if firstName.matches("^[a-zA-Z]+$")
    then firstName.validNec
    else FirstNameHasSpecialCharacters.invalidNec

  private def validateLastName(lastName: String): ValidationResult[String] =
    if lastName.matches("^[a-zA-Z]+$")
    then lastName.validNec
    else LastNameHasSpecialCharacters.invalidNec

  private def validateAge(age: Int): ValidationResult[Int] =
    if age >= 18 && age <= 75 
    then age.validNec
    else AgeIsInvalid.invalidNec

  def validateForm(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    age: Int
  ): ValidationResult[RegistrationData] =
    (
      validateUserName(username),
      validatePassword(password),
      validateFirstName(firstName),
      validateLastName(lastName),
      validateAge(age)
    ).mapN(RegistrationData.apply)

  /**
    * Note that, at the end, we expect to lift the result of the validation functions 
    * in a RegistrationData instance. If the process fails, we'll get our NonEmptyChain
    * detailing what went wrong.
    */
  def formValidatorNecExample() = 
    val res = FormValidatorNec.validateForm(
      username = "Joe",
      password = "Passw0r$1234",
      firstName = "John",
      lastName = "Doe",
      age = 21
    )
    // Valid(a = RegistrationData(username = "Joe", ...)
    println(s"result of validateForm with success ValidatedNec: $res")

    val fail = FormValidatorNec.validateForm(
      username = "Joe%%%",
      password = "password",
      firstName = "John",
      lastName = "Doe",
      age = 21
    )
    // Invalid(
    //    e = Append(
    //         leftNE = Singletion(a = UsernameHasSpecialCharacters), 
    //          rightNE = Singleton(a = PasswordDoesNotMeetCriteria)
    //        )
    //    )
    println(s"result of validateForm with fail ValidatedNec: $fail")
    println(s"Sweet success! Now you can take your validation process to the next level!")

object FormValidatorNec extends FormValidatorNec
  
/**** Combinators and SemiGroup with Validated
------------------------------------------------
As previously stated, ValidatedNec[DomainValidation, A] is an alias for 
Validated[NonEmptyChain[DomainValidation], A]. Typically, you'll see that
Validated is accompanied by a NonEmptyChain when it comes to accumulation. 

The thing here is that you can define your own accumulative data structure
and you're not limited to the aforementioned construction. For doing this, 
you have to provide a Semigroup instance. NonEmptyChain, by definition has 
its own Semigroup. 

Let's take a look about how a Semigroup works in a NonEmptyChain.

We're combining a couple of NonEmptyChains. The first one has its mandatory
element (note that we've built an instance of it with .one) and the second 
has a couple of elements. As you can see, the output of the combination, 
expressed by the |+| operator is another NonEmptyChain with the three elements.

But, what about if we want another way of combining? We can provide our custom
Semigroup instance with the desired combining logic and pass it implicitly/using to 
your scope.
*/
def necAndSemiGroupExample() = 
  import cats.data.NonEmptyChain
  import LearningValidated.*

  // res= Chain(UsernameHasSpecialCharacters, FirstNameHasSpecialCharacters, LastNameHasSpecialCharacters)
  val res = 
    NonEmptyChain.one[DomainValidation](UsernameHasSpecialCharacters) |+| 
    NonEmptyChain[DomainValidation](FirstNameHasSpecialCharacters, LastNameHasSpecialCharacters)
  
  println(s"NonEmptyChain combination result= $res")

  
/** From Validated to Either
 * --------------------------
 * Cats offers us a nice set of combinators for transforming your `Validated`
 * based approach to an `Either` one and vice-versa. We've seen `.toValidated`.
 * There is also .toEither
*/
def toEitherExample() = 
  // Successful case
  val res = FormValidatorNec.validateForm(
    username = "Joe",
    password = "Passw0r$1234",
    firstName = "John",
    lastName = "Doe",
    age = 21
  ).toEither
  println(s"result of validateForm with success ValidatedNec converted toEither: $res\n")
  // res: Either[NonEmptyChain[DomainValidation], RegistrationData] = Right(
  //   value = RegistrationData(
  //     username = "Joe",
  //     password = "Passw0r$1234",
  //     firstName = "John",
  //     lastName = "Doe",
  //     age = 21
  //   )
  // )

  // Invalid case
  val fail = FormValidatorNec.validateForm(
    username = "Joe123#",
    password = "password",
    firstName = "John",
    lastName = "Doe",
    age = 5
  ).toEither
  println(s"result of validateForm with fail ValidatedNec converted toEither: $fail")
  println(s"Sweet success! Now you can take your validation process to the next level!\n")
  // fail: Either[NonEmptyChain[DomainValidation], RegistrationData] = Left(
  //   value = Append(
  //     leftNE = Singleton(a = UsernameHasSpecialCharacters),
  //     rightNE = Append(
  //       leftNE = Singleton(a = PasswordDoesNotMeetCriteria),
  //       rightNE = Singleton(a = AgeIsInvalid)
  //     )
  //   )
  // )

/* 
Another Example showing how Validated helps 
------------------------------------------------
Perhaps you're reading from a configuration file. One could imagine the 
configuration library you're using returns a scala.util.Try, or maybe 
a scala.util.Either. Your parsing may look something like below

  for {
    url  <- config[String]("url")
    port <- config[Int]("port")
  } yield ConnectionParams(url, port)

You run your program and it says key "url" not found, turns out the key 
was "endpoint". So you change your code and re-run. Now it says the 
"port" key was not a well-formed integer. :(

Parallel validation
---------------------------
Our goal is to report any and all errors across independent bits of data. 
For instance, when we ask for several pieces of configuration, each configuration
field can be validated separately from one another. How then do we enforce that 
the data we are working with is independent? We ask for both of them up front.

As our running example, we will look at config parsing. Our config will be
represented by a `Map[String, String]`. Parsing will be handled by a `Read` 
type class - we provide instances just for `String` and `Int` for brevity.
*/
import cats.data.{Validated}
import cats.data.Validated.Invalid
import cats.data.Validated.Valid

object ParallelValidated:

  trait Read[A]:
    def read(s: String): Option[A]

  object Read:
    def apply[A](using ev: Read[A]): Read[A] = ev

    given stringRead: Read[String] = new Read[String]:
      def read(s: String): Option[String] = Some(s)

    given intRead: Read[Int] = new Read[Int]:
      def read(s: String): Option[Int] =
        if (s.matches("-?[0-9]+")) Some(s.toInt) else None

  // our errors
  sealed trait ConfigError
  final case class MissingConfig(field: String) extends ConfigError
  final case class ParseError(field: String) extends ConfigError

  // our parser
  case class Config(map: Map[String, String]):
    def parse[A: Read](key: String): Validated[ConfigError, A] = 
      map.get(key) match
        case None => Invalid(MissingConfig(key))
        case Some(value) => 
          Read[A].read(value) match
            case None => Invalid(ParseError(key))
            case Some(a) => Valid(a)

  /* 
  Everything is in place to write the validate function. Recall that we can only
  do parallel validation if each piece is independent. How do we enforce the 
  data is independent? By asking for all of it up front. Let's start with two 
  pieces of data. 
  */
  def parallelValidateSimple[E, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
    (v1, v2) match
      case (Valid(a), Valid(b))       => Valid(f(a, b))
      case (Valid(_), i@Invalid(_))   => i
      case (i@Invalid(_), Valid(_))   => i
      /* 
      We've run into a problem. In the case where both have errors, we want to report
      both. But we have no way of combining the two errors into one error! Perhaps we 
      can put both errors into a Chain, but that seems needlessly specific - clients 
      may want to define their own way of combining errors.

      How then do we abstract over a binary operation? The Semigroup type class captures
      this idea.
      */
      case (Invalid(e1), Invalid(e2)) => ???

  
  import cats.Semigroup
  def parallelValidate[E : Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
    (v1, v2) match {
      case (Valid(a), Valid(b))       => Valid(f(a, b))
      case (Valid(_), i@Invalid(_))   => i
      case (i@Invalid(_), Valid(_))   => i
      case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
    }
  
  /* 
  Perfect! But going back to our config example, we don't have a way to combine ConfigErrors.
  But as clients, we can change our `Validated` values where the error can be combined. 

  It is common to use a NonEmptyChain[ConfigError] - the NonEmptyChain statically guarantees
  we have at least 1 value, which alings with the fact that if we have an Invalid, then
  we most certainly have at least one error. This technique is so common there is a convenient
  method on `Validated` called `toValidatedNec` that turns any Validated[E, A] value to a
  `Validated[NonEmptyChain[E], A]`. Additionally, the type alias ValidatedNec[E, A] is approved
  */

  def parsingExample() = 
    case class ConnectionParams(url: String, port: Int)
    val config = Config(Map("endpoint" -> "127.0.0.1", "port" -> "not an int"))

    val fail1 = 
      parallelValidate(
        config.parse[String]("url").toValidatedNec,
        config.parse[Int]("port").toValidatedNec
      )(ConnectionParams.apply)
    // fail1: Validated[NonEmptyChain[ConfigError], ConnectionParams] = Invalid(
    //   e = Append(
    //     leftNE = Singleton(a = MissingConfig(field = "url")),
    //     rightNE = Singleton(a = ParseError(field = "port"))
    //   )
    // )
    println(s"result of parallelValidate with fail1 toValidatedNec: $fail1")
    println(s"Sweet success! Now you can take your validation process to the next level!\n")

    val fail2 = 
      parallelValidate(
        config.parse[String]("endpoint").toValidatedNec,
        config.parse[Int]("port").toValidatedNec
      )(ConnectionParams.apply)
    // fail2: Validated[NonEmptyChain[ConfigError], ConnectionParams] = Invalid(
    //   e = Singleton(a = ParseError(field = "port"))
    // )
    println(s"result of parallelValidate with fail2 toValidatedNec: $fail2")
    println(s"Sweet success! Now you can take your validation process to the next level!\n")

    val config2 = Config(Map(("endpoint", "127.0.0.1"), ("port", "1234")))
    val success = 
      parallelValidate(
        config2.parse[String]("endpoint").toValidatedNec,
        config2.parse[Int]("port").toValidatedNec
      )(ConnectionParams.apply)
    // v3: Validated[NonEmptyChain[ConfigError], ConnectionParams] = Valid(
    //   a = ConnectionParams(url = "127.0.0.1", port = 1234)
    // )
    println(s"result of parallelValidate with success ValidatedNec: $success\n")

/**
  * Our `parallelValidate` function looks awfully like the `Apply#map2` function:

      `def map2[F[_], A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]`
    
  * Can we define an Apply instance for Validated? Better yet, can we define an
  * Applicative instance? Yes we can. Below is implementation of Applicative
  * for Validated.
  * 
  * Awesome! now we also get access to all the goodness of Applicative, which 
  * includes map{2-22}, as well as the Semigroupal tuple syntax.
  * 
  * We can now easily ask for several bits of configuration and get any and all
  * errors returned back.
  */
object ApplicativeValidated:
  import cats.{Applicative, Semigroup}

  given validatedApplicative[E: Semigroup]: Applicative[Validated[E, *]] = new Applicative[Validated[E, *]]:
    def ap[A, B](ff: Validated[E, A => B])(fa: Validated[E, A]): Validated[E, B] = 
      (fa, ff) match
        case (Valid(a), Valid(ffab)) => Valid(ffab(a))
        case (i@Invalid(_), Valid(_)) => i
        case (Valid(_), i@Invalid(_)) => i
        case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
      
    def pure[A](x: A): Validated[E, A] = Valid(x)


  def parsingPersonConfigExample() =
    import cats.syntax.all.*
    import ParallelValidated.{Config, ConfigError}
    val personConfig = Config(
      Map(
        "name" -> "cat", 
        "age" -> "not a number", 
        "houseNumber" -> "1234", 
        "lane" -> "feline street"
      )
    )

    case class Address(houseNumber: Int, street: String)
    case class Person(name: String, age: Int, address: Address)

    val fail: ValidatedNec[ConfigError, Person] = 
      Apply[ValidatedNec[ConfigError, *]].map4(
        personConfig.parse[String]("name").toValidatedNec,
        personConfig.parse[Int]("age").toValidatedNec,
        personConfig.parse[Int]("house_number").toValidatedNec,
        personConfig.parse[String]("street").toValidatedNec
      ) {
        case (name, age, houseNumber, street) => Person(name, age, Address(houseNumber, street))
        }
      
    println(s"result of applicative Validate with fail toValidatedNec: $fail")
    println(s"Sweet success! Now you can take your validation process to the next level!\n")

/** VALIDATED HAS ONLY AN APPLICATIVE INSTANCE
 **********************************************
  * Option has flatMap, Either has flatMap, where's Validated's? Let's try to implement it
  * better yet, let's implement the Monad type class.
  * 
  * Note that all Monad instances are also Applicative instances, however, the ap behavior
  * defined in terms of `flatMap` does not behave the same as that of our ap defined above. 
  * Observe:
    validatedMonad.tuple2(Validated.invalidNec[String, Int]("oops"), Validated.invalidNec[String, Double]("uh oh"))
    // Validated[NonEmptyChain[String], (Int, Double)] = Invalid( e = Singleton(a = "oops"))

  * This one short circuits! Therefore, if we were to define a Monad (or FlatMap) instance for 
  * Validated we would have to override ap to get the behavior we want. But then the behavior 
  * of flatMap would be inconsistent with that of ap, and this will violate one of the FlatMap
  *  laws, flatMapConsistentApply:

      def flatMapConsistentApply[F[_], A, B](fa: F[A], fab: F[A => B]): IsEq[F[B]] = 
        fab.ap(fa) <-> fab.flatMap(f => fa.map(f))

    Therefore, VALIDATED HAS ONLY AN APPLICATIVE INSTANCE
  */
object MonadValidated:
  import cats.{Monad, SemigroupK}
  import cats.data.NonEmptyChain
  import ParallelValidated.{ConfigError}

  given necSemigroup: Semigroup[NonEmptyChain[ConfigError]] = SemigroupK[NonEmptyChain].algebra[ConfigError]

  given validatedMonad[E]: Monad[Validated[E, *]] = new Monad[Validated[E, *]]:
    def pure[A](x: A): Validated[E, A] = Valid(x)

    def flatMap[A, B](fa: Validated[E, A])(f: A => Validated[E, B]): Validated[E, B] = 
      fa match
        case Valid(a) => f(a)
        case i@Invalid(e) => i

    @annotation.tailrec
    def tailRecM[A, B](a: A)(f: A => Validated[E, Either[A, B]]): Validated[E, B] = 
      f(a) match
        case Valid(Right(b)) => Valid(b)
        case Valid(Left(e)) => tailRecM(e)(f)
        case i@Invalid(e) => i

    // override def ap[A, B](f: Validated[E, A => B])(fa: Validated[E, A]): Validated[E, B] =
    //   (fa, f) match {
    //     case (Valid(a), Valid(fab)) => Valid(fab(a))
    //     case (i@Invalid(_), Valid(_)) => i
    //     case (Valid(_), i@Invalid(_)) => i
    //     case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
    //   }
      
  def validatedMonadShortcircuits() = 
    println(s"validatedMonad short circuits if you uncomment override def ap: ${validatedMonad.tuple2(
      Validated.invalidNec[String, Int]("oops"),
      Validated.invalidNec[String, Double]("uh oh")
    )}\n")
    // Validated[NonEmptyChain[String], (Int, Double)] = Invalid(e = Singleton(a = "oops"))

/** Sequential Validation
  * -----------------------
  * If you do want error accumulation but occasionally run into places where
  * sequential validation is needed, then Validated provides a couple methods 
  * that may be helpful.
  * 
  * `andThen`
  * -----------
  * similar to flatMap. In the case of success, it passes the valid value into a 
  * function that returns a new `Validated` instance.  Apply a function (that returns
  * a Validated) in the valid case. Otherwise return the original Validated.
  * 
  * This allows "chained" validation: the output of one validation can be fed into
  * another validation function. This function is similar to flatMap on Either.
  * It's not called flatMap, because by Cats convention, flatMap is a monadic bind
  * that is consistent with ap. This method is not consistent with ap (or other 
  * Apply-based methods), because it has "fail-fast" behavior as opposed to 
  * accumulating validation failures.
  * 
  * `withEither`
  * -------------
  * allows you to temporarily turn a Validated instance into an Either instance
  * and apply it to a function. Convert to an Either, apply a function, convert 
  * back. This is handy when you want to use the Monadic properties of the Either type.
  * 
*/
def validatedHelpersForSequentialValidation() =
  import ParallelValidated.{Config, ParseError, ConfigError}
  val config = Config(
      Map(
        "name" -> "cat", 
        "age" -> "not a number", 
        "houseNumber" -> "1234", 
        "lane" -> "feline street"
      )
    )
  /* andThen useage */ 
  val fail = config.parse[Int]("house_number").andThen{ n =>
    if (n >= 0) Validated.valid(n)
    else Validated.invalid(ParseError("house_number"))
  }
  println(s"validatedHelpersForSequentialValidation andThen: $fail")
  // houseNumber: Validated[ConfigError, Int] = Invalid(e = MissingConfig(field = "house_number"))

  /* withEither useage. function positive returns Either types */
  def positive(field: String, i: Int): Either[ConfigError, Int] = {
    if (i >= 0) Right(i)
    else Left(ParseError(field))
  }
  val fail2 = config.parse[Int]("house_number").withEither(
    either => either.flatMap(i => positive("house_number", i))
  )
  println(s"validatedHelpersForSequentialValidation withEither: $fail2 \n")
  // houseNumber: Validated[ConfigError, Int] = Invalid(e = MissingConfig(field = "house_number"))

@main def validatedExamples() = 
  import learning.datatypes.LearningValidated.*
  import FormValidatorNec.formValidatorNecExample

  forComprehensionisFailFast() 
  formValidatorNecExample()
  necAndSemiGroupExample()
  toEitherExample()
  ParallelValidated.parsingExample()
  ApplicativeValidated.parsingPersonConfigExample()
  // VALIDATED HAS ONLY AN APPLICATIVE INSTANCE
  MonadValidated.validatedMonadShortcircuits()
  validatedHelpersForSequentialValidation()
