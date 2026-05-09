import org.http4s.HttpRoutes
import cats.effect.IO
import cats.syntax.all.*
import cats.implicits.*
import org.http4s.Response
import org.http4s.Status
import org.http4s.Request
import org.http4s.Method
import org.http4s.implicits.*
import org.http4s.dsl.io.*
import cats.effect.unsafe.IORuntime
import org.http4s.headers.`Cache-Control`
import cats.data.NonEmptyList
import org.http4s.CacheDirective.`no-cache`
import org.http4s.ResponseCookie
import org.http4s.HttpDate
import scala.concurrent.duration.*
import scala.util.Try
import java.time.LocalDate
import org.http4s.QueryParamDecoder
import org.http4s.QueryParamCodec
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import org.http4s.ParseFailure

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
  * http4s adds a minimum set of headers depending on the response.
  *
  * Extra headers can be added using `putHeaders` for example to specify cache
  * policies.
  *
  * http4s defines all the well known headers directly, but sometimes you need
  * to define custom headers, typically prefixed by an `X-`. In simple cases you
  * can construct a Header instance by hand!
  */
object Headers:
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  val headers = Ok("Ok response!").unsafeRunSync().headers
  // Headers = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 12)

  val ok = Ok("Ok response.", `Cache-Control`(NonEmptyList(`no-cache`(), Nil)))
    .unsafeRunSync().headers
  // Headers = Headers(
  //  Content-Type: text/plain; charset=UTF-8,
  //  Cache-Control: no-cache,
  //  Content-Length: 12
  // )

  val ok2 =
    Ok("Ok response.", "X-Auth-Token" -> "value").unsafeRunSync().headers
  // Headers = Headers(
  //  Content-Type: text/plain; charset=UTF-8,
  //  X-Auth-Token: value,
  //  Content-Length: 12
  // )

/** Cookies
  *
  * http4s has special support for Cookie headers using the `Cookie` type to add
  * and invalidate cookies. Adding a cookie will generate the correct
  * `Set-Cookie` header
  */
object Cookies:
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  val ok = Ok("Ok Response!").map(_.addCookie(ResponseCookie(
    "foo",
    "bar"
  ))).unsafeRunSync().headers
  // Headers = Headers(
  //  Content-Type: text/plain; charset=UTF-8,
  //  Content-Length: 12,
  //  Set-Cookie: foo=bar
  // )

  /** Cookie can be further customized to set, e.g., expiration, the secure
    * flag, httpOnly, flag, etc
    */
  val cookieResp =
    for
      resp <- Ok("Ok response.")
      now <- HttpDate.current[IO]
    yield resp.addCookie(ResponseCookie(
      "foo",
      "bar",
      expires = Some(now),
      httpOnly = true,
      secure = true
    ))

  val headers: org.http4s.Headers = cookieResp.unsafeRunSync().headers
  // Headers = Headers(
  //  Content-Type: text/plain; charset=UTF-8,
  //  Content-Length: 12,
  //  Set-Cookie: foo=bar; Expires=Tue, 05 May 2026 15:01:31 GMT; Secure; HttpOnly
  // )

  /** To request a cookie to be removed on the client, you need to set the
    * cookie value to empty. http4s can do that with removeCookie:
    */
  val headers2: org.http4s.Headers =
    Ok("ok response!").map(_.removeCookie("foo")).unsafeRunSync().headers
  // Headers = Headers(
  //  Content-Type: text/plain; charset=UTF-8,
  //  Content-Length: 12,
  //  Set-Cookie: foo=; Expires=Thu, 01 Jan 1970 00:00:00 GMT
  // )

