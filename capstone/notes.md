# Capstone notes — `MiniCli` (credit band)

Scratch notes for **Capstone 1 §1**: fake credit decision CLI — parse score + income, **`Either`**, regex `split`, empty-line edge case. See **`capstone/README.md`** for the full project description.

## Input parsing

### Regex `[,\\s]+` (Scala string passed to `split`)

- Splits on **one or more** commas and/or **whitespace** (space, tab, …).
- Examples that yield **two tokens**: `"720 50000"`, `"720,50000"`, `"720 , 50000"`.

### Pieces

| Piece | Meaning |
| --- | --- |
| `[` … `]` | One character from the set inside. |
| `,` | Literal comma. |
| `\\s` in a Scala string | Becomes `\s` in the regex → whitespace. |
| `+` | One or more of the preceding group (here, the character class). |
| `\\` in the string | Escaping so the compiler passes `\` through to the regex engine. |

## Empty lines

- After `trim`, an empty line is `""`.
- `"".split("[,\\s]+")` yields **`Array("")`**: length **1**, not 0 — one **empty** token. That is why a generic “wrong number of fields” message felt wrong for “user typed nothing.”
- Handle **`line.isEmpty`** before `split` if you want a clear “no input” error.

## Smart constructor (FP-friendly, not exercise answers)

- Keep **`CreditInfo`** as **plain data** (fields); put **range / domain** checks in the **companion object** as something like **`from(...): Either[InvalidInput, CreditInfo]`** so invalid combos never need to exist as bare `CreditInfo` values.
- Optional: **`private`** constructor on the `case class` so only the companion can call the real constructor after checks (see Scala 3 Book / docs on **private case class constructors**).
- **You** wire `for` / `flatMap` to call **`CreditInfo.from(cs, in)`** instead of **`CreditInfo(cs, in)`** once parsing has produced raw numbers.

## `private case class` vs `case class … private (…)`

| | `private case class A(…)` | `case class B private (…)` |
| --- | --- | --- |
| **Hides** | The **type** `A` outside the enclosing scope | **Construction** (`apply` / ctor) from outside the allowed region |
| **Typical use** | Whole type is an implementation detail | Public type, creation only via companion / factory |

```scala
object Outer {
  private case class Secret(n: Int) // name Secret not visible outside Outer
}

case class Credit private (cs: Int, income: Int) // Credit is public; Credit(1, 2) may be illegal outside companion
object Credit { def trusted(cs: Int, income: Int): Credit = new Credit(cs, income) }
```

Syntax: **`private case class`** (class hidden) vs **`case class Name private (...)`** — `private` **after** the name, before `(`.
