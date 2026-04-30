package learning.effect.concepts

import cats.effect.IO
import cats.effect.IOApp
import scala.concurrent.duration._

trait Fiber[F[_], A]:
  def cancel: F[Unit]
  def join: F[A]

/** `Fibers`
  * -------------------------------------------------------------------------------------
  * Fibers are the fundamental abstraction in Cats Effect. They are lightweight
  * `threads` (often referred to as "green threads" or "coroutines"). Much like
  * threads, they represent a sequence of actions which will ultimately be
  * evaluated in that order by the underlying hardware. Fibers diverge from
  * threads in their footprint and level of abstraction
  *
  * `Fibers` are very lightweight. The Cats Effect `IO` runtime implements
  * fibers in roughly (150 bytes per fiber!), meaning that you can literally
  * create tens of millions of fibers within the same process without a problem,
  * and your primary limiting factor will simply be memory.
  *
  * As an example, any client/server application defined using Cats Effect will
  * create a new fiber for each inbound request, much like how a Django server
  * will create a new Thread for each request (except it is both safe and fast
  * to do this with fibers!).
  *
  * Because they are so lightweight, the act of creating and starting a new
  * fiber is extremely fast in and of itself, making it possible to create very
  * short-lived, "one-off" fibers whenever it is convenient to do so. Many of
  * the functions within Cats Effect are implemented in terms of fibers under
  * the surface, even ones which don't involve parallelism (such as `memoize`).
  *
  * This property alone would be sufficient to make fibers a useful tool, but
  * Cats Effect takes this concept even further!
  *
  * All fibers have first-class support for:
  *   - asynchronous callbacks
  *   - resource handling, and
  *   - cancelation (interruption)
  *
  * The asynchronous support in particular has profound effects, since it means
  * that any individual "step" of a fiber (much like a statement in a thread)
  * may be either synchronous in that it runs until it produces a value or
  * errors, or asynchronous in that it registers a callback which may be
  * externally invoked at some later point, and there is no fundamental
  * difference between these steps: they're just part of the fiber.
  *
  * This means that it is just as easy to define business logic which weaves
  * through asynchronous, callback-oriented actions as it is to define the same
  * logic in terms of classically blocking control flows.
  *
  * with fibers, there is no difference between a `callback` and a `return`. **
  *
  * Each `step in a thread is a statement`, and those statements are defined in
  * sequence by writing them in a particular order within a text file, combined
  * together using the semicolon (;) operator. Each
  * `step in a fiber is an effect`, and those effects are defined in sequence by
  * explicitly composing them using the flatMap function.
  *
  * Since `flatMap` is just a method like any other, rather than magic syntax
  * such as ;, it is possible to build convenience syntax and higher-level
  * abstractions which encode common patterns for composing effects.
  *
  * Every application has a "main fiber". This is very similar to the notion of
  * a "main thread" in that it is the point at which control flow starts within
  * the process. Conventionally in Cats Effect, this main fiber is defined using
  * IOApp and in particular by the effect returned by the run method:
  *
  * `object Hi extends IOApp.Simple { val run = IO.println("Hello") >>
  * IO.println("World") }`
  *
  * When one fiber starts another fiber, we generally say that the first fiber
  * is the "parent" of the second one. This relationship is not directly
  * hierarchical in that the parent can terminate before the child without
  * causing any inconsistencies.
  *
  * Fibers may always observe and recover from errors. (using something like
  * handleErrorWith or attempt). Fibers may also observe their own cancellation,
  * but they can't recover from it. Parent fibers may initiate cancelation in a
  * child (via the `cancel` method), and can observe the final outcome of that
  * child (which may be `Canceled`) and may continue executing after the child
  * has terminated.
  *
  * Cancelation
  *
  * By default, fibers are cancelable at all points during their execution. This
  * means that unneeded calculations can be promptly terminated and their
  * resources gracefully released as early as possible within an application.
  *
  * In practice, fiber cancelation most often happens in response to one of two
  * situations: timeouts and concurrent errors. i.e. fibersAreCancellable()
  *
  * This is very similar in concept to the Thread#interrupt method in the Java
  * standard library. Despite the similarity, there are some important
  * differences which make cancelation considerably more robust, more reliable,
  * and much safer.
  *
  * 1) Fibers are cooperative:
  *
  * When one fiber calls `cancel` on another fiber, it is effectively a request
  * to the target fiber. If target fiber is unable to cancel at that moment for
  * any reason, the canceling fiber async waits for cancelation to become
  * possible. Once cancelation starts, the target fiber will run all of its
  * finalizers (usually to release resources) before yielding control back to
  * the canceler. `interrupt`, on the other hand, always returns immediately
  * even if target Thread has not actually interrupted.
  *
  * 2) Fiber cancelation can be suppressed within scoped regions
  *
  * If a fiber is performing a series of actions which must be executed
  * atomically (either all actions execute, or none of them do), it can use the
  * `IO.uncancelable` method to mask the cancelation signal within the scope,
  * ensuring that cancelation is deferred until the fiber has completed its
  * critical section.
  *
  * This is commonly used in conjunction with compound resource acquisition,
  * where a scarce resource might leak if the fiber were to be canceled "in the
  * middle". This differs considerably from Thread#interrupt, which cannot be
  * suppressed.
  *
  * 3) Granularity of cancelation within target fiber!
  *
  * Finally, due to the fact that the fiber model offers much more control and
  * tighter guarantees around cancelation, it is possible and safe to
  * dramatically increase the granularity of cancelation within the target
  * fiber. In particular, every step of a fiber contains a cancelation check.
  * This is similar to what `interrupt` would do if the JVM checked the
  * interruption flag on every ;
  *
  * This is exactly how the loop fiber in the `fibersAreCancellable` example
  * below is canceled despite the fact that the loop never calls a blocking
  * `sleep` (or similar): each `IO.println` and the recursive `>> loop` is a
  * separate step in the `IO` program, so the runtime can stop between
  * iterations. Anyone who has ever attempted to use `Thread#interrupt` on a
  * `while` loop without inserting manual flag checks understands how important
  * this distinction is.
  *
  * Canceling that kind of fiber in Cats Effect is common.
  *
  * A Fiber carries an `F` action to execute (typically an `IO` instance).
  * Fibers are like light threads, meaning they can be used in a similar way to
  * threads to create concurrent code. However they are NOT threads! Spawning
  * new fibers does not guarantee that the action described in the `F`
  * associated to it will be run if there is a shortage of threads. Internally,
  * Cats-Effect uses thread pools to run fibers when running on the JVM, so if
  * there is no thread available in the pool then the fiber execution will
  * 'wait' until some thread is free again.
  *
  * On the other hand when the execution of some fiber is blocked e.g. because
  * it must wait for a semaphore to be released, the thread running the fiber is
  * recycled by cats-effect so it is available for other fibers. When the fiber
  * can be resumed cats-effect will look for some free thread to continue the
  * execution. The term "fiber blocking" is used sometimes to denote that
  * blocking the fiber does not involve halting any thread. CE also recycles
  * threads of finished and canceled fibers. But, keep in mind that, in
  * contrast, if the fiber is truly blocked by some external action like waiting
  * for some input from a TCP socket, then CE has no way to recover back that
  * thread until the action finishes. Such calls should be warpped by
  * `IO.blocking` to signal that the wrapped code will block the thread.
  * Cats-effect uses that info as a hint to optimize `IO` scheduling.
  *
  * Another difference with threads is that fibers are very cheap entities. We
  * can spawn millions of them at ease without impacting the performance.
  *
  * A worthy note is that you do not have to explicitly shut down fibers. If you
  * spawn a fiber and it finishes actively running its IO it will get cleaned up
  * by the garbage collector unless there is some other active memory reference
  * to it. So basically you can treat a fiber as any other regular object,
  * except that when the fiber is running (present tense), the cats-effect
  * runtime itself keeps the fiber alive.
  *
  * This has some interesting implications as well. Like if you create an
  * IO.async node and register the callback with something, and you're in a
  * fiber which has no strong object references anywhere else (i.e. you did some
  * sort of fire-and-forget thing), then the callback itself is the only strong
  * reference to the fiber. Meaning if the registration fails or the system you
  * registered with throws it away, the fiber will just gracefully disappear.
  *
  * As with threads, often you will need to coordinate the work of concurrent
  * fibers. Writing concurrent code is a difficult exercise, but cats-effect
  * implements some concurrency primitives such as Deferred, Ref, Semaphore...
  * that will help you in that task. Way more detailed info about concurrency in
  * cats-effect can be found in this other tutorial 'Concurrency in Scala with
  * Cats-Effect'.
  *
  * Extra Refernces:
  *
  * 1) Concurrency in Scala with Cats-Effect:
  * https://github.com/slouc/concurrency-in-scala-with-ce
  *
  * 2) Deferred/Ref/Semphaore in https://typelevel.org/cats-effect/docs/std/ref
  * https://typelevel.org/cats-effect/docs/std/deferred
  * https://typelevel.org/cats-effect/docs/std/semaphore
  */