/** Responsing with a `Body`
  *
  * Simple Bodies
  *
  * Most status codes take an argument as a body. In http4s, `Request[F]` and
  * `Response[F]` bodies are represented as a `fs2.Stream[F, Byte]`. It's also
  * considered good HTTP manners to provide a `Content-Type` and, where known in
  * advance, `Content-Length` header in one's responses.
  *
  * This is neatly handled by http4s' `EntityEncoder`s. We'll cover these in
  * more depth later. The imporant point for now is that a response body can be
  * generated for any type with an implicit `EntityEncoder` in scope. http4s
  * prvides several out of the box.
  *
  * Per the HTTP specification, some status codes don't support a body. http4s
  * prevents such nonsense at compile time:
  *
  * NoContent("does not compile")
  */
object ResponseBodies:
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  val ok = Ok("Received Request.").unsafeRunSync()
  // Response[IO] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 17),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@513cc49d
  // )

  import java.nio.charset.StandardCharsets.UTF_8
  val ok2 = Ok("binary".getBytes(UTF_8)).unsafeRunSync()
  // Response[IO] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Type: application/octet-stream, Content-Length: 6),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@464e89fe
  // )

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  /** Asynchronous Responses
    *
    * While http4s prefers `F[_]: Async`, you may be working with libraries that
    * use standard library like `Future`.
    *
    * You can respond with a `Future` of any type that has an `EntityEncoder` by
    * lifting it into an IO or any F[_] that suspends future. Note: unlike IO,
    * wrapping a side effect in Future doesn't suspend it, and the resulting
    * expression would still be side effectful
    *
    * `IO.fromFuture` ensures that the suspended future is shifted to the
    * correct thread pool.
    *
    * NOTE: In both cases, a Content-Length header is calculated. http4s waits
    * for the Future or F to complete before wrapping it in its HTTP envelope,
    * and thus has what it needs to calculate a Content-Length.
    */
  val ioFuture = Ok(IO.fromFuture(IO(Future {
    println("I run when the future is constructed")
    "greetings from the future!"
  })))

  val res = ioFuture.unsafeRunSync()
  // I run when the future is constructed.
  // Response[IO] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 26),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@58f63ddb
  // )

  // As good functional programmers who like to delay our side effects,
  // we of course prefer to operate in Fs:
  val io = Ok(IO {
    println("I run when the IO is run")
    "Mission accomplished"
  })

  val res2 = io.unsafeRunSync()
  // I run when the IO is run.
  // Response[IO] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 21),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@59b6972f
  // )

  import fs2.Stream

  /** Streaming Bodies
    *
    * Streaming bodies are supported by returning a `fs2.Stream`. Like `IO`, the
    * stream may be of any type that has an `EntityEncoder`. An intro to
    * `Stream` is out of scope, but we can glimpse the power here. This stream
    * emits the elapsed time every 100 milliseconds for one second.
    */
  val drip: Stream[IO, String] =
    Stream.awakeEvery[IO](100.millis).map(_.toString).take(10)

  val dripOutIO = drip
    .through(fs2.text.lines)
    .evalMap(s => IO { println(s); s })
    .compile
    .drain
  // IO[Unit] = Uncancelable(
  //   body = cats.effect.IO$$$Lambda$20182/0x0000000804ce7040@2038a56e,
  //   event = cats.effect.tracing.TracingEvent$StackTrace
  // )
  dripOutIO.unsafeRunSync()
  /* 101745104 nanoseconds200897022 nanoseconds300802612 nanoseconds400815541
   * nanoseconds500806109 nanoseconds600752667 nanoseconds700732613
   * nanoseconds800813478 nanoseconds900734637 nanoseconds1000707753
   * nanoseconds
   */

  /** When wrapped in a Response[F], http4s will flush each chunk of a Stream as
    * they are emitted.
    *
    * NOTE: a stream's length can't generally be anticipated before it runs, so
    * this triggers chunked transfer encoding:
    */
  val dripOk = Ok(drip)
  // IO[Response[IO]] = Pure(
  //   value = (
  //     `` = Status(code = 200),
  //     `` = HttpVersion(major = 1, minor = 1),
  //     `` = Headers(Content-Type: text/plain; charset=UTF-8, Transfer-Encoding: chunked),
  //     `` = Stream(..),
  //     `` = org.typelevel.vault.Vault@673f2887
  //   )
  // )

