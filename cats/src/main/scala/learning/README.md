# [Cats](https://typelevel.org/cats/) — tutorial layout

This tree holds **topic-based modules** for learning Cats. 

- Cats is a library which provides abstractions for functional programming in the Scala programming language.
- Supports both object-oriented and functional programming
- Provides FP abstractions that are core, binary compatible, modular, approachable and
efficient
- Available for JVM, Scala Native and Scala.js
- Relies on improved type inferences via the fix for SI-2712. For Scala <= 2.12, you need
to add `scalacOptions += "-Ypartial-unification"` to your build.sbt. Partial unification
is on by default since Scala 2.13.

## How to install

```sbt
libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
```

## Installation Options

- `cats-kernel`: Small set of basic type classes (required).
- `cats-core`: Most core type classes and functionality (required).
- `cats-laws`: Laws for testing type class instances.
- `cats-free`: Free structures such as the free monad, and supporting type classes.
- `cats-testkit`: lib for writing tests for type class instances using laws.
- `algebra`: Type classes to represent algebraic structures.
- `alleycats-core`: Cats instances and classes which are not lawful.
- `cats-effect`: standard IO type together with Sync, Async and Effect type classes
- `cats-mtl`: transformer typeclasses for Cats' Monads, Applicatives and Functors.
- `mouse`: a small companion to Cats that provides convenient syntax (aka extension methods)
- `kittens`: automatic type class instance derivation for Cats and generic utility functions
- `cats-tagless`: Utilities for tagless final encoded algebras
- `cats-collections`: Data structures which facilitate pure functional programming
- `cats-testkit-scalatest`: Cats testkit integration with Scalatest

## What's covered

| Directory                               | Purpose                                                   |
| --------------------------------------- | --------------------------------------------------------- |
| `[typeclasses/](typeclasses/README.md)` | Typeclasses: Functor, Applicative, Monad, etc.            |

