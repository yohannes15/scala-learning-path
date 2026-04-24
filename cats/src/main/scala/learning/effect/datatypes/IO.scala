package learning.effect.datatypes

import cats.effect.IOApp
import cats.effect.IO

/** IO
 * ------------------
 * A data type for encoding side effects as pure values. IO is a pure abstraction representing 
 * the intention to perform a side effect, where the result of that side effect may be obtained
 * synchronously (via return) or asynchronously (via callback). 
 * 
 * IO values are pure, immutable values and thus preserve referential transparency, being usable
 * in functional programming. An IO is a data structure that represents just a description of a
 * side effectful computation.
 * 
 * A value of type IO[A] is a computation which, when evaluated, can perform effects before 
 * returning a value of type A.
 * 
 * IO can describe synchronous or asynchronous computations that:
  
    1. on evaluation yield exactly one result
    2. can end in either success or failure and in case of failure flatMap chains get short-circuited 
       (IO implementing the algebra of MonadError)
    3. can be canceled, but note this capability relies on the user to provide cancellation logic
  
 * Effects described via this abstraction are not evaluated until the "end of the world", which 
 * is to say, when the runtime executes the program (e.g. IOApp’s run, or the various unsafe* 
 * / *Sync / *Cancelable run paths depending on version). Effectful results are not
 * memoized, meaning that memory overhead is minimal (and no leaks), and also that a single effect 
 * may be run multiple times in a referentially-transparent manner.
*/

object LearningIO extends IOApp.Simple:
  def run: IO[Unit] =
    effectsMayBeRunMultipleTimes()

  /** prints "hey!" twice, as the effect re-runs each time it is sequenced in the monadic chain. 
   * The same doesn't work with Future, because the IO data type preserves referential 
   * transparency even when dealing with side effects and is lazily evaluated.
  */
  def effectsMayBeRunMultipleTimes() =
    val ioa = IO(println("Hey!"))
    for 
      _ <- ioa
      _ <- ioa
    yield ()

