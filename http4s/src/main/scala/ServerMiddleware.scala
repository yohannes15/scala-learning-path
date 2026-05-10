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

/** Server Middleware (https://http4s.org/v0.23/docs/server-middleware.html)
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
  val client = Client.fromHttpApp(service.orNotFound)

//*******************************************************************************
//**************************Headers Middleware **********************************
//*******************************************************************************

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

  /** NOTE: HeaderEcho
    *
    * Adds headers included in the request to the response.
    */
  import org.http4s.server.middleware.HeaderEcho
  val echoService =
    HeaderEcho.httpRoutes(echoHeadersWhen = _ => true)(service).orNotFound
  val echoClient = Client.fromHttpApp(echoService)

  val he = echoClient.run(
    okRequest.putHeaders("Hello" -> "hi")
  ).use(_.headers.pure[IO]).unsafeRunSync()
  // Headers = Headers(Content-Length: 0, Hello: Hi)

  /** NOTE: Response Timing
    *
    * Sets response header with the request duration.
    */
  import org.http4s.server.middleware.ResponseTiming

  val timingService = ResponseTiming(service.orNotFound)
  val timingClient = Client.fromHttpApp(timingService)

  val tihe = timingClient.run(okRequest).use(_.headers.pure[IO]).unsafeRunSync()
  // Headers = Headers(Content-Length: 0, X-Response-Time: 0)

  /** NOTE: RequestId
    *
    * Use the RequestId middleware to automatically generate a X-Request-ID
    * header for a request, if one wasn't supplied. Adds a X-Request-ID header
    * to the response with the id generated or supplied as part of the request.
    */
  import org.http4s.server.middleware.RequestId

  val requestIdService = RequestId.httpRoutes(HttpRoutes.of[IO] {
    case req =>
      val reqId = req.headers.get(ci"X-Request-ID").fold("null")(_.head.value)
      // use request id to correlate logs with the request
      Console[IO].println(s"request recieved. cid=$reqId") *> Ok()
  })

  val requestIdClient = Client.fromHttpApp(requestIdService.orNotFound)
  // Note: req.attributes.lookup(RequestId.requestIdAttrKey) can also be used
  // to lookup the request id extracted from the header, or the generated request id.

  val rehe: (org.http4s.Headers, Option[String]) =
    requestIdClient.run(okRequest).use(resp =>
      (
        resp.headers,
        resp.attributes.lookup(RequestId.requestIdAttrKey)
      ).pure[IO]
    ).unsafeRunSync()
  // request received, cid=e2480ff5-f2b8-4a68-a907-06455b647acc
  // (Headers, Option[String]) = (
  //   Headers(Content-Length: 0, X-Request-ID: e2480ff5-f2b8-4a68-a907-06455b647acc),
  //   Some(value = "e2480ff5-f2b8-4a68-a907-06455b647acc")
  // )

  /** NOTE: StaticHeaders
    *
    * Adds static headers to the response.
    */
  import org.http4s.server.middleware.StaticHeaders

  val sHService = StaticHeaders(Headers("X-Hello" -> "hi"))(service).orNotFound
  val sHClient = Client.fromHttpApp(sHService)
  val sh = sHClient.run(okRequest).use(_.headers.pure[IO]).unsafeRunSync()
  // Headers = Headers(X-Hello: hi, Content-Length: 0)

//*******************************************************************************
//******************Request Rewriting Middleware*********************************
//*******************************************************************************

  /** NOTE: Auto Slash
    *
    * Removes a trailing slash from the requested url so that requests with
    * trailing slash map to the route without.
    */
  import org.http4s.server.middleware.AutoSlash
  val autoSlashService = AutoSlash(service).orNotFound
  val autoSlashClient = Client.fromHttpApp(autoSlashService)
  val okWithSlash = Request[IO](Method.GET, uri"/ok/")

  // without the middleware the request with trailing slash fails
  val tsuccess = client.status(okRequest).unsafeRunSync()
  // Status = Status(code = 200)
  val t404 = client.status(okWithSlash).unsafeRunSync()
  // Status = Status(code = 404)
  // with the middleware both work
  val asSuccess = autoSlashClient.status(okRequest).unsafeRunSync()
  // Status = Status(code = 200)
  val withSuccess = autoSlashClient.status(okWithSlash).unsafeRunSync()
  // res11: Status = Status(code = 200)
