# [Cats Effect](https://typelevel.org/cats-effect/) — tutorial layout

This tree holds **topic-based modules** for learning Cats Effect in this repo.

- Fast: Cats Effect provides lightweight fibers for asynchronous, highly concurrent applications. Your code stays fast even under extreme load.
- Reliable: Keep your applications up and running under high resource contention with automatic resource safety, backpressure, and cancellation of unnecessary work.
- Ecosystem: Cats Effect powers a thriving ecosystem of streaming frameworks, database layers, HTTP servers, and much more!
- Community: Cats Effect is open source software maintained by the Typelevel community. We provide a friendly, safe, and welcoming environment for everyone. 

## How to install

```scala
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0"
```

## Asynchronous

The `IO` monad allows you to capture and control asynchronous, callback-driven effects behind a clean, synchronous interface. Although superficially similar to `Future`, `IO` takes this concept to the next level with a powerful API that leaves you fully in control of evaluation semantics and behavior. Write programs that seamlessly mix synchronous and asynchronous code without sacrificing code comprehension or composability.

## Concurrency

`IO` can power highly concurrent applications, like web services that must serve tens of thousands of requests per second. Concurrency in `IO` is facilitated by `fibers`, which are lightweight, interruptible threads that are managed completely by the runtime. Fibers are much cheaper than native OS threads, so your application can spawn tens of millions without breaking a sweat. Focus on high-level concurrency control without worrying about details like thread management or executor shifting.

## Tracing

IO collects runtime information as your program executes, making it super-easy to track down the origin of errors or introspect your program as it evaluates. Tracing can be enabled in production without any noticable impact on performance, which automatically unlocks powerful features like enhanced exceptions that make it easier to diagnose errors. Full instrumentation is also supported for developer environments when tracking down thorny issues, even through monad transformers or third-party libraries.

## Safety

Real-world applications must often deal with resources like network connections and file handles to serve requests. Resource management is an exceptionally difficult problem in concurrent applications; one slight bug could result in a memory leak that OOM-kills your service or even a deadlock that renders your service completely unresponsive. IO manages resource lifecycles for you and guarantees that resources are safely allocated and released even in the presence of exceptions and cancellations.

## Composable

Cats Effect embraces purely functional programming: IO represents a description of a program rather than a running computation, which gives you ultimate control over how and when effects are evaluated. Simple programs can be composed to form more complex programs, while retaining the ability to reason about the behavior and complexity. Refactor without fear!
