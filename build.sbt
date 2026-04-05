ThisBuild / scalaVersion := "3.6.3"
ThisBuild / organization := "learning.local"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "scala-tutorial",
  )
