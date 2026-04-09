// For a little project like this, the build.sbt file only needs a scalaVersion 
// entry, but we'll add three lines that you commonly see:
name := "HelloWorld"
version := "0.1"
scalaVersion := "3.8.3"

// libraryDependencies: built-in sbt setting that holds the list of external libraries.
// ++=  appends a Seq to the existing list  (+=  adds a single item)
//
// Each dependency has up to four %-separated parts:
//   "org.scalatest"  %%  "scalatest"  %  "3.2.19"  %  Test
//    └─ Group ID      │   └─ Artifact   └─ Version   └─ Scope
//                     │
//                    %%  auto-appends the Scala version suffix to the artifact
//                        e.g. resolves to "scalatest_3" for Scala 3
//                     %  (single) used for Java libs or exact artifact names
//                        e.g. "org.postgresql" % "postgresql" % "42.7.0"
//
// Test scope: dependency is only on the classpath during tests, not in production
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)
