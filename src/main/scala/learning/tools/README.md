# Tools

## What's covered

Coursier, sbt build tool, ScalaTest testing framework

---

## Coursier

[Coursier](https://get-coursier.io/docs/overview) is a dependency resolver — similar to Maven and Ivy — written from scratch in Scala. It embraces functional programming principles and downloads artifacts in parallel for fast resolution. sbt uses it under the hood for all dependency resolution. As a standalone CLI tool it can also install sbt, Java, and Scala on your system.

---

## sbt

[sbt](https://www.scala-sbt.org/) is the first build tool created specifically for Scala.

### How it boots

```
sbt (launcher)
  └── reads project/build.properties   → picks sbt version
        └── reads build.sbt            → picks Scala version, dependencies
              └── compiles src/         → produces bytecode in target/
```

### Directory structure

```
.
├── build.sbt                     # Scala version, dependencies, settings
├── project/
│   └── build.properties          # sbt version
├── src/
│   ├── main/
│   │   ├── scala/                # app source
│   │   ├── java/                 # Java source (optional)
│   │   └── resources/            # config files, images (optional)
│   └── test/
│       ├── scala/                # test source
│       ├── java/                 # Java test source (optional)
│       └── resources/            # test resources (optional)
├── target/                       # sbt output (generated, don't edit)
└── lib/                          # unmanaged JARs (optional)
```

### build.sbt styles

**Bare style** — simple, single-project builds:

```scala
name         := "HelloWorld"
version      := "0.1"
scalaVersion := "3.8.2"
```

**Multi-project style** — explicit `lazy val` per subproject:

```scala
ThisBuild / scalaVersion := "3.8.2"   // applies to all subprojects

lazy val root = (project in file("."))
  .settings(
    name := "my-app",
    libraryDependencies ++= Seq(...)
  )
```

Bare settings are shorthand — sbt treats them as if they were inside a single `lazy val root` block.

### `libraryDependencies` format

```scala
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
//  └─ Group ID    │   └─ Artifact  └─ Version  └─ Scope
//                %%
//                 └─ picks the right artifact for your Scala binary version (e.g. scalatest_3)
//                  % would pin the exact artifact name (used for Java libs)
)
```

- `++=` appends a `Seq` to the existing list; `+=` adds a single item
- `%%` resolves to e.g. `scalatest_3` for Scala 3
- `%` (single) for Java libraries: `"org.postgresql" % "postgresql" % "42.7.0"`
- `Test` scope — only on the classpath during tests, not in production

### Creating a new project

Manually:

```bash
mkdir HelloWorld && cd HelloWorld
mkdir -p src/{main,test}/scala
mkdir project target
# then create build.sbt and project/build.properties
```

From a template:

```bash
sbt new scala/scala3.g8
```

### Common sbt commands


| Command                          | Description                                                                    |
| -------------------------------- | ------------------------------------------------------------------------------ |
| `sbt compile`                    | Compile sources                                                                |
| `sbt run`                        | Run the main class                                                             |
| `sbt test`                       | Run all tests                                                                  |
| `sbt clean`                      | Delete `target/` (clears stale build cache)                                    |
| `sbt updateClassifiers`          | Download `-sources.jar` for all dependencies (enables go-to-definition in IDE) |
| `sbt "runMain com.example.Main"` | Run a specific main class                                                      |
| `sbt "project foo"`              | Switch to subproject `foo` in a multi-project build                            |


---

## ScalaTest

[ScalaTest](https://www.scalatest.org/) is the most widely used testing framework for Scala. It is flexible and supports several testing styles. The simplest to get started with is `AnyFunSuite`.

### Setup

Add to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)
```

### AnyFunSuite style

```scala
import org.scalatest.funsuite.AnyFunSuite

class MathUtilsTests extends AnyFunSuite:

  test("'double' should handle zero") {
    val result = MathUtils.double(0)
    assert(result == 0)
  }

  test("'double' should handle 1") {
    val result = MathUtils.double(1)
    assert(result == 2)
  }

  test("test with Int.MaxValue") (pending)   // placeholder — not written yet

end MathUtilsTests
```

- Extend `AnyFunSuite`
- Each `test("name") { ... }` block is one test case
- Use `assert(condition)` to verify expected behaviour
- Mark unwritten tests as `(pending)` — they show up in the report without failing
- Similar to JUnit if you're coming from Java

### Running tests

```bash
sbt test                        # run all tests
sbt "testOnly math.MathUtilsTests"  # run a specific test class
```

