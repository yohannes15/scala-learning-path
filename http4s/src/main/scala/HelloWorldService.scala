import org.http4s.HttpRoutes
import cats.effect.IO
import org.http4s.dsl.io.*

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

/** Returning Content in the Response */
