package learning.effect.concepts

import java.util.concurrent.{Executors, TimeUnit}
import cats.effect.{IO, IOApp}

/**
  * Asynchronous is the opposite of "synchronous", and it pertains to a manner in 
  * which a given effect produces a value. Synchronous effects are defined using 
  * `apply` (also `delay`, `blocking`, `interruptible` or `interruptibleMany`) and
  * produce their results using `return`, or alternatively raise errors using `throw`. 
  * 
  * Asynchronous effects are defined using async (or async_) and produce their results
  * using a callback, where a successful result is wrapped in Right while an error is 
  * wrapped in Left.
  * 
  * Both the `Thread.sleep` and the `schedule` effects shown here have the same semantics.
  * They delay for 500 milliseconds before allowing the next step in the fiber to take place. 
  * Where they differ is the fashion in which they were defined.
  * 
  * Thread.sleep is synchronous while schedule is asynchronous. The implications of this
  * are surprisingly profound. Since Thread.sleep does not return JVM-level control flow
  * until after its delay expires, it effectively wastes a scarce resource (the underlying
  * kernel Thread) for its full duration, preventing other actions from utilizing that 
  * resource more efficiently in the interim. 
  * 
  * Conversely, `schedule` returns immediately when run and simply invokes the callback
  * in the future once the given time has elapsed. This means that the underlying kernel
  * Thread is not wasted and can be repurposed to evaluate other work in the interim.
  * 
  * Asynchronous effects are considerably more efficient than synchronous effects (whenever
  * they are applicable, such as for network I/O or timers), but they are generally considered
  * to be harder to work with in real applications due to the need to manually manage callbacks
  * and event listeners. 
  * 
  * Fibers entirely eliminate this disadvantage due to their built-in support for asynchronous
  * effects. In both of the below examples, the effect in question is simply a value of type 
  * IO[Unit], and from the outside, both effects behave identically. Thus, the difference 
  * between `return/throw` aka sync and a `callback` aka async is encapsulated entirely at 
  * the definition site, while the rest of your application control flow remains entirely 
  * oblivious. This is a large part of the power of fibers.
  * 
  * It is critical to note that nothing in the definition of "asynchronous" implies 
  * "parallel" or "simultaneous", nor does it negate the meaning of "sequential" (remember
  * all fibers are sequences of effects). "Asynchronous" simply means "produces values or
  * errors using a callback rather than return/throw". It is an implementation detail of an
  * effect, managed by a fiber, rather than a larger fundamental pattern to be designed around.
  */

object LearningAsync extends IOApp.Simple:
  def run: IO[Unit] =
    syncExample() >>
    asyncExample()

  def syncExample(): IO[Unit] =
    IO.println("🐌 Sync: two 1s naps in a row — the carrier thread is stuck snoring.") >>
    IO(Thread.sleep(1000)) // => IO[Unit]
      >> IO.interruptible(Thread.sleep(1000)) // => IO[Unit]
      >> IO.println("🐌 Sync: awake! Same thread was parked the whole ~2s.")

  def asyncExample(): IO[Unit] =
    val scheduler = Executors.newScheduledThreadPool(1)
    (
      IO.println("🚀 Async: handing the timer to the scheduler — thread is free instantly.") >>
        IO.async_[Unit] { cb =>
          scheduler.schedule(
            new Runnable { def run = cb(Right(())) },
            1000,
            TimeUnit.MILLISECONDS
          )
          ()
        } // => IO[Unit]
        >> IO.println("🚀 Async: ding! Callback ran later; the pool thread only worked ~0ms to register.")
    ).guarantee(IO.blocking {
      scheduler.shutdown()
      scheduler.awaitTermination(5, TimeUnit.SECONDS)
      ()
    })
    

  


