package learning.effect.concepts

import cats.effect.{IO, IOApp, Outcome}
import cats.effect.implicits.* // Parallel[IO] for parTupled
import cats.syntax.all.*
import scala.concurrent.duration.*

/*
Concurrent generally refers to 2 or more actions which are defined to be independent
in their control flow. It is the opposite of "sequential", or rather, "sequential"
implies that something cannot be "concurrent". It is possible for things that are
"concurrent" to evaluate sequentially if the underlying runtime decides it is
optimal, whereas actions which are sequential will be always be evaluated one after
the other.

Concurrency is often conflated with asynchronous execution due to the fact that,
in practice, the implementation of concurrency often relies upon some mechanism for
async evaluation, but like we see in Asynchronous.scala, async says nothing about
concurrent vs sequential semantics. 

Cats effect has numerous mechanisms for defining concurrent effects. One of the most
straightforward of these is `parTupled`, which evaluates a pair of independent effects
and produces a tuple of their results:

  (callServiceA(params1), callServiceB(params2)).parTupled // IO[(Response, Response)]

As with all concurrency support, parTupled is a way of declaring to the underlying 
runtime that two effects (callServiceA(params1) and callServiceB(params2)) are 
independent and can be evaluated in parallel. 

**********************************************************************************
** Cats Effect will never assume that two effects can be evaluated in parallel. **
**********************************************************************************

All concurrency in Cats Effect is implemented in terms of underlying primitives which
create and manipulate fibers: 
  - `start` -> Start execution of the source suspended in the IO context.
  - `join` -> Awaits completion of fiber and returns its Outcome once it completes.

These concurrency primitives are very similar to the equivalently-named operations
on Thread, but as with most things in Cats Effect, they are considerably faster and
safer.

Structured Concurrency
----------------------
Structured concurrency is a form of control flow in which all concurrent operations
must form a closed hierarchy. Conceptually, it means that any operation which `forks`
some actions to run concurrently must forcibly ensure that those actions are completed
before moving forward. 

Furthermore, the results of a concurrent operation must:
  - ONLY be made available upon its completion and
  - ONLY to its parent in the hierarchy

`parTupled` is a good example. the IO[(Response, Response)] above is unavailable as
a result until BOTH service calls have completed and those responses are only accessible
within the resulting tuple. 

Cats Effect has large number of structured concurrency tools:
  - parTupled
  - parMapN
  - parTraverse

Additionally offers a number of robust structured concurrency operators such as:
  - background
  - Supervisor
  - Dispatcher

It has also fostered an ecosystem wherein structured concurrency is the rule rather 
than the exception, particularly with the help of higher-level frameworks such as 
Fs2 (https://fs2.io/#/). However, structured concurrency can be very limiting, and 
Cats Effect does not prevent unstructured concurrency when it is needed.

In particular, fibers may be `start`ed without the caller being forced to wait for
their completion. This low-level flexibility is necessary in some cases, but it is
also somewhat dangerous since it can result in fiber "leaks" (where a fiber is 
started and all references to it outside of the runtime are abandoned!). It is 
generally better to rely on structured (but very flexible) tools such as `background`
and `Supervisor`

Additionally, Cats Effect provides two general coordination tools for modeling shared
state:

  - `Ref` — one shared value that many fibers can read and update. Every read and write is
    an `IO` effect, so changes stay orderly without you managing locks by hand.

  - `Deferred` — a simple “pass the result when ready” handshake between fibers. One side
    runs an `IO` that waits (suspends) until the other side supplies a value — like saying
    “ping me when you have the answer” instead of checking in a loop. After a value has
    been supplied, that particular `Deferred` is finished; you create another if you need
    another round.

Both are flexible and easy to overuse in application code: wiring many of them together
can obscure your business flow compared with structured concurrency or clearer domain
types. They still earn their keep as the usual building blocks for `Queue`, `Semaphore`,
and similar primitives.

All of which is to say that Cats Effect encourages structured concurrency and provides
users with a large number of flexible tools for achieving it, but it does not prevent
unstructured concurrent compositions such as `start`, `Ref`, or `Deferred`.
*/

case class Response(value: String)

def callServiceA(paramA: String): IO[Response] =
  IO.sleep(150.millis) >> IO.pure(Response(paramA))

