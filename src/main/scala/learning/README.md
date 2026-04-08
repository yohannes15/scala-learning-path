# `learning` — tutorial layout

This tree holds **topic-based modules** for learning Scala 3. Each subdirectory groups related `.scala` files and (where noted) a **README** that explains *why* the topic matters and *what* the files cover. Use them in any order; `fundamentals` is the usual starting point.

| Directory | Purpose |
|-----------|---------|
| [`fundamentals/`](fundamentals/README.md) | Core language: syntax, control flow, methods, packages, FP basics, `@main`, string interpolation |
| [`types/`](types/README.md) | Type system: ADTs, generics, variance, unions/intersections, GADTs, opaque & structural types |
| [`functions/`](functions/README.md) | Functions as values: anonymous, HOFs, partial functions, eta expansion |
| [`collections/`](collections/README.md) | Immutable/mutable collections, operations, and usage patterns |
| [`domain/`](domain/README.md) | Modeling domains with traits, classes, enums — OOP and FP styles |
| [`contextualabstractions/`](contextualabstractions/README.md) | `given` / `using`, extensions, type classes, multiversal equality |
| [`concurrency/`](concurrency/README.md) | Concurrent programming with `Future` (notes and planned examples) |
| [`static/`](static/README.md) | SVG diagrams referenced from `docs/diagrams.md` (type hierarchy, collections) |

**Entry point:** `learning.fundamentals.hello` — see the comment at the top of [`fundamentals/Basics.scala`](fundamentals/Basics.scala) for how to run it with sbt.
