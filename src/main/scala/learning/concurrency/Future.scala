package learning.concurrency

/* 
Future
---------------------------
- Represents a value which may or may not currently be available,
  but will be available at some point, or an exception if that value
  could  not be made available.

- Used to create a temporary pocket of concurrency. For example,
  when you need to call an algorithm that runs an indeterminate amount
  of time - such as calling a remote microservice - so you want to run
  it off of the main thread

- Value in a `Future` is always an instance of one of the `scala.util.Try`
    -> `Success`
    -> `Failure`

- Therefore when you work with the result of a future, you use the
  usual Try handling techniques
*/

/* 
Import `global` when you want to provide the global
 `ExecutionContext` implicitly to Future
*/
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

// example of long-running single-threaded algorithm
def longRunningAlgorithm() =
    Thread.sleep(2_000)
    42

def futureExample() = 
    val eventualInt = Future(longRunningAlgorithm())
    // rightaway, your computation - call to longRunningAlgorithm -
    // begins running. If you immediately check the value of the
    // variable `eventualInt`, you see future isn't completed yet
    // eventualInt: scala.concurrent.Future[Int] = Future(<not completed>)
    println(s"type of future `eventualInt` is $eventualInt. Sleeping 2 seconds")
    Thread.sleep(2_300)
    // eventualInt: scala.concurrent.Future[Int] = Future(Success(42))
    println(s"type of eventualInt is $eventualInt. It has completed")

/*
Future methods
----------------------------
The Future class has many methods you can use. It has some methods that you
find on Scala collections classes, including:
    - filter
    - flatMap
    - map
Its callback methods are:
    - onComplete
    - andThen
    - foreach
Other transformation methods include:
    - fallbackTo
    - recover
    - recoverWith
See the Futures and Promises page for a discussion of additional methods 
available to futures. https://docs.scala-lang.org/overviews/core/futures.html

Using `map` with futures
----------------------------
- use just like the map method on collections.
*/

def futureMapExample() = 
    val a = Future(longRunningAlgorithm()).map(_ * 2)
    // a: scala.concurrent.Future[Int] = Future(<not completed>)
    println(s"type of future `a` is $a. Sleeping 2 seconds")
    Thread.sleep(2_300)
    println(s"type of `a` is ${a}. It has completed")
    // a: scala.concurrent.Future[Int] = Future(Success(84))

/* 
Using callback methods with futures
--------------------------------------
In addition to higher-order functions like map, you can also use callback
methods with future. One commonly used callback method is `onComplete`, 
which takes a partial function in which you handle the `Success` and
`Failure` cases
*/

