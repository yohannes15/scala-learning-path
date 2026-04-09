# Concurrency — `Future`

## Why this is useful

CPU-bound and I/O-bound work should not block your whole program. **Futures** (with a good **execution context**) give you a standard way to run work asynchronously and compose results without manually juggling raw `Thread`s. This folder collects notes toward that style.

When you want to write parallel and concurrent applications in Scala, you can use the native Java `Thread` — but the Scala `Future` offers a more high-level and idiomatic approach, so it’s preferred, and covered in this chapter.

## Future

**Scaladoc:** A Future represents a value which may or may not currently be available, but will be available at some point, or an exception if that value could not be made available.

To demonstrate what that means, let’s first look at single-threaded programming. In the single-threaded world you bind the result of a method call to a variable like this:

```scala
def aShortRunningTask(): Int = 42
val x = aShortRunningTask()
```

In this code, the value 42 is immediately bound to `x`. When you're working with a Future, the assignment process looks similar:

```scala
def aLongRunningTask(): Future[Int] = ???
val x = aLongRunningTask()
```

But the main difference in this case is that because `aLongRunningTask` takes an indeterminate amount of time to return, the value in `x` may or may not be currently available, and may be available in the future.

Another way to look at this is in terms of blocking. In this single-threaded example, the `println` statement isn’t printed until `aShortRunningTask` completes:

```scala
def aShortRunningTask(): Int =
  Thread.sleep(500)
  42

val x = aShortRunningTask()
println("Here")
```

On the other hand, if `aShortRunningTask` is created as a `Future`, the `println` statement is printed almost immediately because the task is spawned off on some other thread — it doesn't block.

When you think about futures, it’s important to know that they’re intended as a one-shot construct:

> Handle this relatively slow computation on some other thread, and call me back with a result when you’re done.

## This chapter covers

- How to use futures
- How to run multiple futures in parallel
- Combine results of futures in a `for` expression
- Examples of methods that are used to handle the value in a future once it returns

## Key Points

To summarize, a few key points about futures are:

- You construct futures to run tasks off of the main thread
- Futures are intended for one-shot, potentially long-running concurrent tasks that eventually return a value; they create a temporary pocket of concurrency
- A future starts running as soon as you construct it
- A benefit of futures over threads is that they work with for expressions, and come with a variety of callback methods that simplify the process of working with concurrent threads
- When you work with futures you don’t have to concern yourself with the low-level details of thread management
- You handle the result of a future with callback methods like `onComplete` and `andThen`, or transformation methods like `filter`, `map`, etc.
- The value inside a `Future` is always an instance of one of the `Try` types: `Success` or `Failure`
- If you’re using multiple futures to yield a single result, combine them in a for expression

Also, as you saw with the import statements in these examples, the Scala `Future` depends on an `ExecutionContext`.

For more details about futures, see [Futures and Promises](https://docs.scala-lang.org/overviews/core/futures.html), an article that discusses futures, promises, and execution contexts. It also provides a discussion of how a for expression is translated into a flatMap operation.
