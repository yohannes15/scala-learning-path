# Functions

## Why this is useful

In Scala, **functions are values**: you pass them to methods, return them, and partially apply them. That style is the default for collection pipelines, concurrency, and FP libraries. This module shows how to read and write **first-class functions** idiomatically.

## What’s covered

| File | Topics |
|------|--------|
| `AnonymousFunctions.scala` | Function literals, `=>` syntax |
| `FunctionVariables.scala` | Storing functions in `val`s, function types |
| `FirstClassFunctions.scala` | Passing and returning functions |
| `HigherOrderFunctions.scala` | Methods that take or return functions (`map`, custom HOFs) |
| `PartialFunctions.scala` | `PartialFunction`, `collect`, domain restrictions |
| `ReturningFunctions.scala` | Methods whose result is a function — closures |
| `EtaExpansion.scala` | When methods become functions — eta expansion |