def futureCallbackExample() = 
    println("calling callback method oncomplete on longrunningalgorithm (2 secs)")
    Future(longRunningAlgorithm()).onComplete {
        case Success(value) => println(s"Got the callback, value = $value")
        case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(2_300)

/*
Blocking on a Future: `Await`
------------------------------
Use when you must wait for a Future on the current thread (scripts, tests,
or calling blocking APIs). In services, prefer `map` / `flatMap` / `for`
so you do not tie up threads.

- `Await.result(future, atMost)` — blocks up to `atMost`, then returns the
  computed value. If the Future failed, the exception is thrown here.
  If it is not done in time, throws `java.util.concurrent.TimeoutException`.

- `Await.ready(future, atMost)` — blocks until the Future completes or time
  runs out; returns the same `Future` (often you then use `.value` / `foreach`).

`atMost` is a `Duration`: e.g. `3.seconds`, `500.millis`, or `Duration.Inf`
(wait without a cap — use rarely).

Examples (see `futureAwaitExample` / `futureAwaitReadyExample` below):
- `Await.result`: `val n: Int = Await.result(Future(longRunningAlgorithm()), 5.seconds)`
- `Await.ready`: block until done, then read `Try` from `.value` (same Future reference):

      val f = Future(longRunningAlgorithm())
      val done = Await.ready(f, 5.seconds) // same instance as f
      done.value.get /* Option[Try[Int]] */ .get /* Success/Failure */

`Await` vs `onComplete`: both wait for the same completion, but you normally
pick one style. `Await.result` blocks and gives you the value (or throws).
`Await.ready` blocks and gives you a future (Success/Failure) and 
`onComplete` registers work that runs later without blocking the caller — inside
the callback you already have `Success` / `Failure`; there is nothing left to
`Await` for that step. 

Await is what is used to ensure proper handling of blocking for Awaitable 
instances. While occasionally useful, e.g. for testing, it is recommended 
that you avoid Await whenever possible— instead favoring combinators 
and/or callbacks. Await's result and ready methods will block the calling
thread's execution until they return, which will cause performance 
degradation, and possibly, deadlock issues.
*/

def futureAwaitExample(): Unit =
    val f = Future(longRunningAlgorithm())
    val n = Await.result(f, 5.seconds)
    println(s"Await.result returned: $n. Await.result throws exception if not finished in time")

def futureAwaitReadyExample(): Unit =
    val f = Future(longRunningAlgorithm())
    val done = Await.ready(f, 5.seconds)
    /* `ready` returns the same Future; when completed, `.value` is Some(Success/Failure) */
    done.value.foreach(
        _.fold(
            // Applies e if this is a Failure or v if this is a Success
            e => e.printStackTrace(),
            v => println(s"Await.ready: $v. Doesn't throw exception. Returns Futures (Success/Failure)")
        )
    )

/* 
Running multiple futures and joining their results
-----------------------------------------------------
- Use a `for` expression to run computations in parallel and join
  their results 

Steps

1) Start the computations that return Future results
2) Merge their results in a for expression (yield f1 + f2 + f3 as an example)
3) Extract the merged result using `onComplete` or similar technique

KEY: start the computations that return futures!, 
     and then join them in the for expression!

In the example when you run the app, you see this output

creating the futures:   0
before the 'sleep(3000)': 25
in the `yield: 836
in the success case 837
result = 6

As that output shows, the futures are created very rapidly, and the 
print statement right before the sleep(3000) statement at the end of the
method is reached. All of that code is run on the JVM’s main thread.

Then, at 836/7 ms, the three futures complete and the code in the yield
block is run. Then the code immediately goes to the Success case in 
the onComplete method.

The 836 ms output is a key to seeing that the three computations are
run in parallel. If they were run sequentially, the total time would be 
about 1,400 ms — the sum of the sleep times of the three computations.
But because they’re run in parallel, the total time is just slightly 
longer than the longest-running computation: f1, which is 800 ms.
*/

def futureMultipleExample() = 
    val startTime = System.currentTimeMillis()
    def delta() = System.currentTimeMillis() - startTime
    def sleep(millis: Long) = Thread.sleep(millis)

    println(s"creating the futures: ${delta()}")

    // (1) start the computations that return futures
    val f1 = Future { sleep(800); 1}
    val f2 = Future { sleep(200); 2}
    val f3 = Future { sleep(400); 3}

    // (2) join the futures in a `for` expression
    val result: Future[Int] = 
        for
            r1 <- f1
            r2 <- f2
            r3 <- f3
        yield
            println(s"in the `yield: ${delta()}")
            r1 + r2 + r3

    // (3) process the result
    result.onComplete {
        case Success(value) => 
            println(s"in the success case ${{delta()}}")
            println(s"result = $value")
        case Failure(exception) => 
            exception.printStackTrace
    }

    println(s"before the 'sleep(3000)': ${delta()}")

    // important for a little parallel demo: keep the jvm alive
    sleep(3000)

/* IMPORTANT
Notice that if the computations were run within the for expression, 
they would be executed sequentially, not in parallel:

// Sequential execution (no parallelism!)
for
  r1 <- Future { sleep(800); 1 }
  r2 <- Future { sleep(200); 2 }
  r3 <- Future { sleep(400); 3 }
yield
  r1 + r2 + r3

So, if you want the computations to be possibly run in parallel, 
remember to run them outside the for expression.
*/

/* 
Method that returns a Future
--------------------------------------
So far you’ve seen how to pass a single-threaded algorithm into a 
Future constructor. You can use the same technique to create a method
that returns a Future.

As with the previous examples, just assign the result of the method 
call to a new variable. Then when you check the result right away 
you’ll see that it’s not Future(<not completed>), but after the delay
time the future will have a result of Future(Success(.))
*/

def slowlyDouble(x: Int, delay: Long): Future[Int] =
    Future {
        Thread.sleep(delay)
        x * 2
    }
