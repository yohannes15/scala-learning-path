package learning.effect.datatypes

import cats.effect.{Fiber, IO, Resource, Concurrent, IOApp}

/** Motivation
  *
  * `Spawn` provides multiple ways to spawn a fiber to run an action:
  *
  * `Spawn[F]#start`: start and forget, no lifecycle management for the spawned fiber
  *
  * `Spawn[F]#background`: ties the lifecycle of the spawned fiber to that of the fiber
  * that invoked background
  *
  * The following diagrams illustrate the lifecycle of the spawned fiber in both cases. I
  * n each example, some fiber A is spawning another fiber B. Each box represents the
  * lifecycle of a fiber. If a box is enclosed within another box, it means that the lifecycle
  * of the former is confined within the lifecycle of the latter. In other words, if an
  * outer fiber terminates, the inner fibers are guaranteed to be terminated as well.
  *
  * Spawn[F]#start:
  *
  * Fiber A lifecycle
  * +---------------------+
  * |                 |   |
  * +-----------------|---+
  *                   |
  *                   |A starts B
  * Fiber B lifecycle |
  * +-----------------|---+
  * |                 +   |
  * +---------------------+
  *
  * Spawn[F]#background:
  *
  * Fiber A lifecycle
  * +------------------------+
  * |                    |   |
  * | Fiber B lifecycle  |A starts B
  * | +------------------|-+ |
  * | |                  | | |
  * | +--------------------+ |
  * +------------------------+
  *
  * Supervisor
  *
  * A supervisor spawns fibers whose lifecycles are bound to that of the
  * supervisor. If you need to run an action in a fiber in a "start-and-forget"
  * manner, you'll want to use Supervisor. This lets you safely evaluate an
  * effect in the background without waiting for it to complete and ensuring
  * that the fiber and all its resources are cleaned up at the end. You can
  * configure a `Supervisor` to wait for all supervised fibers to complete at
  * the end its lifecycle, or to simply cancel any remaining active fibers.
  *
  * Any fibers created via the supervisor will be finalized when the supervisor
  * itself is finalized via Resource#use
  *
  * The lifecycle of fibers spawned with Supervisor can be illustrated in the same
  * style as above:
  *
  * Supervisor lifecycle
  * +---------------------+
  * | Fiber B lifecycle   |
  * | +-----------------+ |
  * | |               + | |
  * | +---------------|-+ |
  * +-----------------|---+
  *                   |
  *                   | A starts B
  * Fiber A lifecycle |
  * +-----------------|---+
  * |                 |   |
  * +---------------------+
  */
object SupervisorImpl:
  trait Supervisor[F[_]]:
    def supervise[A](fa: F[A]): F[Fiber[F, Throwable, A]]

  object Supervisor:
    def apply[F[_]](await: Boolean)(using
        F: Concurrent[F]
    ): Resource[F, Supervisor[F]] = ???

/** There are two finalization strategies according to the await parameter of
  * the constructor:
  *
  *   - `true` -> wait for the completion of the active fibers
  *   - `false` -> cancel the active fibers
  *
  * NOTE: if an effect that never completes is supervised by a `Supervisor` with
  * the awaiting termintation policy, the termination of the `Supervisor` is
  * indefinitely suspended:
  */
import cats.effect.std.Supervisor

object Example:
  val a = Supervisor[IO](await = true).use { supervisor =>
    supervisor.supervise(IO.never).void
  }

// Cats Effect recipes use a simplified HTTP model; these are not real framework types.
case class Request(path: String, params: Map[String, String])

sealed trait Response
final case class Ok(body: String) extends Response
case object NotFound extends Response

def longRunningTask(params: Map[String, String]): IO[Unit] =
  IO.unit

final case class HttpServer(handler: Request => IO[Response]):
  def resource: Resource[IO, Unit] =
    Resource.eval(IO.never)

/** Start a supervised task that outlives the creating scope.
  *
  * If you need to run an action in a fiber in a "start-and-forget" manner,
  * you'll want to use Supervisor. This lets you safely evaluate an effect in
  * the background without waiting for it to complete and ensuring that the
  * fiber and all its resources are cleaned up at the end. You can configure
  * aSupervisor to wait for all supervised fibers to complete at the end its
  * lifecycle, or to simply cancel any remaining active fibers.
  *
  * Here is a very simple example of Supervisor telling a joke. In this example,
  * longRunningTask is started in the background. The server returns to the
  * client without waiting for the task to finish.
  */
object Server extends IOApp.Simple:
  def handler(supervisor: Supervisor[IO]): Request => IO[Response] =
    case Request("start", params) =>
      supervisor.supervise(longRunningTask(params)).void >>
        IO.pure(Ok("started"))
    case Request(_, _) => IO.pure(NotFound)

  val run =
    Supervisor[IO](await = true).flatMap { supervisor =>
      HttpServer(handler(supervisor)).resource
    }.useForever
