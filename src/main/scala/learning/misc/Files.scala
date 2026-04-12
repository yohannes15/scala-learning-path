package learning.misc

/*
How To Read A File
---------------------------------

The os-lib library is widely considered the best way to handle
files in Scala.

To add os-lib, add the scala toolkit into your dependencies
or just a specific version of OS-lib

    `lazy val example = project.in(file("."))
    .settings(
        scalaVersion := "3.4.2",
        libraryDependencies += "org.scala-lang" %% "toolkit" % "0.7.0"
        // libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.3"
    )`

Reading a file
-------------------------------
Suppose you have a path to a file. You can read using os.read. To get
seq of lines use os.read.lines. To get a stream of lines use os.read.
lines.stream
 */

val path: os.Path = os.pwd / "capstone" / "samples" / "receipt-good.txt"

def fileOperationsExample() =
  val readFullContent: String = os.read(path)
  println("The full content is: ")
  println("----------------------")
  println(readFullContent)

  val lines: Seq[String] = os.read.lines(path)
  println(s"The longest line is: ${lines.maxBy(_.size)}")
  println("----------------------")

  // memory efficient
  val lineStream: geny.Generator[String] = os.read.lines.stream(path)
  lineStream.foreach(print(_))
