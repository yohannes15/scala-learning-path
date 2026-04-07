# Contextual Abstractions

## The Problem: Passing context everywhere is painful

Imagine every function in a large codebase needs access to the same
"ambient" information — a database connection, a sort order, an execution context, a logger.
Without contextual abstractions, you must thread it manually through every call:

```scala
def findUser(id: Int, db: Database): User       = ...
def loadPrefs(user: User, db: Database): Prefs  = ...
def render(prefs: Prefs, db: Database): String  = ...

render(loadPrefs(findUser(42, db), db), db)   // `db` everywhere
```

This is repetitive, noisy, and error-prone. Contextual abstractions let the compiler
thread `db` through automatically once you declare it.

---

## The Solution: `given` and `using`

The two core Scala 3 keywords are:

- **`given`** — declares a contextual value the compiler can supply.
  *"Here is the canonical value for type T."*
  `given` instances allow programmers to define the canonical value of a certain type.
  This makes programming with type-classes more straightforward without leaking implementation details.

- **`using`** — marks a parameter that should be filled in from context.
  *"Compiler, find me the canonical value for type T."*
  `using` clauses allow programmers to abstract over information that is available in the
  calling context and should be passed implicitly.

```scala
given db: Database = Database.connect(...)
// one declaration — compiler remembers this for the whole scope

def findUser(id: Int)(using db: Database): User = db.query(id)
// no need to pass db at every call site

findUser(42)   // compiler fills in `db` automatically
```

---

## Term Inference vs Type Inference

Both reduce boilerplate by having the compiler fill something in for you:

| | What gets filled in | Example |
|---|---|---|
| **Type inference** | a *type* | `val x = 42` → inferred as `val x: Int = 42` |
| **Term inference** | a *value* | `findUser(42)` → inferred as `findUser(42)(using db)` |

`given`/`using` is Scala's term inference system.

---

## Use Cases

| Pattern | Description | Example |
|---|---|---|
| Type classes | Define canonical behaviour for a type | `Ordering[T]`, `Show[T]`, `Eq[T]` |
| Context propagation | Pass ambient values without explicit threading | `ExecutionContext`, `Logger`, `Config` |
| Dependency injection | Wire dependencies at compile time, not runtime | |
| Capabilities | Express what a piece of code is allowed to do | `"this function can perform I/O"` |
| Type-level proofs | Express and verify relationships between types | `A =:= B`, `A <:< B` |

---

## Scala 2 vs Scala 3

In Scala 2, all of this was done with a single keyword: `implicit`.
One keyword covered many unrelated use cases, which made it hard to learn, easy to misuse,
and hard to read (implicits could silently appear from many places).

Scala 3 replaces `implicit` with several focused, intent-driven features:

| Scala 2 | Scala 3 |
|---|---|
| `implicit val` / `implicit def` | `given` |
| `implicit` parameter | `(using ...)` |
| `implicit class` / implicit conversion | extension methods + `Conversion[A, B]` |
| `import x.implicits._` | `import x.given` (explicit) |

**Key improvements in Scala 3:**
- One clear way to define a given value
- One clear way to declare a contextual parameter (`using`)
- Givens must be imported explicitly — they can't silently appear
- Implicit conversions require an explicit `Conversion[A, B]` instance
- The compiler gives actionable suggestions when a given can't be found.
  In case an implicit parameter cannot be resolved by the compiler, it now provides
  import suggestions that may fix the problem.
- Context functions make contextual abstractions a first-class citizen

---

## Influence on Other Languages

Scala's approach has influenced many languages:

- **Rust** — traits and trait bounds (type class dispatch)
- **Swift** — protocol extensions
- **Kotlin** — compile-time dependency resolution (proposed)
- **C#** — Shapes and Extensions (proposed)
- **F#** — Traits (proposed)

Contextual abstractions are also central to theorem provers like **Coq** and **Agda**,
where the compiler synthesises proof terms from their types.
---
