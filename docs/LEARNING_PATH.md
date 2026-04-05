# Learning path (through `FunctionalProgramming.scala`)

This document matches the order of topics wired from `hello` in [`Basics.scala`](../src/main/scala/learning/Basics.scala): run examples by uncommenting the calls you want, or invoking the individual `def` examples from the REPL / tests.

**Prerequisites:** JDK 17+, [sbt](https://www.scala-sbt.org/), Scala 3 (see `build.sbt`).

**Run the default entrypoint:**

```bash
sbt "run hello Scala"
```

**Diagrams:** see [`diagrams.md`](diagrams.md) for SVGs (type hierarchy, casting, collection hierarchies).

---

## Suggested order (as in `Basics.scala` → FP)

| Step | Module | File(s) | What to run / explore |
|------|--------|---------|------------------------|
| 1 | Basics | [`Basics.scala`](../src/main/scala/learning/Basics.scala) | `helloInteractive`, `matching`, `tryCatchFinally`, `whileLoop` |
| 2 | Domain modeling (ADT / traits) | [`domain/DomainModeling.scala`](../src/main/scala/learning/domain/DomainModeling.scala) | `exampleTraitClasses`, `sumTypeExample`, `caseClassExample`, enums, product types |
| 3 | OOP modeling | [`domain/OopModeling.scala`](../src/main/scala/learning/domain/OopModeling.scala), [`OopModelingExample.scala`](../src/main/scala/learning/domain/OopModelingExample.scala) | mixins, `SubjectObserver` example |
| 4 | FP-style domain | [`domain/FpModeling.scala`](../src/main/scala/learning/domain/FpModeling.scala) | `fpModelingExample` |
| 5 | Methods | [`Methods.scala`](../src/main/scala/learning/Methods.scala) | `testExtension`, `methodVisibility` |
| 6 | `@main` and CLI | [`MainMethods.scala`](../src/main/scala/learning/MainMethods.scala) | `testMainMethods`, `happyBirthday`, `run` (see file for CLI notes) |
| 7 | Functions | [`Functions.scala`](../src/main/scala/learning/Functions.scala) | anonymous functions, HOFs, `map`, methods returning functions |
| 8 | First-class functions | [`FirstClassFunctions.scala`](../src/main/scala/learning/FirstClassFunctions.scala) | `higherOrderFunc` |
| 9 | Singletons & modules | [`Singleton.scala`](../src/main/scala/learning/Singleton.scala) | companion objects, traits as modules |
| 10 | Collections (taste) | [`collections/CollectionsTasteOfScala.scala`](../src/main/scala/learning/collections/CollectionsTasteOfScala.scala) | `listCollection`, `lazyListExample`, folds, tuples |
| 11 | Collection types | [`collections/CollectionTypes.scala`](../src/main/scala/learning/collections/CollectionTypes.scala) | `vectorExample`, `arrayExample`, `arrayBufferExample`, `mapExample`, `setExample`, `rangeExample` |
| 12 | Collection methods | [`collections/CollectionMethods.scala`](../src/main/scala/learning/collections/CollectionMethods.scala) | `map` / `filter` / `reduce` / `head` / `tail` / … demos |
| 13 | Types | [`Types.scala`](../src/main/scala/learning/Types.scala) | `instancesOfAnyExample`, `typeCastExample`; use [`diagrams.md`](diagrams.md) for SVGs |
| 14 | String interpolation | [`StringInterpolation.scala`](../src/main/scala/learning/StringInterpolation.scala) | custom interpolators, extractors |
| 15 | Control structures | [`ControlStructures.scala`](../src/main/scala/learning/ControlStructures.scala) | `for` / `for`-yield, `match`, `try`/`catch` |
| 16 | Packages & imports | [`Packages.scala`](../src/main/scala/learning/Packages.scala) | `given`, imports, `export` |
| 17 | **Functional programming** | [`FunctionalProgramming.scala`](../src/main/scala/learning/FunctionalProgramming.scala) | `immutableValuesExample`, `pureFunctionsExample`, `optionExample`, `optionToReplaceNullExample`, `trySuccessFailureExample` |

---

## End of this path

The material in [`FunctionalProgramming.scala`](../src/main/scala/learning/FunctionalProgramming.scala) ends with a recap of:

- Avoiding `null` (prefer `Option`)
- Expressing errors without exceptions where appropriate (`Option`, `Try`, `Either`)
- Using `match` and `for` comprehensions with `Option` / `Try`

**Next steps (not part of the numbered path above):** deeper domain modeling, effect systems, or HTTP/JSON stacks—see the backend-oriented section in the root [`README.md`](../README.md) if that matches your goals.

---

## Related official docs

- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html)
- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html)
- [Collections overview](https://docs.scala-lang.org/overviews/core/collections.html)
