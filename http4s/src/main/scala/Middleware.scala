import cats.data.Kleisli
import cats.syntax.all.*
import org.http4s.implicits.*
import cats.effect.unsafe.IORuntime
import org.http4s.HttpRoutes
import org.http4s.Header
import cats.effect.IO
import org.http4s.Request
import org.http4s.Status
import org.http4s.dsl.io.*
import org.http4s.Method
import org.http4s.Response
import org.http4s.AuthedRequest
import org.http4s.server.Middleware
import cats.data.OptionT

given runtime: IORuntime = cats.effect.unsafe.IORuntime.global

/** Middleware
  *
  * A middleware is an abstraction around a service that provides a means of
  * manipulating the `Request` sent to service, and/or the `Response` returned
  * by the service. In some cases, such as `Authentication`, middleware may even
  * prevent the service from being called.
  *
  * At its most basic, middleware is a function that takes one service and
  * returns another. The middleware function can take any additional params it
  * needs to perform its task.
  *
  * {{{
  *   libraryDependencies ++= Seq(
  *     "org.http4s" %% "http4s-dsl" % VERSION
  *   )
  * }}}
  */
object LearningMiddleware:

  /** middleware that adds a header to successful responses. All we do here is
    * pass the request to the service, which returns an F[Response]. So, we use
    * map to get the request out of the task, add the header if the response is
    * a success, and then pass the response on. We could just as easily modify
    * the request before we passed it to the service.
    */
  def addHeaderMware(
      service: HttpRoutes[IO],
      header: Header.ToRaw
  ): HttpRoutes[IO] =
    Kleisli { (req: Request[IO]) =>
      service(req).map {
        case Status.Successful(resp) => resp.putHeaders(header)
        case resp                    => resp
      }
    }

  /** Now, let's create a simple service. HttpRoutes is implemented as a
    * Kleisli, which is just a function at heart, we can test a service without
    * a server. HttpRoutes[F] returns a F[Response[F]], we need to call
    * unsafeRunSync on the result of the function to extract the Response[F].
    * NOTE: that basically, you shouldn't use unsafeRunSync in your application.
    * Here we use it for demo reasons only.
    */
  val service = HttpRoutes.of[IO] {
    case GET -> Root / "bad" => BadRequest()
    case _                   => Ok()
  }

  val goodRequest = Request[IO](Method.GET, uri"/")
  val badRequest = Request[IO](Method.GET, uri"/bad")

  val res = service.orNotFound(goodRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@5ea8592b
  // )
  val res1 = service.orNotFound(badRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 400),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@291ddf84
  // )

  /** Now, we'll apply the service to our middleware function to create a new
    * service, and try it out.
    */
  val modifiedService = addHeaderMware(service, "SomeKey" -> "SomeValue")
  val mres = modifiedService.orNotFound(goodRequest).unsafeRunSync()
  // Note that the successful response has header added to it.
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0, SomeKey: SomeValue),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@101095dd
  // )
  val mres1 = modifiedService.orNotFound(badRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 400),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@67c241da
  // )

  /** If you intend to use you middleware in multiple places, you may want to
    * implement it as an object and use the apply method.
    */
  object addHeaderMiddleware:
    def addHeader(resp: Response[IO], header: Header.ToRaw): Response[IO] =
      resp match
        case Status.Successful(resp) => resp.putHeaders(header)
        case resp                    => resp

    def apply(service: HttpRoutes[IO], header: Header.ToRaw): HttpRoutes[IO] =
      service.map(addHeader(_, header))

  val newService = addHeaderMiddleware(service, "SomeKey" -> "SomeValue")

  val mnres = newService.orNotFound(goodRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0, SomeKey: SomeValue),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@4a29e684
  // )
  val mnres1 = newService.orNotFound(badRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 400),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@1d2d48ef
  // )

/** Let's consider Authentication middleware as an example. Authentication
  * middleware is a function that takes `AuthedRoutes[F]`, translates to:
  *
  *   - {{{AuthedRequest[F, T] => F[Option[Response[F]]])}}}
  *
  * and returns HttpRoutes[F], translates to:
  *
  *   - {{{Request[F] => F[Option[Response[F]]])}}}
  *
  * There is a type defined for this in the `http4s.server` package. See here
  * https://http4s.org/v0.23/docs/auth.html for more details
  */
object AuthMiddleQuickConsideration:
  type AuthMiddleware[F[_], T] = Middleware[
    OptionT[F, *],
    AuthedRequest[F, T],
    Response[F],
    Request[F],
    Response[F]
  ]

/** Composing Services with Middleware
  *
  * Since middleware returns a Kleisli, you can compose it with another
  * middleware. Additionally, you can compose services before applying the
  * middleware function, and/or compose services with the service obtained by
  * applying some middleware function.
  *
  * In example below:
  *
  * NOTE: goodRequest ran through the addHeaderMiddleWare and the Result had our
  * header added to it. But, apiRequest did not go through the middleware and
  * did not have the header added to it's Result.
  */
object ComposingMiddleware:
  import LearningMiddleware.addHeaderMiddleware
  val apiService = HttpRoutes.of[IO] {
    case GET -> Root / "api" => Ok()
  }

  val anotherService = HttpRoutes.of[IO] {
    case GET -> Root / "another" => Ok()
  }

  val aggregateService = apiService <+> addHeaderMiddleware(
    LearningMiddleware.service <+> anotherService,
    "SomeKey" -> "SomeValue"
  )

  val apiRequest = Request[IO](Method.GET, uri"/api")

  val res =
    aggregateService.orNotFound(LearningMiddleware.goodRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0, SomeKey: SomeValue),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@52628207
  // )
  val res1 = aggregateService.orNotFound(apiRequest).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Length: 0),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@2e893711
  // )
