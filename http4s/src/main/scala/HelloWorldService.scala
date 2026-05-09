import cats.syntax.all.*
import org.http4s.implicits.*
import org.http4s.HttpRoutes
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.dsl.io.*
import org.http4s.EntityEncoder
import scala.concurrent.duration.*
import org.http4s.server.Router
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import cats.effect.IOApp
import cats.effect.ExitCode

/** An HttpRoutes[F] is a simple alias for
  *
  * -> `Kleisli[OptionT[F, *], Request, Response]`
  *
  * If that's meaningful to you, great. If not, don't panic: Kleisli is just a
  * convenient wrapper around a Request => F[Response], and F is an effectful
  * operation. https://typelevel.org/cats/datatypes/kleisli.html
  *
  * Using the http4s-dsl, we can construct an HttpRoutes by pattern matching the
  * request. Let's build a service that matches requests to GET /hello/:name,
  * where :name is a path parameter for the person to greet.
  */
val helloWorldService = HttpRoutes.of[IO] {
  case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
}

/** Returning Content in the Response
  *
  * In order to return content of type `T` in the response, an
  * `EntityEncoder[T]` must be used. We can define the `EntityEncoder[T]`
  * implicitly so that it doesn't need to be explicitly included when serving
  * the response.
  *
  * In the example below, we're defining a tweetEncoder and then explicitly
  * using it to encode the response contents of a Tweet, which can be seen as
  * `Ok(getTweet(tweetId))(tweetEncoder)`.
  *
  * We've defined tweetsEncoder as being implicit so that we don't need to
  * explicitly reference it when serving the response, which can be seen as
  * `getPopularTweets().flatMap(Ok(_))`.
  */
object TweetService:
  case class Tweet(id: Int, message: String)

  given tweetEncoder: EntityEncoder[IO, Tweet] = ???
  given tweetsEncoder: EntityEncoder[IO, Seq[Tweet]] = ???

  def getTweet(tweetId: Int): IO[Tweet] = ???
  def getPopularTweets(): IO[Seq[Tweet]] = ???

  val tweetService = HttpRoutes.of[IO] {
    case GET -> Root / "tweets" / "popular" => getPopularTweets().flatMap(Ok(_))
    case GET -> Root / "tweets" / IntVar(tweetId) =>
      getTweet(tweetId).flatMap(Ok(_))

  }

/** Running Your Service
  *
  * http4s supports multiple server backends. In this example, we'll use
  * `ember`, the native backend supported by http4s.
  *
  * NOTE: We start from a EmberServerBuilder, and then mount the
  * helloWorld/tweet Service under the base path of / and the remainder of the
  * services under the base path of /api. The services can be mounted in any
  * order as the request will be matched against the longest base paths first.
  * The EmberServerBuilder is immutable with chained methods, each returning a
  * new builder
  *
  * Multiple HttpRoutes can be combined with the `combineK` method (or its alias
  * <+>) by importing `cats.implicits.*` and org.http4s.implicits.*
  *
  * Running Your Service as an App
  *
  * As a convenience, cats-effect provides an cats.effect.IOApp trait with an
  * abstract run method that returns a IO[ExitCode]. An IOApp runs the process
  * and adds a JVM shutdown hook to interrupt the infinite process and
  * gracefully shut down your server when a SIGTERM is received.
  */
object HelloWorldService extends IOApp:
  // val services = TweetService.tweetService <+> helloWorldService
  val services = helloWorldService.combineK(TweetService.tweetService)
  val httpApp = Router(
    "/" -> helloWorldService,
    "/api" -> services
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
