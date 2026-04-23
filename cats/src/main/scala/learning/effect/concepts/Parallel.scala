package learning.effect.concepts

import cats.effect.{IO, IOApp}
import cats.effect.implicits.* // Parallel[IO] for parTupled
import cats.syntax.all.*
import scala.concurrent.duration.*

/**
  * Much like asynchronous execution, parallelism is an implementation detail of the
  * runtime. When two things are evaluated in parallel, it means that the underlying
  * runtime and hardware are free to schedule the associated computations simultaneously
  * on the underlying processors. This concept is very related to that of `concurrency`
  * in that concurrency is how users of Cats Effect declare to the runtime that things
  * can be executed in parallel.
  * 
  * It is generally easier to understand the distinction between `concurrency` 
  * and `parallelism` by examining scenarios in which concurrent effects would not be 
  * evaluated in parallel.
  * 
  * Example 1: Application running on Javascript
  * --------------------------------------------------------------------------------------
  * One obvious scenario is when the application is running on JavaScript rather than on 
  * the JVM. Since JavaScript is a single-threaded language, it is impossible for anything
  * to evaluate in parallel, even when defined to be concurrent. Now, this doesn't mean 
  * that concurrency is useless on JavaScript, since it is still helpful for the runtime
  * to understand that it doesn't need to wait for A to finish before it executes B, but 
  * it does mean that everything will, on the underlying hardware, evaluate sequentially.
  * 
  * Example 2: Number Of Fibers > Number Of Underlying Threads Within The Runtime.
  * --------------------------------------------------------------------------------------
  * In general, Cats Effect's runtime attempts to keep the number of underlying threads
  * matched to the number of physical threads provided by the hardware, while the number
  * of fibers may grow into the tens of millions (or even higher depending on memory).
  * Since there are only a small number of actual carrier threads, the runtime will
  * schedule some of the concurrent fibers on the same underlying carrier thread, meaning
  * that those fibers will execute in series rather than in parallel.
  * 
  * FIBERS ARE NON-HOGGING
  * --------------------------------------------------------------------------------------
  * It is worth noting that fibers are prevented from "hogging" their carrier thread, even
  * when the underlying runtime only has a single thread of execution (such as JS).
  * Whenever a fiber sequences an asynchronous effect, it yields its thread to the next
  * fiber in the queue! Additionally, if a fiber has had a long series of sequential effects
  * without yielding, the runtime will detect the situation and insert an `artificial` yield
  * to ensure that other pending fibers have a chance to make progress. This is an important
  * `fairness` element of fibers

Run: `sbt "cats/runMain learning.effect.concepts.LearningParallel"`. 
*/
object LearningParallel extends IOApp.Simple:
  def run: IO[Unit] =
    timedSequentialVsConcurrentSleeps()

  /** Same two sleeps: sequential wall clock ≈ sum; `parTupled` ≈ max when the runtime overlaps them. */
  def timedSequentialVsConcurrentSleeps(): IO[Unit] =
    val nap = IO.sleep(350.millis)
    val sequential = nap >> nap
    val concurrent = (nap, nap).parTupled
    IO.println("Parallelism vs concurrency: you declare independence; wall clock shows overlap only when the runtime can.") >>
      sequential.timed.flatMap { case (dSeq, _) =>
        IO.println(s"  Sequential:  ${dSeq.toMillis} ms  (~700 ms expected)") >>
          concurrent.timed.flatMap { case (dPar, _) =>
            IO.println(
              s"  parTupled:   ${dPar.toMillis} ms  (~350 ms on JVM if both sleeps overlap—not guaranteed on a busy or single-thread host)"
            )
          }
      }
