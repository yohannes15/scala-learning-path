//  are two styles of writing build.sbt:

// This file — "multi-project" / programmatic style:

// Settings are scoped with ThisBuild / (applies to all subprojects)
// Projects are defined as lazy val — explicit Scala values
// .settings(...) groups all settings for that project
// Designed to support multiple subprojects in one build

ThisBuild / scalaVersion := "3.8.2"
ThisBuild / organization := "learning.local"
ThisBuild / version := "0.1.0-SNAPSHOT"

// `capstone` is its own subproject (own target/, deps, run). It does not depend on `root`
// and `root` does not aggregate it — compile each explicitly: `sbt compile` vs `sbt capstone/compile`.
lazy val capstone = (project in file("capstone"))
  .settings(
    name := "capstone",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scala-lang" %% "toolkit" % "0.7.0"
    )
  )

// Sample mini-project (sources under `sbtsampleproj/`)
lazy val sbtsmallproj = (project in file("sbtsampleproj"))
  .settings(
    name := "HelloWorld",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )

lazy val root = (project in file("."))
  .settings(
    name := "scala-tutorial",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scala-lang" %% "toolkit" % "0.7.0"
    )
  )

lazy val cats = (project in file("cats"))
  .settings(
    name := "learning-cats",
    // starts a separate JVM
    Compile / run / fork := true,
    // Needed so stdin is wired when CopyFile prompts to overwrite (`StdIn.readLine`).
    Compile / run / connectInput := true,
    // Default fork cwd is this subproject (`cats/`). Paths like `cats/src/...` then become
    // `cats/cats/...` and break. Use the build root (repo root / `/workspaces` in devcontainer).
    Compile / run / forkOptions := ForkOptions()
      .withRunJVMOptions((Compile / run / javaOptions).value.toVector)
      .withWorkingDirectory(Some((ThisBuild / baseDirectory).value)),
    // Lets `Nested[Option, Validated[String, *], Int]` match the Cats Nested docs notation.
    scalacOptions ++= Seq(
      "-Xkind-projector",
      "-Wnonunit-statement" // warn on pure expressions as statements (e.g. unused `IO`)
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "toolkit" % "0.1.7",
      "org.typelevel" %% "cats-core" % "2.13.0",
      // "core" module - IO, IOApp, schedulers
      // This pulls in the kernel and std modules automatically.
      "org.typelevel" %% "cats-effect" % "3.7.0",
      // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
      "org.typelevel" %% "cats-effect-kernel" % "3.5.3",
      // standard "effect" library (Queues, Console, Random etc.)
      "org.typelevel" %% "cats-effect-std" % "3.5.3",
      // Tests: MUnit + cats-effect (`munit` comes in transitively).
      "org.typelevel" %% "munit-cats-effect" % "2.2.0" % Test
    )
  )

// Theyre functionally equivalent for a single project — the bare style is just shorthand.
// Under the hood, sbt treats bare settings as if they were inside a lazy val root = (project in file(".")).settings(...)
// block. The multi-project style becomes necessary once you have more than one subproject.

// Try `learning.effect.tutorials.CopyFile` with bundled tutorial files (paths from build root).
addCommandAlias(
  "copyFileDemo",
  "cats/runMain learning.effect.tutorials.CopyFile cats/src/main/scala/learning/effect/tutorials/origin.txt cats/src/main/scala/learning/effect/tutorials/dest.txt --yes"
)
addCommandAlias(
  "copyFilePolyDemo",
  "cats/runMain learning.effect.tutorials.CopyFilePoly cats/src/main/scala/learning/effect/tutorials/origin.txt cats/src/main/scala/learning/effect/tutorials/dest.txt --yes"
)
