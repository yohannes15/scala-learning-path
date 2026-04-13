//  are two styles of writing build.sbt:

// This file — "multi-project" / programmatic style:

// Settings are scoped with ThisBuild / (applies to all subprojects)
// Projects are defined as lazy val — explicit Scala values
// .settings(...) groups all settings for that project
// Designed to support multiple subprojects in one build

ThisBuild / scalaVersion := "3.6.3"
ThisBuild / organization := "learning.local"
ThisBuild / version      := "0.1.0-SNAPSHOT"

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

// Theyre functionally equivalent for a single project — the bare style is just shorthand.
// Under the hood, sbt treats bare settings as if they were inside a lazy val root = (project in file(".")).settings(...)
// block. The multi-project style becomes necessary once you have more than one subproject.
