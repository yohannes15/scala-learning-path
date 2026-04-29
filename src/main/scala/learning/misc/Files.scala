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
        scalaVersion := "3.8.2",
        libraryDependencies += "org.scala-lang" %% "toolkit" % "0.7.0"
        // libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.3"
    )`

Reading a file
-------------------------------
Suppose you have a path to a file. You can read using os.read. To get
seq of lines use os.read.lines. To get a stream of lines use os.read.
lines.stream
 */

/*
os.read.lines.stream vs release (Cats Resource, FileInputStream)
-------------------------------------------------------------
Resource.make is for things that own a scarce resource (fd, stream, etc.)
and need a paired release when the scope ends.

FileInputStream: you open it and must close it yourself; Resource.release
matches that story.

os.read.lines.stream returns a geny.Generator[String]. In os-lib (ReadWriteOps),
the Generator’s generate opens read.inputStream → InputStreamReader →
BufferedReader, reads with readLine, and puts is.close / isr.close / buf.close
in a finally. So cleanup is inside that method—same “close in finally” idea,
not a naked handle left to the caller.

The file opens when iteration enters generate; if nobody runs the generator,
nothing opens. When generate finishes (end of file, Generator.End, or
exception), finally runs.

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
