import cats.effect.*
import cats.syntax.all.*
import org.typelevel.ci.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.client.Client
import cats.effect.unsafe.IORuntime
import scala.concurrent.duration.*
import cats.effect.std.Random
import fs2.Stream
import cats.effect.std.Console

given runtime: IORuntime = cats.effect.unsafe.implicits.global

object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

/** Server Middleware
  *
  * Http4s includes some middleware out of the box in the
  * `org.http4s.server.middleware` package. Some of it is document in its own
  * page:
  *
  *   - Authentication (https://http4s.org/v0.23/docs/auth.html)
  *   - Cross Origin Resource Sharing AKA CORS
  *     (https://http4s.org/v0.23/docs/cors.html)
  *   - Response Compression AKA GZip (https://http4s.org/v0.23/docs/gzip.html)
  *   - HSTS (https://http4s.org/v0.23/docs/hsts.html)
  *   - CSRF (https://http4s.org/v0.23/docs/csrf.html)
  *
  * We'll provide descriptions for the remaining middleware, but first lets set
  * up the service.
  *
  * NOTE: these examples might use non-idiomatic constructs like unsafeRunSync
  * for conciseness.
  */
@main def runService() =
  val service = HttpRoutes.of[IO] {
    case GET -> Root / "bad"       => BadRequest()
    case GET -> Root / "ok"        => Ok()
    case r @ POST -> Root / "post" => r.as[Unit] >> Ok()
    case r @ POST -> Root / "echo" => r.as[String].flatMap(Ok(_))
    case GET -> Root / "b" / "c"   => Ok()
    case POST -> Root / "queryForm" :? NameQueryParamMatcher(name) =>
      Ok(s"hello $name")
    case GET -> Root / "wait" => IO.sleep(10.millis) >> Ok()
    case GET -> Root / "boom" => IO.raiseError(new RuntimeException("boom!"))
    case r @ POST -> Root / "reverse" =>
      r.as[String].flatMap(s => Ok(s.reverse))
    case GET -> Root / "forever" => IO(
        Response[IO](headers = Headers("hello" -> "hi"))
          .withEntity(Stream.constant("a").covary[IO])
      )
    case r @ GET -> Root / "doubleRead" => (r.as[String], r.as[String])
        .flatMapN((a, b) => Ok(s"$a == $b"))

    case GET -> Root / "random" => Random.scalaUtilRandom[IO]
        .flatMap(_.nextInt)
        .flatMap(random => Ok(random.toString))
  }

  val okRequest = Request[IO](Method.GET, uri"/ok")
  val badRequest = Request[IO](Method.GET, uri"/bad")
  val postRequest = Request[IO](Method.POST, uri"/post")
  val waitRequest = Request[IO](Method.GET, uri"/wait")
  val boomRequest = Request[IO](Method.GET, uri"/boom")
  val reverseRequest = Request[IO](Method.POST, uri"/reverse")
  // val client = Client.fromHttpApp(service.orNotFound)

  /** Headers Middleware */

  /** NOTE: Caching
    *
    * This middleware adds response headers so that clients know how to cache a
    * response. It performs no server-side caching. See
    * https://shorturl.at/WeFX8
    */
  import org.http4s.server.middleware.Caching

  val cacheService = Caching.cache(
    lifetime = 3.hours,
    isPublic = Left(CacheDirective.public),
    methodToSetOn = _ == Method.GET,
    statusToSetOn = _.isSuccess,
    http = service
  ).orNotFound

  val cacheClient = Client.fromHttpApp(cacheService)
  val h1 = cacheClient.run(okRequest).use(_.headers.pure[IO]).unsafeRunSync()
  println(s"cacheClient: $h1")
  // Headers = Headers(
  //  Content-Length: 0, Cache-Control: public, max-age=10800,
  //  Date: Tue, 05 May 2026 15:01:46 GMT, Expires: Tue, 05 May 2026 18:01:46 GMT
  // )
  val h2 = cacheClient.run(badRequest).use(_.headers.pure[IO]).unsafeRunSync()
  println(h2)
  // Headers = Headers(Content-Length: 0)
  val h3 = cacheClient.run(postRequest).use(_.headers.pure[IO]).unsafeRunSync()
  println(h3)
  // Headers = Headers(Content-Length: 0)

  /** NOTE: Date
    *
    * Adds the current date to the response.
    */
  import org.http4s.server.middleware.Date

  val dateService = Date.httpRoutes(service).orNotFound
  val dateClient = Client.fromHttpApp(dateService)

  val dh = dateClient.run(okRequest).use(_.headers.pure[IO]).unsafeRunSync()
  println(s"dateClient: $dh")
  // Headers = Headers(Content-Length: 0, Date: Tue, 05 May 2026 15:01:46 GMT)
