# Scala Backend API Learning Path (Typelevel First → ZIO Second)

## Portfolio

A structured roadmap for learning modern Scala backend development, with a focus on the Typelevel ecosystem (Cats Effect, http4s, circe, doobie) and ZIO. Includes runnable examples, exercises, and capstone projects to build REST APIs with PostgreSQL.

This repository documents my hands-on learning journey through the Scala ecosystem, from language fundamentals to production-ready backend services. It's designed for experienced backend engineers (Python, C#, Go) who prefer official docs and videos over books.

---

**Profile:** experienced backend engineer (Python/C#), prefers **videos + official docs** over books, target **REST/JSON APIs** with **PostgreSQL**.

**Primary stack (learn first):** Scala 3 · **Cats Effect** · **http4s** · **circe** · **doobie** · Postgres (**Flyway**).

**Secondary stack (learn after):** **ZIO** · **zio-http** (or ZIO-native HTTP) — you reuse effect-system intuition; you mainly learn new APIs and ecosystem layout.

---

## Why this order? (transferability)

- **Learn first (Typelevel):** **Cats Effect**, **http4s**, **circe**, **doobie** — the bundle shows up constantly in **jobs, tutorials, and OSS**; you learn how those libraries **compose** (closest to "general Scala backend literacy").
- **Learn second (ZIO):** **ZIO** + **zio-http** — coherent and fast to ship; its own universe. From Typelevel **to** ZIO you mostly remap `IO` / `Resource` to `ZIO` / `ZLayer`. **ZIO → Typelevel** is doable but you spend more time reconciling **Cats** / `IO` / **fs2** in older code.

**Rule of thumb:** master **one effect system deeply** (Cats Effect), then add **ZIO** in 2–3 weeks of focused docs + a port of a small service.

---

## Tooling (week 0, before content)

Set these up once; slow tooling kills momentum.


| Tool              | Purpose                                    | Link                                                    |
| ----------------- | ------------------------------------------ | ------------------------------------------------------- |
| **JDK 17+** (LTS) | JVM for Scala 3                            | [Adoptium Temurin](https://adoptium.net/)               |
| **sbt**           | build / REPL / tests                       | [sbt download](https://www.scala-sbt.org/download.html) |
| **Metals**        | IDE support in VS Code / Cursor / IntelliJ | [Metals](https://scalameta.org/metals/)                 |


**Minimal sanity check:** `sbt new http4s/http4s.g8` or clone the [http4s quickstart](https://http4s.org/versions/) and run the server locally.

---

## Phase A — Scala 3 language (before HTTP)

**Goal:** read and write `case class`, `enum`, `match`, `Option` / `Either`, collections, `for`-comprehensions, packages, basic ADTs for API models.

### Phase A — Docs (read in order)

1. [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html) — work through at least: **Basics**, **Types**, **Control structures**, **Collections**, **FP**, **Contextual abstractions** (high level).
2. [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) — skim as reference.

### Phase A — Optional extras

- **Video:** Scala videos on topics you care about (e.g. `Either`, FP, `map` — get comfortable with the FP style).
- **Exercism:** [Exercism — Scala](https://exercism.org/tracks/scala/exercises) (requires a free account).
- **Optional puzzles:** [Advent of Code](https://adventofcode.com/) — past years stay up; each puzzle includes **example input/output in the statement** you can solve against **without logging in** (a login only matters if you want your personalized puzzle input / leaderboard).

**Exit criteria:** implement a CLI or small library that parses input and returns `Either[String, Result]`—same shape as validation in APIs.

**Structured capstone (optional but recommended):** **[`capstone/README.md`](capstone/README.md)** — *Capstone 1* (**complete** in this repo: **MiniCli** + **ReceiptApp**; optional AoC-style reps) before opening Cats docs.

---

## Phase B — Cats → Cats Effect (the spine)

**Goal:** get **practical** type-class fluency first (how to *use* `Functor` through `Traverse`, and the core **data types** below), *then* add **`IO`**, **`Resource`**, and basic **concurrency** (`Fiber` / `Deferred`) in Cats Effect. Category theory is optional.

**Cats core spine (read in dependency order, not sidebar A–Z):** **Semigroup → Monoid → Functor → Applicative → Monad → Foldable / Traverse**. Pair with **`Option`**, **`Either`**, and **`Validated`** (accumulating errors; lines up with Applicative). Skim **[Imports](https://typelevel.org/cats/imports.html)** when implicit resolution or syntax gets confusing. **Nested** and monad transformers can wait until the spine feels obvious.

**Official entry:** [Cats — home](https://typelevel.org/cats/) and [Type classes](https://typelevel.org/cats/typeclasses.html) set the scene; drill into each type class's own page as you go.

**Local notes & runnable snippets:** [`cats/src/main/scala/learning/README.md`](cats/src/main/scala/learning/README.md) (this repo's Cats topic tree).

### Phase B — Docs

1. **[Cats](https://typelevel.org/cats/)** — spine above + data types **`Validated`**, **`Either`**, **`Option`** as you meet them in examples.
2. **[Cats Effect](https://typelevel.org/cats-effect/)** — after the Cats core spine is comfortable: **`IO`**, **`Resource`**, **thread model** (skim), **concurrency** basics.

### Phase B — Optional extras

- [Rock the JVM](https://rockthejvm.com/): **Cats** and **Cats Effect** courses (paid; follow the same order as docs).
- Other free videos on **Cats** and **Cats Effect**.

**Exit criteria (Cats core):** you can explain **Functor vs Applicative vs Monad** in one sentence each, and when to prefer **`Validated`** vs **`Either`** for errors; small snippets compile without fighting implicits.

**Exit criteria (Phase B full):** write a program that reads config, opens a `Resource` (e.g. file or fake connection), runs `IO`, shuts down cleanly.

---

## Phase C — http4s (HTTP API surface)

**Goal:** routes, request/response, entity bodies, middleware, running a server.

### Phase C — Docs

1. [http4s — versions / getting started](https://http4s.org/versions/)
2. [http4s — service / DSL](https://http4s.org/v1.0/service/) (adjust version in URL to match your `build.sbt`)
3. [http4s — JSON](https://http4s.org/v1.0/json/) — usually with **circe**

### Phase C — Videos

- [Rock the JVM](https://rockthejvm.com/): **Cats Effect** + **http4s** content (paid; build the [official quickstart](https://http4s.org/versions/) alongside).

**Milestone project (v1):**

- `GET /health` → 200 JSON `{ "status": "ok" }`
- `POST /items` JSON body → validate → 201 or 400 with error body
- `GET /items/:id` → 200 or 404

Store items **in memory** (`Ref` from Cats Effect) first.

---

## Phase D — circe (JSON)

**Goal:** encode/decode, handle validation errors, keep codecs separate from routes.

### Phase D — Docs

1. [circe](https://circe.github.io/circe/)

**Pattern:** define `case class` request/response types + **semiauto** or **derivation** codecs; map domain errors to HTTP in one place.

---

## Phase E — doobie + PostgreSQL

**Goal:** typechecked SQL, transactors, connection lifecycle, basic repo layer.

### Phase E — Docs

1. [doobie — book of doobie](https://tpolecat.github.io/doobie/docs/index.html) — at minimum: **Installation**, **First programs**, **Connecting to a database**, **Selecting**, **Parameterized queries**, **Updates**, **Fragments**.

### Local Postgres

- Install Postgres locally or use Docker; one database per project.

### Migrations

- [Flyway](https://documentation.red-gate.com/flyway/) — versioned SQL migrations; run from sbt or CI.

**Milestone project (v2):** same API as v1, but **persist** `items` in Postgres via doobie; integration tests against a test DB.

---

## Phase F — "production-shaped" API skills

Pick topics as needed; use **docs + one reference implementation** in your repo.


| Topic              | Where to start                                                                                                                                            |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Structured logging | [log4cats](https://typelevel.org/log4cats/) (common with Typelevel stack)                                                                                 |
| Configuration      | [Ciris](https://cir.is/) or [pureconfig](https://pureconfig.github.io/docs/) or lightbend config — pick one and stay consistent                           |
| Testing            | [MUnit](https://scalameta.org/munit/) or [ScalaTest](https://www.scalatest.org/) + [http4s client](https://http4s.org/v1.0/client/) for integration tests |
| OpenAPI (optional) | [tapir](https://tapir.softwaremill.com/en/) (often introduced *after* you're comfortable with http4s)                                                     |


---

## Phase G — ZIO (second stack, for transferability)

**Goal:** read ZIO codebases; optionally ship a small duplicate service to feel ergonomics.

### Phase G — Docs

1. [ZIO](https://zio.dev/) — **ZIO**, **ZLayer**, **ZIO Test**
2. [ZIO HTTP](https://zio.dev/zio-http/) (if you choose this as the HTTP layer)

**Exercise:** re-implement the **same** `items` API (memory → Postgres) using ZIO; compare layering and testing.

---

## 8-week schedule (example)

Assume **~8–12 hrs/week**; adjust proportionally.


| Week  | Focus                                       | Deliverable                                                  |
| ----- | ------------------------------------------- | ------------------------------------------------------------ |
| **1** | Scala 3 syntax + tooling                    | sbt project runs; small `Either`-based validation            |
| **2** | Collections + ADTs + `for`-comprehensions   | domain models for your API (`Item`, errors)                  |
| **3** | Cats core (`Validated`, `Either` pipelines) | validation module with clear error accumulation              |
| **4** | Cats Effect (`IO`, `Resource`)              | shutdown-safe `IO` app + `Ref` in-memory store               |
| **5** | http4s routes + middleware                  | REST v1 in-memory                                            |
| **6** | circe codecs + error mapping                | stable JSON + consistent error responses                     |
| **7** | doobie + Postgres + Flyway                  | same API backed by DB + migrations                           |
| **8** | Integration tests + hardening               | tests for happy paths + DB failures; optional logging/config |


**After week 8:** spend **2–3 weeks** on **ZIO + zio-http** port or parallel microservice.

---

## Quick link index


| Resource       | URL                                                                                             |
| -------------- | ----------------------------------------------------------------------------------------------- |
| Scala 3 Book   | [docs.scala-lang.org — Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html) |
| Tour of Scala  | [docs.scala-lang.org — Tour](https://docs.scala-lang.org/tour/tour-of-scala.html)               |
| Advent of Code | [adventofcode.com](https://adventofcode.com/) (examples in puzzle text need no account)         |
| Exercism (Scala) | [exercism.org/tracks/scala](https://exercism.org/tracks/scala)                               |
| Rock the JVM | [rockthejvm.com](https://rockthejvm.com/)                                                       |
| Cats           | [typelevel.org/cats](https://typelevel.org/cats/)                                               |
| Cats Effect    | [typelevel.org/cats-effect](https://typelevel.org/cats-effect/)                                 |
| http4s         | [http4s.org/versions](https://http4s.org/versions/)                                             |
| circe          | [circe.github.io](https://circe.github.io/circe/)                                               |
| doobie         | [tpolecat.github.io/doobie](https://tpolecat.github.io/doobie/docs/index.html)                  |
| Flyway         | [documentation.red-gate.com/flyway](https://documentation.red-gate.com/flyway/)                 |
| sbt            | [scala-sbt.org](https://www.scala-sbt.org/)                                                     |
| Metals         | [scalameta.org/metals](https://scalameta.org/metals/)                                           |
| ZIO            | [zio.dev](https://zio.dev/)                                                                     |
| ZIO HTTP       | [zio.dev/zio-http](https://zio.dev/zio-http/)                                                   |


---

## Success criteria (you're "job-ready" to interview on this stack)

- You can **explain** `IO`, `Resource`, and why `IO` is used for side effects.
- You can **build** a CRUD JSON API with **consistent error responses**.
- You can **write** doobie queries and run them through a **transactor** safely.
- You have **integration tests** that hit real HTTP + real Postgres (testcontainer or local).
- You can **read** a ZIO service and map it mentally to "effects + dependency injection."

---

*Generated for local study. Update library doc URLs when you pin versions in `build.sbt` (http4s and Cats Effect docs are version-sensitive).*