def callServiceB(paramB: String): IO[Response] =
  IO.sleep(150.millis) >> IO.pure(Response(paramB))

/**
  * [[https://typelevel.org/cats-effect/docs/concepts#concurrent]]).
  * Run: `sbt "cats/runMain learning.effect.concepts.LearningConcurrency"`.
  */
object LearningConcurrency extends IOApp.Simple:
  def run: IO[Unit] =
    independentEffectsSequentialThenParTupled() >>
      startAndJoin() >>
      refUpdate() >>
      deferredHandoff()

  /** Two *independent* effect-shaped values composed sequentially: total delay ≈ sum. */
  def independentEffectsSequentialThenParTupled(): IO[Unit] =
    val responseA: IO[Response] = callServiceA("paramA")
    val responseB: IO[Response] = callServiceB("paramB")
    val sequential: IO[(Response, Response)] = 
      responseA.flatMap(ra => responseB.map(rb => (ra, rb)))
    
    val par: IO[(Response, Response)] = (responseA, responseB).parTupled
    IO.println("1) Same two independent service IOs: sequential flatMap (≈300ms) then parTupled (≈150ms).") >>
      sequential.flatMap { case (x, y) => IO.println(s"   sequential: ($x, $y)") } >>
      par.flatMap { case (x, y) => IO.println(s"   parTupled:  ($x, $y)\n") }

  /** Under the hood, concurrency is still fibers: `start` then later `join`. */
  def startAndJoin(): IO[Unit] =
    val child = IO.println("   (child) running...") >> IO.sleep(250.millis) >> IO.pure(7)
    IO.println("2) start + join: parent does other work, then waits for the child outcome.") >>
      child.start.flatMap { fib =>
        IO.println("   (parent) doing a bit of work...") >> IO.sleep(100.millis) >> fib.join.flatMap {
          case Outcome.Succeeded(fa) =>
            fa.flatMap(n => IO.println(s"   (parent) child finished with $n\n"))
          case Outcome.Errored(e) => IO.raiseError(e)
          case Outcome.Canceled() => IO.println("   (parent) child was canceled\n")
        }
      }

  /** `Ref` is a mutable *cell* you only touch through `IO`: each `get`, `set`, and `update` is its
    * own effect, so several fibers can share one ref without the usual ad-hoc locking story.
    * Common pattern: one ref for a piece of shared state for the lifetime of a scope. Cats Effect
    * implements `Queue`, `Semaphore`, and friends on top of `Ref`. It is a low-level primitive—
    * application code that threads many refs can become hard to read—so prefer a higher-level
    * tool when it matches your use case.
    */
  def refUpdate(): IO[Unit] =
    IO.ref(0).flatMap { r => // initial value 0; in larger programs one ref is often shared across fibers in a scope
      IO.println("3) Ref: shared counter updated on this fiber.") >>
        r.update(_ + 1) >> r.get.flatMap(n => IO.println(s"   get: $n\n"))
    }

  /** `Deferred` is a handshake between fibers: one side calls `get` and its `IO` pauses there
    * until another side calls `complete` with a value—like leaving a note “call me when you’re
    * done” instead of asking over and over. Only the first successful `complete` wins; after
    * that, this `Deferred` is done (create a new one for another round).
    * 
    * `get` suspends the fiber without busy-waiting (it obtains the value of the Deferred, 
    * or waits until it has been completed. The returned value may be canceled.) 
    * 
    * `tryGet` checks once and moves on, often `None` until someone completes it. Like `Ref`,
    * it is a small generic building block; use clearer domain types when they communicate 
    * intent better.
    */
  def deferredHandoff(): IO[Unit] =
    IO.deferred[String].flatMap { gate =>
      // Order: worker.start runs the child (sleep >> gate.complete); main fiber suspends on gate.get until
      // complete(...) runs. fib.join then waits for the child fiber’s IO to finish—separate concern; 
      // here complete is the worker’s last step so get and join line up closely.
      val worker = IO.sleep(100.millis) >> gate.complete("from worker")
      IO.println("4) Deferred: main waits (fiber suspended on get) until the worker completes the gate.") >>
        worker.start.flatMap { fib =>
          gate.get.flatMap { msg =>
            IO.println(s"   main received: $msg") >> fib.join.void
          }
        }
    }
