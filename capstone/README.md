# Capstone 1 — before Cats

This is the **first capstone** in this repo: finish it before **[Phase B — Cats → Cats Effect](../README.md)** (see main `README.md`).

**Everything for this capstone lives under the `capstone/` folder** (this file, samples, and Scala sources).

**Goal:** Build **habit**, **`Either`**, and **small programs** so Cats feels like a new library, not a second first language.

**Time box:** About **1–2 weeks** at an easy pace (adjust freely).

---

## Layout (`capstone/`)

| Path | Purpose |
| --- | --- |
| **`README.md`** | This checklist (steps 1–4 + boss project) |
| **`src/main/scala/capstone/`** | Code — packages `capstone.mini`, `capstone.receipt`, … |
| **`samples/`** | Example `.txt` files for the boss receipt parser |

---

## Why this exists

Cats assumes you’re comfortable with **generic types**, **`map` / `flatMap` intuition**, and **errors as values** (`Option`, `Either`). This capstone is the **playground** before **type classes** and **`IO`**.

---

## 1. Tiny CLI (Phase A capstone)

- [ ] Use package **`capstone.mini`** (stub: `src/main/scala/capstone/mini/MiniCli.scala`) — **not** the giant `hello` in `Basics.scala`.
- [ ] One **`@main`** that reads **stdin** or **one string argument** (your choice).
- [ ] Parse input into a **`case class`**; on bad input return **`Either[String, T]`** and print a **clear** left/right outcome.
- [ ] **Ideas (pick one):** guess-the-number, mood logger (`good|ok|bad` → emoji), fake “credit band” (`score` + `income` → `Approved` / `Declined` with reason).

---

## 2. Reps that feel like games

- [ ] **[Advent of Code](https://adventofcode.com/)** — any year, **days 1–3** only. Parse in Scala; use **`Either`** when input can be junk.
- [ ] **[Exercism — Scala](https://exercism.org/tracks/scala)** — **3–5** easy exercises; prefer **`Option`** / lists.

*(External sites — no subfolder required; add notes under `capstone/notes/` if you want, optional.)*

---

## 3. Brush-up (only if rusty)

- [ ] Skim **`Either`** in the [Scala 3 Book — functional error handling](https://docs.scala-lang.org/scala3/book/fp-functional-error-handling.html) (and `Option` there).
- [ ] **`for`-comprehension** chaining **two** `Either` steps (parse → validate).

---

## 4. Stop — you’re ready for Cats when…

- [ ] You can **explain** in one sentence why **`flatMap`** on `Either` short-circuits on first **Left**.
- [ ] You’ve **finished** one small CLI or AoC mini you’d show without apologizing.

---

## Then what?

Main roadmap: **[README.md](../README.md)** → **Phase B — Cats → Cats Effect**.

---

## If you get stuck

- Smaller scope: **half** a CLI (validation only, print `Either` with `println`).
- Skip Exercism if AoC is more fun — **one** track is enough.

---

## Boss project — Receipt line parser + tax summary

**Name:** **Receipt line parser + tax summary**

**Why it tests you:** Real parsing, **line-level errors**, **ADTs**, **`Either`**, **aggregation**, one **file + `@main`** — same *shape* as config/API validation at work, **stdlib-only** (no Cats).

### Input

- Path to a **text file** as the **first program argument** (or stdin — pick one and document in code).
- Each **non-empty** line looks like `description|taxCode|price` (rules in the table below).
- Empty lines: skip.

| Field | Rules |
| --- | --- |
| `description` | Non-empty after trim; max **40** characters. |
| `taxCode` | **`EXEMPT`**, **`STANDARD`**, or **`REDUCED`** (`enum` or sealed type). |
| `price` | Non-negative decimal (`BigDecimal` or `Double` — pick one). **Two decimal places** in input (e.g. `12.99` ok) — state your rule in a comment. |

### Output (success)

- Summary: line count, **subtotal by `taxCode`**, **total tax**, **grand total**, using e.g. **EXEMPT 0%**, **REDUCED 5%**, **STANDARD 10%** (define **one** tax rule in comments and stick to it).

### Output (failure)

- Invalid lines: report **line number** (1-based) and **reason** (e.g. `Line 4: expected '|' separator`). Do **not** silently skip bad lines.

**MVP:** Fail on **first** error (`Either` short-circuit).  
**Stretch:** Report **all** bad lines, then non-zero exit — still stdlib only.

### Non-goals

- No HTTP, DB, Cats, or heavy CLI frameworks — `args(0)` is enough.

### Done when

`sbt "runMain capstone.receipt.ReceiptApp path/to/receipt.txt"` works on a **good** and a **bad** sample (see [`samples/`](samples/)).

---

## Run (stubs)

From the **repository root**:

```bash
sbt "runMain capstone.mini.MiniCli"
sbt "runMain capstone.receipt.ReceiptApp capstone/samples/receipt-good.txt"
```

*Keep it fun; Cats will still be there when this capstone is done.*
