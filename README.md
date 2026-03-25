# Scala Backend API Learning Path (Typelevel First → ZIO Second)

**Profile:** experienced backend engineer (Python/C#), prefers **videos + official docs** over books, target **REST/JSON APIs** with **PostgreSQL**.

**Primary stack (learn first):** Scala 3 · **Cats Effect** · **http4s** · **circe** · **doobie** · Postgres (**Flyway**).

**Secondary stack (learn after):** **ZIO** · **zio-http** (or ZIO-native HTTP) — you reuse effect-system intuition; you mainly learn new APIs and ecosystem layout.

---

## Why this order (transferability)

| Learn first | Learn second | Why |
|-------------|--------------|-----|
| **Cats Effect + http4s + doobie** | **ZIO + zio-http** | The **Typelevel** bundle (Cats, Cats Effect, http4s, circe, doobie) appears constantly in **jobs, tutorials, and OSS**. You learn **multiple mature libraries** and how they compose—closest to “general Scala backend literacy.” |
| | | **ZIO** is coherent and fast to ship with, but it is its own universe. Coming **from** Typelevel **to** ZIO is straightforward: you already know `IO`/`Resource`/error channels; you map concepts to `ZIO`/`ZLayer`. Going **ZIO → Typelevel** is doable but you spend more time reconciling **Cats**/`IO`/`fs2` patterns in older code. |

**Rule of thumb:** master **one effect system deeply** (Cats Effect), then add **ZIO** in 2–3 weeks of focused docs + a port of a small service.

---

## Tooling (week 0, before content)

Set these up once; slow tooling kills momentum.

| Tool | Purpose | Link |
|------|---------|------|
| **JDK 17+** (LTS) | JVM for Scala 3 | [Adoptium Temurin](https://adoptium.net/) |
| **sbt** | build / REPL / tests | [sbt download](https://www.scala-sbt.org/download.html) |
| **Metals** | IDE support in VS Code / Cursor / IntelliJ | [Metals](https://scalameta.org/metals/) |

**Minimal sanity check:** `sbt new http4s/http4s.g8` or clone the [http4s quickstart](https://http4s.org/versions/) and run the server locally.

---

## Phase A — Scala 3 language (before HTTP)

**Goal:** read and write `case class`, `enum`, `match`, `Option` / `Either`, collections, `for`-comprehensions, packages, basic ADTs for API models.

### Docs (read in order)

1. [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html) — work through at least: **Basics**, **Types**, **Control structures**, **Collections**, **FP**, **Contextual abstractions** (high level).
2. [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) — skim as reference.

### Videos (parallel track)

- [Rock the JVM — Scala](https://rockthejvm.com/p/the-scala-bundle) — use their **Scala 3** modules as your “lecture,” docs as your “reference.”

### Practice

- 50–100 small exercises: [Scala Exercises](https://www.scala-exercises.org/scala_tutorial) (or Rock the JVM exercises if bundled).

**Exit criteria:** implement a CLI or small library that parses input and returns `Either[String, Result]`—same shape as validation in APIs.

---

## Phase B — Cats → Cats Effect (the spine)

**Goal:** understand **type classes**, **`Functor` / `Monad` / `Applicative`** at *usage* level (not category theory), then **`IO`**, **`Resource`**, **`Fiber`/`Deferred`** at *practical* level.

### Docs

1. [Cats](https://typelevel.org/cats/) — start with **getting started** and **data types** (`Option`, `Either`, `Validated`, `NonEmptyList`).
2. [Cats Effect](https://typelevel.org/cats-effect/) — **`IO`**, **`Resource`**, **thread model** (skim), **concurrency** basics.

### Videos

- Rock the JVM: **Cats** and **Cats Effect** courses (follow the same order as docs).

**Exit criteria:** write a program that reads config, opens a `Resource` (e.g. file or fake connection), runs `IO`, shuts down cleanly.

---

## Phase C — http4s (HTTP API surface)

**Goal:** routes, request/response, entity bodies, middleware, running a server.

### Docs

1. [http4s — versions / getting started](https://http4s.org/versions/)
2. [http4s — service / DSL](https://http4s.org/v1.0/service/) (adjust version in URL to match your `build.sbt`)
3. [http4s — JSON](https://http4s.org/v1.0/json/) — usually with **circe**

### Videos

- Rock the JVM: **Cats Effect** + **http4s** content (build the official quickstart alongside).

**Milestone project (v1):**

- `GET /health` → 200 JSON `{ "status": "ok" }`
- `POST /items` JSON body → validate → 201 or 400 with error body
- `GET /items/:id` → 200 or 404

Store items **in memory** (`Ref` from Cats Effect) first.

---

## Phase D — circe (JSON)

**Goal:** encode/decode, handle validation errors, keep codecs separate from routes.

### Docs

1. [circe](https://circe.github.io/circe/)

**Pattern:** define `case class` request/response types + **semiauto** or **derivation** codecs; map domain errors to HTTP in one place.

---

## Phase E — doobie + PostgreSQL

**Goal:** typechecked SQL, transactors, connection lifecycle, basic repo layer.

### Docs

1. [doobie — book of doobie](https://tpolecat.github.io/doobie/docs/index.html) — at minimum: **Installation**, **First programs**, **Connecting to a database**, **Selecting**, **Parameterized queries**, **Updates**, **Fragments**.

### Local Postgres

- Install Postgres locally or use Docker; one database per project.

### Migrations

- [Flyway](https://documentation.red-gate.com/flyway/) — versioned SQL migrations; run from sbt or CI.

**Milestone project (v2):** same API as v1, but **persist** `items` in Postgres via doobie; integration tests against a test DB.

---

## Phase F — “production-shaped” API skills

Pick topics as needed; use **docs + one reference implementation** in your repo.

| Topic | Where to start |
|-------|----------------|
| Structured logging | [log4cats](https://typelevel.org/log4cats/) (common with Typelevel stack) |
| Configuration | [Ciris](https://cir.is/) or [pureconfig](https://pureconfig.github.io/docs/) or lightbend config — pick one and stay consistent |
| Testing | [MUnit](https://scalameta.org/munit/) or [ScalaTest](https://www.scalatest.org/) + [http4s client](https://http4s.org/v1.0/client/) for integration tests |
| OpenAPI (optional) | [tapir](https://tapir.softwaremill.com/en/) (often introduced *after* you’re comfortable with http4s) |

---

## Phase G — ZIO (second stack, for transferability)

**Goal:** read ZIO codebases; optionally ship a small duplicate service to feel ergonomics.

### Docs

1. [ZIO](https://zio.dev/) — **ZIO**, **ZLayer**, **ZIO Test**
2. [ZIO HTTP](https://zio.dev/zio-http/) (if you choose this as the HTTP layer)

**Exercise:** re-implement the **same** `items` API (memory → Postgres) using ZIO; compare layering and testing.

---

## 8-week schedule (example)

Assume **~8–12 hrs/week**; adjust proportionally.

| Week | Focus | Deliverable |
|------|--------|-------------|
| **1** | Scala 3 syntax + tooling | sbt project runs; small `Either`-based validation |
| **2** | Collections + ADTs + `for`-comprehensions | domain models for your API (`Item`, errors) |
| **3** | Cats core (`Validated`, `Either` pipelines) | validation module with clear error accumulation |
| **4** | Cats Effect (`IO`, `Resource`) | shutdown-safe `IO` app + `Ref` in-memory store |
| **5** | http4s routes + middleware | REST v1 in-memory |
| **6** | circe codecs + error mapping | stable JSON + consistent error responses |
| **7** | doobie + Postgres + Flyway | same API backed by DB + migrations |
| **8** | Integration tests + hardening | tests for happy paths + DB failures; optional logging/config |

**After week 8:** spend **2–3 weeks** on **ZIO + zio-http** port or parallel microservice.

---

## Quick link index

| Resource | URL |
|----------|-----|
| Scala 3 Book | https://docs.scala-lang.org/scala3/book/introduction.html |
| Tour of Scala | https://docs.scala-lang.org/tour/tour-of-scala.html |
| Scala Exercises | https://www.scala-exercises.org/scala_tutorial |
| Rock the JVM | https://rockthejvm.com/ |
| Coursera FP in Scala (optional depth) | https://www.coursera.org/learn/progfun1 |
| Cats | https://typelevel.org/cats/ |
| Cats Effect | https://typelevel.org/cats-effect/ |
| http4s | https://http4s.org/versions/ |
| circe | https://circe.github.io/circe/ |
| doobie | https://tpolecat.github.io/doobie/docs/index.html |
| Flyway | https://documentation.red-gate.com/flyway/ |
| sbt | https://www.scala-sbt.org/ |
| Metals | https://scalameta.org/metals/ |
| ZIO | https://zio.dev/ |
| ZIO HTTP | https://zio.dev/zio-http/ |

---

## Success criteria (you’re “job-ready” to interview on this stack)

- You can **explain** `IO`, `Resource`, and why `IO` is used for side effects.
- You can **build** a CRUD JSON API with **consistent error responses**.
- You can **write** doobie queries and run them through a **transactor** safely.
- You have **integration tests** that hit real HTTP + real Postgres (testcontainer or local).
- You can **read** a ZIO service and map it mentally to “effects + dependency injection.”

---

*Generated for local study. Update library doc URLs when you pin versions in `build.sbt` (http4s and Cats Effect docs are version-sensitive).*
