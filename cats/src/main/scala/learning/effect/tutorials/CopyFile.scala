package learning.effect.tutorials

import java.io.{
  File, FileInputStream, FileOutputStream, InputStream, OutputStream
}
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import cats.effect.IOApp
import cats.effect.ExitCode

import scala.concurrent.duration.*

/** Copying files - basic concepts, resource handling and cancelation
  * -------------------------------------------------------------------- Our
  * goal is to create a program that copies files. We need:
  *
  * 1) a function that carries out such a task, 2) a program that can be invoked
  * from shell and uses that function.
  *
  * First of all we must code the function that copies the content from a file
  * to another file. The function takes the source and destination files as
  * parameters. But this is functional programming! So invoking the function
  * shall not copy anything, instead it will return an `IO` instance that
  * encapsulates all the side effects involved:
  *
  *   - opening/closing files
  *   - reading/writing content
  *
  * This way purity is kept. Only when that IO instance is evaluated all those
  * side-effectful actions will be run. In our implementation the IO instance
  * will return the amount of bytes copied upon execution, but this is just a
  * design decision. Of course errors can occur, but the IO instance will carry
  * the error raised.
  *
  * IOApp is a kind of 'functional' equivalent to Scala's `App`, where instead
  * of coding an effectful main method we code a pure run function. When
  * executing the class a main method defined in IOApp will call the run
  * function we have coded. Any interruption (like pressing Ctrl-c) will be
  * treated as a cancelation of the running IO.
  *
  * When coding IOApp, instead of a main function we have a run function, which
  * creates the IO instance that forms the program. In our case, our run method
  * can look like this:
  *
  * {{{
  *    sbt 'cats/runMain learning.effect.tutorials.CopyFile cats/.../origin.txt cats/.../dest.txt --yes'
  * }}}
  *
  * Pass `--yes` / `-y` / `--overwrite` when the destination may already exist but
  * you have no TTY (e.g. `sbt copyFileDemo` with a forked JVM: stdin often does
  * not reach `readLine` reliably even with `connectInput`).
  *
  * [[CopyFilePoly]] is a second `IOApp` with the same flags and prompts; it runs the
  * copy through `Async`/`Resource` only (see that object for the polymorphic pipeline).
  */