/** Matching and Extracting Requests
  *
  * NOTE: A `Request` is a regular case class - you can destructure it to
  * extract its values. By extension, you can also match/case it with different
  * possible destructurings. To build these different extractors, you can make
  * use of the DSL
  *
  * The -> object
  *
  * More often, you extract the Request into a HTTP Method and path info via the
  * `->` object. On the left side is the method, and on the right side, the path
  * info. The following matches a request to `GET /hello`
  *
  * Path Info
  *
  * Path matching is done on the request's `pathInfo`. Path info is the
  * request's URI's path after the following:
  *
  *   - the mount point of the service
  *   - the prefix, if the service is composed with a `Router`
  *   - the prefix, if the service is rewritten with `TranslateUri`
  *
  * Matching on request.pathInfo instead of request.uri.path allows multiple
  * services to be composed without rewriting all the path matchers.
  */
object Requests:
  val route = HttpRoutes.of[IO] {
    // Methods such as GET are typically found in org.http4s.Method,
    // but are imported automatically as part of the DSL.
    case GET -> Root / "hello" => Ok("hello")
  }

  /** Matching Paths
    *
    * A request to the root of the service is matched with the `Root` extractor.
    * `Root` consumes the leading slash of the path info. The following matches
    * requests to `GET /`:
    */
  val rootRoute = HttpRoutes.of[IO] {
    case GET -> Root => Ok("root")
  }

  /** We usually match paths in a left-associative manner with Root and /. Each
    * "/" after the initial slash delimits a path segment, and is represented in
    * the DSL with the '/' extractor. Segments can be matched as literals or
    * made available through standard Scala pattern matching. For example, the
    * following service responds with "Hello, Alice!" to GET /hello/Alice:
    */
  val nameRoute = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name!")
  }

  /** The above assumes only one path segment after "hello", and would not match
    * GET /hello/Alice/Bob. To match to an arbitrary depth, we need a
    * right-associative /: extractor. In this case, there is no Root, and the
    * final pattern is a Path of the remaining segments. This would say "Hello,
    * Alice and Bob!"
    */
  val arbitraryNameRightAssocativeRoute = HttpRoutes.of[IO] {
    case GET -> "hello" /: rest =>
      Ok(s"""Hello, ${rest.segments.mkString(" and ")}""")
  }

  /** To match a file extension on a segment, use the ~ extractor: */
  val fileRoute = HttpRoutes.of[IO] {
    case GET -> Root / file ~ "json" =>
      Ok(s"""{"response": "You asked for $file"}""")
  }

/** Handling Path Parameters
  *
  * Path params can be extracted and converted to a specifc type but are
  * `String`s by default. There are numeric extractors provided in the form of
  * IntVar and LongVar, as well as UUIDVar extractor for `java.util.UUID`.
  *
  * If you want to extract a variable of type `T`, you can provide a custom
  * extractor object which implements `def unapply(str: string): Option[T]`,
  * similar to the way in which IntVar does it
  */
@main def handlingPathParameters() =
  def getUserName(userId: Int): IO[String] = IO.pure(userId.toString)

  val usersService = HttpRoutes.of[IO] {
    case GET -> Root / "users" / IntVar(userId) => Ok(getUserName(userId))
  }

  object LocalDateVar:
    def unapply(str: String): Option[LocalDate] =
      if (!str.isEmpty)
        println(s"parsing $str")
        Try(LocalDate.parse(str)).toOption
      else
        None

  def getTemperatureForecast(date: LocalDate): IO[Double] = IO(-10.00)

  val dailyWeatherService = HttpRoutes.of[IO] {
    case GET -> Root / "weather" / "temperature" / LocalDateVar(localDate) =>
      Ok(getTemperatureForecast(
        localDate
      ).map(s"The temperature on $localDate will be: " + _))
  }

  val request = Request[IO](Method.GET, uri"/weather/temperature/2016-11-05")
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  dailyWeatherService.orNotFound(request).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 44),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@3cbb78a8
  // )

