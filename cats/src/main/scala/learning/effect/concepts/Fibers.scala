package learning.effect.concepts

import cats.effect.IO
import cats.effect.IOApp


/** `Fibers`
-------------------------------------------------------------------------------------
Fibers are the fundamental abstraction in Cats Effect. They are lightweight `threads` 
(often referred to as "green threads" or "coroutines"). Much like threads, they
represent a sequence of actions which will ultimately be evaluated in that order
by the underlying hardware. Fibers diverge from threads in their footprint and level
of abstraction

`Fibers` are very lightweight. The Cats Effect `IO` runtime implements fibers in 
roughly (150 bytes per fiber!), meaning that you can literally create tens of millions
of fibers within the same process without a problem, and your primary limiting factor
will simply be memory.

As an example, any client/server application defined using Cats Effect will create 
a new fiber for each inbound request, much like how a Django server will create a 
new Thread for each request (except it is both safe and fast to do this with fibers!).

Because they are so lightweight, the act of creating and starting a new fiber is 
extremely fast in and of itself, making it possible to create very short-lived, 
"one-off" fibers whenever it is convenient to do so. Many of the functions within
Cats Effect are implemented in terms of fibers under the surface, even ones which
don't involve parallelism (such as `memoize`).

This property alone would be sufficient to make fibers a useful tool, but Cats Effect
takes this concept even further!

All fibers have first-class support for:
  - asynchronous callbacks
  - resource handling, and 
  - cancelation (interruption) 

The asynchronous support in particular has profound effects, since it means that 
any individual "step" of a fiber (much like a statement in a thread) may be either
synchronous in that it runs until it produces a value or errors, or asynchronous 
in that it registers a callback which may be externally invoked at some later 
point, and there is no fundamental difference between these steps: they're just
part of the fiber. 

This means that it is just as easy to define business logic which weaves through 
asynchronous, callback-oriented actions as it is to define the same logic in terms
of classically blocking control flows.

******************************************************************************
** with fibers, there is no difference between a `callback` and a `return`. **
******************************************************************************

Each `step in a thread is a statement`, and those statements are defined in
sequence by writing them in a particular order within a text file, combined
together using the semicolon (;) operator. Each `step in a fiber is an effect`,
and those effects are defined in sequence by explicitly composing them using
the flatMap function. 

Since `flatMap` is just a method like any other, rather than magic syntax such
as ;, it is possible to build convenience syntax and higher-level abstractions
which encode common patterns for composing effects. 

Every application has a "main fiber". This is very similar to the notion of a
 "main thread" in that it is the point at which control flow starts within the
process. Conventionally in Cats Effect, this main fiber is defined using IOApp
and in particular by the effect returned by the run method:

  `object Hi extends IOApp.Simple {
    val run = IO.println("Hello") >> IO.println("World")
  }`
*/

object LearningFibers extends IOApp.Simple:
  def run: IO[Unit] =
    stepInAFiberIsAnEffect()

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
    val res: IO[Unit] = IO.println("Hi") flatMap { _ => IO.println("John")}

    /* quite common to use for-comprehensions to express the same thing: */
    val res2: IO[Unit] = for
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


@main def fibers() =
  LearningFibers.stepInAFiberIsAnEffect() 
