import cats.{FlatMap, Functor}
import cats.syntax.all.*

/** Functions
  *
  * One of the most useful properties of functions is that they compose. That
  * is, given a function A => B and a function B => C, we can combine them to
  * create a new function A => C. It is through this compositional property that
  * we are able to write many small functions and compose them together to
  * create a larger one that suits our needs.
  *
  * {{{
  *   val twice: Int => Int = x => x * 2
  *
  *   val countCats: Int => String = x => if (x == 1) "1 cat" else s"$x cats"
  *
  *   val twiceAsManyCats: Int => String = twice andThen countCats
  *   // equivalent to: countCats compose twice
  *
  *   twiceAsManyCats(1) // "2 cats" // res0: String = "2 cats"
  * }}}
  *
  * Sometimes, our functions will need to return monadic values. For instance,
  * consider the following set of functions.
  *
  * {{{
  *   val parse: String => Option[Int] =
  *     s => if (s.matches("-?[0-9]+")) Some(s.toInt) else None
  *
  *   val reciprocal: Int => Option[Double] =
  *   i => if (i != 0) Some(1.0 / i) else None
  * }}}
  *
  * NOTE: As it stands we cannot use Function1.compose (or Function1.andThen) to
  * compose these two functions. The output type of parse is Option[Int] whereas
  * the input type of reciprocal is Int. This is where Kleisli comes into play.
  * Kleisli enables composition of functions that return a monadic value, for
  * instance an Option[Int] or a Either[String, List[Double]], without having
  * functions take an Option or Either as a parameter, which can be strange and
  * unwieldy.
  */
object LearningKleisli:

  /** At its core, Kleisli[F[_], A, B] is just a wrapper around the function A =>
    * F[B]. Depending on the properties of the F[_], we can do different things
    * with Kleislis. For instance, if F[_] has a FlatMap[F] instance (we can
    * call flatMap on F[A] values), we can compose two Kleislis much like we can
    * two functions.
    *
    * It is important to note that the F[_] having a FlatMap (or a Monad)
    * instance is not a hard requirement - we can do useful things with weaker
    * requirements. Such an example would be Kleisli#map, which only requires
    * that F[_] have a Functor instance (e.g. is equipped with map: F[A] => (A =>
    * B) => F[B]).
    *
    * Below are some more methods on Kleisli that can be used as long as the
    * constraint on F[_] is satisfied.
    *
    *   - andThen | FlatMap
    *   - compose | FlatMap
    *   - flatMap | FlatMap
    *   - lower | Monad
    *   - map | Functor
    *   - traverse | Applicative
    */
  final case class Kleisli[F[_], A, B](run: A => F[B]):
    def compose[Z](k: Kleisli[F, Z, A])(using F: FlatMap[F]): Kleisli[F, Z, B] =
      Kleisli[F, Z, B](z => k.run(z).flatMap(run))

    def map[C](f: B => C)(using F: Functor[F]): Kleisli[F, A, C] =
      Kleisli[F, A, C](a => F.map(run(a))(f))

  def exampleEffectfulCompose() =
    val parse: Kleisli[Option, String, Int] =
      Kleisli(s => if (s.matches("-?[0-9]+")) Some(s.toInt) else None)

    val reciprocal: Kleisli[Option, Int, Double] =
      Kleisli(i => if (i != 0) Some(1.0 / i) else None)

    val parseAndReciprocal: Kleisli[Option, String, Double] =
      reciprocal.compose(parse)