import org.http4s.dsl.impl.MatrixVar

/** Handling Matrix Path Parameters
  *
  * Matrix path parameters can be extracted using `MatrixVar`.
  *
  * In following example, we extract the `first` and `last` matrix path
  * parameters. By default, matrix path parameters are extracted as Strings
  *
  * Like standard path parameters, matrix path parameters can be extracted as
  * numeric types using `IntVar` or `LongVar`.
  */
@main def handlingMatrixPathParameters() =
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  object FullNameExtractor
      extends MatrixVar("name", List("first", "last", "id"))

  val greetingService = HttpRoutes.of[IO] {
    case GET ->
        Root /
        "hello" / FullNameExtractor(first, last, IntVar(id)) / "greeting" =>
      Ok(s"Hello $first $last. Your User ID is $id.")
  }
  val res = greetingService
    .orNotFound(Request[IO](
      method = Method.GET,
      uri = uri"/hello/name;first=john;last=doe/greeting"
    )).unsafeRunSync()
  // Response[[A]IO[A]] = (
  //   `` = Status(code = 200),
  //   `` = HttpVersion(major = 1, minor = 1),
  //   `` = Headers(Content-Type: text/plain; charset=UTF-8, Content-Length: 37),
  //   `` = Stream(..),
  //   `` = org.typelevel.vault.Vault@1f6613b2
  // )

import java.time.{Year, Instant}
import java.time.format.DateTimeFormatter

/** Handing Query Parameters
  *
  * A query parameter needs to have a `QueryParamDecoderMatcher` provided to
  * extract it. In order for the `QueryParamDecoderMatcher` to work there needs
  * to be an implicit `QueryParamDecoder[T]` in scope. QueryParamDecoders for
  * simple types can be found in the QueryParamDecoder object. There are also
  * QueryParamDecoderMatchers available which can be used to return optional or
  * validated parameter values.
  *
  * In the example below we're finding query params named `country` and `year`
  * and then parsing them as `String` and `java.time.Year`.
  *
  * Missing Required Query Parameters
  *
  * A request with a missing required query parameter will fall through to the
  * case statements and may eventually return a 404. To provide contextual error
  * handling, optional query parameters or fallback routes can be used.
  */
