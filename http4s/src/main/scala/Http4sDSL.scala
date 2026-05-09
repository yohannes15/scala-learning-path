import org.http4s.HttpRoutes
import cats.effect.IO
import org.http4s.Response
import org.http4s.Status
import org.http4s.Request
import org.http4s.Method
import org.http4s.implicits.*
import org.http4s.dsl.io.*
import cats.effect.unsafe.IORuntime

/** The http4s DSL
  *
  * Recall from earlier than HttpRoutes[F] is just a type alias for
  * Kleisli[OptionT[F, *], Request[F], Response[F]]. This provides a minimal
  * foundation for declaring services and executing them on blaze or a servlet
  * container. While this foundation is composable, it is not highly productive.
  * Most service authors will seek a higher level DSL.
  *
  * Add the http4s-dsl to your build
  *
  * One option is the `http4s-dsl`. It is officially supported by the http4s
  * team, but kept separate from core in order to encourage multiple approaches
  * for different needs.
  *
  * {{{
  *   libraryDependencies ++= Seq("org.http4s" %% "http4s-dsl" % http4sVersion, )
  * }}}
  *
  * The Simplest Service
  *
  * The central concept of http4s-dsl is pattern matching. An `HttpRoutes[F]` is
  * declared as a simple series of case statements. Each case statement attempts
  * to match and optionally extract from an incoming `Request[F]`. The code
  * associated with the first matching case is used to generate a
  * `F[Response[F]]`
  *
  * The simplest case statement matches all requests without extracting
  * anything. The right hand side of the request must return a `F[Response[F]]`
  *
  * In the following we use `cats.Effect.IO` as the effect type `F`
  *
  * SIDENOTE: If you're in a REPL, we also need a runtime:
  *
  * {{{
  * import cats.effect.unsafe.IORuntime
  * implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  * }}}
  */
val service = HttpRoutes.of[IO] {
  case _ => IO(Response(Status.Ok))
}

/** Testing the Service
  *
  * One beautiful thing about the HttpRoutes[F] model is that we don't need a
  * server to test our route. We can construct our own request and experiment
  * directly in the REPL.
  *
  * Where is our Response[F]? It hasn't been created yet. We wrapped it in an
  * IO. In a real service, generating a Response[F] is likely to be an
  * asynchronous operation with side effects, such as invoking another web
  * service or querying a database, or maybe both. Operating in a F gives us
  * control over the sequencing of operations and lets us reason about our code
  * like good functional programmers.
  *
  * NOTE: It is the HttpRoutes[F]'s job to describe the task, and the server's
  * job to run it.
  */
@main def simpleService() =
  val getRoot = Request[IO](Method.GET, uri"/")
  val serviceIO = service.orNotFound.run(getRoot)
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  val response = serviceIO.unsafeRunSync()
  // response: Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@5ed6cbaf
  // )

  /** Generating Responses
    *
    * We'll circle back to more sophisticated pattern matching of requests, but
    * it will be a tedious affair until we learn a more succinct way of
    * generating F[Response]s.
    *
    * Status codes
    *
    * http4s-dsl provides a shortcut to create an F[Response] by applying a
    * status code:
    */
  val okIo: IO[Response[IO]] = Ok()

  /** This simple Ok() expression succinctly says what we mean in val service
    * above
    */
  val a = HttpRoutes.of[IO] {
    case _ => Ok()
  }.orNotFound.run(getRoot).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@5a98f1d0
  // )

  /** This syntax works for other status codes as well. In our example, we don't
    * return a body, so a 204 No Content would be a more appropriate response:
    */
  val b = HttpRoutes.of[IO] {
    case _ => NoContent()
  }.orNotFound.run(getRoot).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 204),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@a84573d
  // )

  /** Some other examples are: */
  val c = HttpRoutes.of[IO] {
    case _ => Conflict()
  }.orNotFound.run(getRoot).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 409),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@473c4d71
  // )

  val d = HttpRoutes.of[IO] {
    case _ => Created()
  }.orNotFound.run(getRoot).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 201),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@5a576b33
  // )

  val e = HttpRoutes.of[IO] {
    case _ => Forbidden()
  }.orNotFound.run(getRoot).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 403),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@6b356f1e
  // )

/** Headers
  *
  * http4s adds a minimum set of headers depending on the response, e.g:
  */

object Headers:
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  val headers = Ok("Ok response!").unsafeRunSync().headers
  // Headers = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 12)