object LearningFibers extends IOApp.Simple:
  def run: IO[Unit] =
    stepInAFiberIsAnEffect() >> IO(println("*************")) >>
      anotherFiberExample().flatMap(x => IO.println(s"outcome: $x"))
  /*
    All of below are fibers; rather, they are definitions of part of a fiber,
    much like a pair of statements defines part of a thread. IO.println
    itself is defined in terms of flatMap and other operations, meaning that it
    too is part of a fiber. Each step of a fiber is an effect, and composing
    steps together produces a larger effect, which can in turn continue to be
    composed.
   */
  def stepInAFiberIsAnEffect() =
    /*
    infix form (allowed when the method has one param and one argument)
      a.method(b)         == a method b
      a.flatMap(f)        == a flatMap f
     */
    val res: IO[Unit] = IO.println("Hi") flatMap { _ => IO.println("John") }
    // val fiber: IO[Fiber[IO, Throwable, Unit]] = res.start

    /* quite common to use for-comprehensions to express the same thing: */
    val res2: IO[Unit] =
      for
        _ <- IO.println("Hello")
        _ <- IO.println("Daisy")
      yield ()

    /*
    pattern where we put together two effects, ignoring the result of the
    first one, is common enough to merit its own operator: >>

        `a >> b  ≈  a.flatMap(_ => b)`

    So it means:
      Run `a` to completion.
      Throw away the value `a` produced (_).
      Then run `b` (which does not depend on that value).

    Ignoring the result of the first one” means: we don’t use that ()
    (or any other value) to build the second action. We only care that `a`
    finished, then we run b.

    This operator is nearly synonymous with *>, which is also commonly
    used and has the same meaning.
     */
    val res3: IO[Unit] = IO.println("Hello") >> IO.println("World")
    res >> res2 >> res3 >> IO.println("done")
    /* Prints the following
    --------------------
        Hi
        John
        Hello
        Daisy
        Hello
        World
        done
     */

    /** Constructs an IO which starts a fiber. This fiber prints "Yooo!"
      * infinitely to standard out. Left to its own devices, the loop fiber will
      * run forever. However, the timeout function delays for 3 seconds, after
      * which it calls cancel on the fiber, interrupting its execution and
      * freeing any resources it currently holds (in this case, none).
      */
  def fibersAreCancellable() =
    lazy val loop: IO[Unit] = IO.println("Yooo!") >> loop
    loop.timeout(0.5.seconds) // => IO[Unit]

  def anotherFiberExample() =
    val launchMissiles: IO[Unit] = IO.raiseError(new Exception("boom!"))
    val runToBunker = IO(println("To the bunker!!!"))

    for
      fiber <- launchMissiles.start
      // Retreat failed, cancel launch (maybe we should have retreated to our bunker before the launch?)
      _ <- runToBunker.handleErrorWith(error =>
        fiber.cancel >> IO.raiseError(error)
      )
      aftermath <- fiber.join
    yield aftermath