object CopyFile extends IOApp:

  private def pathArgs(args: List[String]): List[String] =
    args.filterNot(a => a.startsWith("-"))

  private def overwriteFlag(args: List[String]): Boolean =
    args.exists {
      case "-y" | "--yes" | "--overwrite" => true
      case _                              => false
    }

  /** Pause between buffered read/write iterations so you can Ctrl-C during copy
    * and see [[inputStream]] / [[outputStream]] release logs. Set to
    * `Duration.Zero` (or import `*` and use `0.seconds`) for normal/fast
    * copies.
    */
  private val transferSleepBetweenChunks: FiniteDuration = 2.seconds
  override def run(args: List[String]): IO[ExitCode] =
    for
      // Flags (`--yes`, ...) are optional; positional args are origin then dest.
      paths = pathArgs(args)
      forceOverwrite = overwriteFlag(args)
      _ <- IO.raiseWhen(paths.length != 2)(
        new IllegalArgumentException(
          "Need exactly two paths: origin and destination (optional: --yes|-y|--overwrite)."
        )
      )
      orig = new File(paths(0))
      dest = new File(paths(1))
      _ <- IO.raiseWhen(
        orig == dest
      )(new IllegalArgumentException(
        "origin and destination paths can't be identical"
      ))
      _ <- IO.raiseWhen(
        !orig.exists()
      )(new IllegalArgumentException("origin doesn't exist"))
      // File API hits the FS; keep the check in IO for consistent effect boundaries.
      destExists <- IO.blocking(dest.exists())
      overwrite <-
        if !destExists then IO.pure(true)
        else if forceOverwrite then
          IO.println(s"Destination exists — overwriting (--yes): ${dest.getPath}") *>
            IO.pure(true)
        else
          cats.effect.std.Console[IO].println(
            "Dest path already exists. Type yes/no (or y/n): overwrite?"
          ) >>
            IO.blocking {
              Option(scala.io.StdIn.readLine())
                .map(_.trim.toLowerCase)
                .getOrElse("") match
                case "y" | "yes" => true
                case _           => false
            }
      count <-
        if overwrite then copy(orig, dest) else IO.pure(0L)
      _ <-
        if overwrite then
          IO.println(
            s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"
          )
        else
          IO.println(
            s"Copy skipped; did not overwrite existing file ${dest.getPath}."
          )
    yield if overwrite then ExitCode.Success else ExitCode(1)

  /** Opening a stream is considered a side-effectful action, so we have to
    * encapsulate those actions on their own IO instances. We can just embed the
    * actions by calling IO(action), but when dealing with input/output actions,
    * it is advised to be used IO.blocking(action) instead.This helps CE to plan
    * how to assign threads to actions.
    *
    * We will use a Resource to orderely create/use/release resources.
    */
  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make(IO.blocking(new FileInputStream(f))) { inStream =>
      IO.println(s"[release] closing input stream (${f.getPath})") >>
        IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make(IO.blocking(new FileOutputStream(f))) { outStream =>
      IO.println(s"[release] closing output stream (${f.getPath})") >>
        IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit)
    }

  /** We want to ensure that streams are closed once we are done using them, no
    * matter what. That is why we use `Resource` in both inputStream and
    * outputStream functions, each one returning one `Resource` that
    * encapsulates the actions for opening and then closing each stream.
    *
    * `inputOuputStreams` encapsulates both resources in a single Resource
    * instance that will be available once the creation of both streams has been
    * successful, and only in that case. Resources can be combined in
    * for-comprehensions. Note also when releasing resources we must also take
    * care of any possible error during the release itself, eg with
    * .handleErrorWith. In this case we just ignore, but normally it should be
    * at least logged. Often you will see `.attempt.void` - this is used to get
    * the same `swallow and ignore errors` behavior
    */
  def inputOutputStreams(
      in: File,
      out: File
  ): Resource[IO, (InputStream, OutputStream)] =
    for
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    yield (inStream, outStream)

  /** Optionally, we could have used `Resource.fromAutoCloseable` to define our
    * resources. This creates Resource instances over objects that implement the
    * java.lang.AutoCloseable interface without having to define how the
    * resource is released.
    *
    * This code is simpler but we would not have control over what would happen
    * if close operation throws an exception. Also it could be that we want to
    * be aware when closing operations are being run, for example using logs. In
    * contrast, using Resource.make allows to easily control the actions of the
    * release phase
    */
  def inputStreamFromAC(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO.blocking(new FileInputStream(f)))

  /** copies file from origin to destination. When run, all side-effects will be
    * actually executed and the IO instance will return the bytes copied in a
    * Long (note that IO is parameterized by the return type)
    */
  def copy(origin: File, destination: File): IO[Long] =
    val streamResources = inputOutputStreams(origin, destination)
    streamResources.use {
      // We can use transferPoly or transfer (showcasing using IO vs Sync/Async)
      case (in, out) => transfer(in, out, new Array[Byte](1024 * 10), 0)
    }

  /** If you are familiar with cats-effect's `bracket` you may wonder why we use
    * `Resource` elsewhere — `Resource` is built from bracket-like guarantees.
    *
    * Bracket has three stages, each an `IO`: acquire, use, release. **Once
    * `acquire` completes successfully**, `release` runs when `use` is done,
    * whether `use` finished normally, failed, or was cancelled. So “release
    * always runs” is true **after** a successful acquire and **around** `use`;
    * it is not invoked for a resource you only half-opened inside a failing
    * `acquire`.
    *
    * Here, `acquire` is a **single** `IO` that opens both streams (`.tupled`
    * combines `inIO` and `outIO` into one effect). If the input opens but the
    * output throws **before** that combined `acquire` succeeds, the bracket
    * never reaches “acquired”: **`release` is not called**, and the input
    * stream can leak. The remedy is nested brackets (or `Resource` /
    * `flatMap`), so each stream has its own acquire/release pair. A one-shot
    * `bracket` on a tupled pair is still fine when that failure mode is
    * acceptable; for several lifetimes, `Resource` is usually clearer.
    */
  def copyBracketVersion(origin: File, destination: File): IO[Long] =
    val inIO: IO[InputStream] = IO.blocking(new FileInputStream(origin))
    val outIO: IO[OutputStream] = IO.blocking(new FileOutputStream(destination))

    (inIO, outIO) // Stage 1: Getting resources
      .tupled // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
      .bracket {
        case (in, out) =>
          transfer(
            in,
            out,
            new Array[Byte](1024 * 10),
            0L
          ) // Stage 2: Using resources (for copying data, in this case)
      } {
        case (in, out) => // Stage 3: Freeing resources
          (IO(in.close()), IO(out.close()))
            .tupled // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
            .void.handleErrorWith(_ => IO.unit)
      }

  /** This will do the real work of actually copying the data, once the streams
    * are obtained. When they are not needed anymore, whatever the outcome of
    * transfer (success/failure) both streams will be closed. If any of the
    * streams could not be obtained, then transfer will not be run.
    *
    * Even better, because of Resource semantics, if there is any problem
    * opening the input file then the output file will not be opened. On the
    * other hand, if there is any issue opening the output file, then the input
    * stream will be closed.
    *
    * Steps are read data from the input stream into a buffer, and the write the
    * buffer contents into output stream. At same time, loop keeps a counter of
    * the bytes transfer for return. Observe that both input and output actions
    * are created by invoking IO.blocking.
    *
    * IO, being a monad, we can sequence our new IO instances using a for-comp
    * to create another IO. The for-comp loops as long as the call to read()
    * does not return a negative value that would signal that the end of the
    * stream has reached.
    *
    * >> is a Cats operator to sequence two operations where the ouput of the
    * first is not needed by the second. it is equivalent to
    * `first.flatMap(_ => second)`. It means that after each write operation we
    * recursively all transfer again, but as IO is stack safe we are not
    * concerned about stack overflow issues. At each iteration we increase the
    * counter acc with the amount of bytes read at that iteration
    */
  def transfer(
      origin: InputStream,
      destination: OutputStream,
      buffer: Array[Byte],
      acc: Long
  ): IO[Long] =
    for
      amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
      count <-
        (
          if (amount > -1) then
            (
              IO.sleep(transferSleepBetweenChunks) >>
                IO.blocking(destination.write(buffer, 0, amount)) >>
                transfer(origin, destination, buffer, acc + amount)
            )
          // end of read stream reached (by java.io.InputStream contract)
          else IO.pure(acc)
        )
    yield count

/** TODO: Pending exercise: recursive folder copy (cancelable).
  *
  * Create a program that copies folders; if the origin has nested folders, copy
  * recursively. Cancellation should remain safe at any point (`Resource`, sleep
  * between chunks vs `IO.sleep` tradeoffs apply as with this tutorial).
  */