@main def handlingQueryParameters() =
  given runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  object CountryQueryParamMatcher
      extends QueryParamDecoderMatcher[String]("country")

  given yearQueryParamDecoder: QueryParamDecoder[Year] =
    QueryParamDecoder[Int].map(Year.of)
  // QueryParamDecoder[Year] = org.http4s.QueryParamDecoder$$anon$7@6c3ee458

  object YearQueryParamMatcher extends QueryParamDecoderMatcher[Year]("year")

  def getAverageTemperatureForCountryAndYear(
      country: String,
      year: Year
  ): IO[Double] = IO.pure(32.00)

  val averageTemperatureService = HttpRoutes.of[IO] {
    case GET -> Root / "weather" / "temperature" :?
        CountryQueryParamMatcher(country) +& YearQueryParamMatcher(year) =>
      Ok(getAverageTemperatureForCountryAndYear(
        country,
        year
      ).map(s"Average temperatur for $country in $year was " + _))
  }

  /** To support a QueryParamDecoderMatcher[Instant], consider
    * QueryParamCodec#instantQueryParamCodec. That outputs a
    * QueryParamCodec[Instant], which offers both a QueryParamEncoder[Instant]
    * and QueryParamDecoder[Instant].
    */
  given isoInstantCodec: QueryParamDecoder[Instant] =
    QueryParamCodec.instantQueryParamCodec(DateTimeFormatter.ISO_INSTANT)

  object IsoInstantParamMatcher
      extends QueryParamDecoderMatcher[Instant]("timestamp")

  /** Flag Query Parameters
    *
    * To handle query parameters that do not contain any values but are simple
    * flags the FlagQueryParamMatcher should be used.
    *
    * Please note that you have to use the same syntax as with the other query
    * parameters (i.e. MyFlag(value)) in the routes definition.
    */
  object WithClouds extends FlagQueryParamMatcher("with-clouds")

  val serviceWithFlags = HttpRoutes.of[IO] {
    case GET -> Root / "weather" / "map" :? WithClouds(clouds) =>
      if (clouds)
        Ok("Showing clouds on the weather map.")
      else
        Ok("Showing no clouds on the weather map")
  }

  /** Optional Query Parameters
    *
    * To accept an optional query parameter a `OptionalQueryParamDecoderMatcher`
    * can be used
    */
  object OptionalYearQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[Year]("year")

  def getAverageTemperatureForCurrentYear: IO[String] = IO.pure("27.00")
  def getAverageTemperatureForYear(y: Year): IO[String] = IO.pure("27.00")

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "temperature" :?
        OptionalYearQueryParamMatcher(maybeYear) =>
      maybeYear match
        case None =>
          Ok(getAverageTemperatureForCurrentYear)
        case Some(year) =>
          Ok(getAverageTemperatureForYear(year))
  }

  /** Optional Multiple Query Parameters
    *
    * To accept multiple query parameters that are also optional, a
    * `OptionalMultipleQueryParamDecoderMatcher` can be used
    */
  object OptionalMultiColorQueryParam
      extends OptionalMultiQueryParamDecoderMatcher[String]("maybeColors")

  def getProductsOfMaybeColors(maybeColors: List[String]): IO[String] =
    IO.pure("purple")

  val routes2 = HttpRoutes.of[IO] {
    case GET -> Root / "products" :?
        OptionalMultiColorQueryParam(maybeColors) =>

      maybeColors match
        case Invalid(e) => BadRequest(
            s"Parse Error(s): ${e.toList.map(_.message).mkString(",")}"
          )
        case Valid(a) => Ok(getProductsOfMaybeColors(a))
  }

/** Invalid Query Parameter Handling
  *
  * To validate query parsing you can use `ValidatingQueryParamDecoderMatcher`
  * which returns a `ParseFailure` if param can't be decoded. Be careful not to
  * return the raw invalid value in a `BadRequest` because it could be used for
  * `Cross Site Scripting` attacks
  */
object InvalidQueryParamHandling:
  given yearQueryParamDecoder: QueryParamDecoder[Year] =
    QueryParamDecoder[Int]
      .emap(i =>
        Try(Year.of(i))
          .toEither
          .leftMap(t => ParseFailure(t.getMessage, t.getMessage))
      )
  // yearQueryParamDecoder: QueryParamDecoder[Year] = org.http4s.QueryParamDecoder$$anon$9@5219726a

  object YearQueryParamMatcher
      extends ValidatingQueryParamDecoderMatcher[Year]("year")

  def getAverageTemperatureForYear(year: Year): IO[String] = IO.pure("32.00")

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "temperature" :? YearQueryParamMatcher(yearValidated) =>
      yearValidated.fold(
        parseFailures => BadRequest("unable to parse argument year"),
        year => Ok(getAverageTemperatureForYear(year))
      )
  }

  /** Optional Invalid Query Parameter Handling
    *
    * Consider `OptionalValidatingQueryParamDecoderMatcher[A]` given the power
    * that `Option[cats.data.ValidatedNel[org.http4s.ParseFailure, A]]`
    * provides.
    */
  object LongParamMatcher
      extends OptionalValidatingQueryParamDecoderMatcher[Long]("long")

  val routes2 = HttpRoutes.of[IO] {
    case GET -> Root / "number" :? LongParamMatcher(maybeNumber) =>
      maybeNumber match
        case None    => BadRequest("missing number")
        case Some(n) => n.fold(
            parseFailures => BadRequest("unable to parse argument 'long'"),
            year => Ok(n.toString)
          )
  }
