# Fundamentals

## Why this is useful

Before patterns and libraries, you need the **core language**: how expressions work, how to structure programs with methods and packages, how Scala supports **functional style** (immutability, `Option`, purity), and how **`@main`** turns methods into runnable programs. This folder is the on-ramp: everything here shows up constantly in real Scala code.

## What’s covered

| File | Topics |
|------|--------|
| `Basics.scala` | Tutorial `hello` entry point; interactive I/O sketch; maps and basic usage patterns |
| `ControlStructures.scala` | `if`/`for`/`match`, pattern matching, guards, expression-oriented style |
| `Methods.scala` | Method definitions, visibility, extension-style examples |
| `MainMethods.scala` | `@main` methods, command-line parsing, `CommandLineParser.FromString` |
| `Packages.scala` | Packages, encapsulation, `import` / `export` patterns |
| `Singleton.scala` | `object`, companions, modules from traits |
| `StringInterpolation.scala` | `s`, `f`, raw interpolators, custom interpolators |
| `FunctionalProgramming.scala` | Immutability, pure functions, `Option`, `Try` vs exceptions |

Some examples **import types** from other modules (for example `learning.domain` for enums used in `MainMethods` and `ControlStructures`) so the compiler can tie lessons together across packages.
