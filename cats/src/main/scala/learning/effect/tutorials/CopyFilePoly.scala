package learning.effect.tutorials

import java.io.{
  File, FileInputStream, FileOutputStream, InputStream, OutputStream
}
import cats.syntax.all.*
import cats.effect.{IO, Resource, Sync, Async}
import cats.effect.IOApp
import cats.effect.ExitCode

import scala.concurrent.duration.*

/** Copying files — polymorphic `cats-effect` helpers **and** a second `IOApp` entry point
  * -------------------------------------------------------------------- Same tutorial story as
  * [[CopyFile]], but this object keeps only `F`-polymorphic `Resource`/`Async`/`Sync` helpers and
  * runs the **same CLI** (`runMain`) as [[CopyFile]] using `copy[IO]`.
  *
  * `sbt cats/runMain learning.effect.tutorials.CopyFilePoly <origin> <dest> [--yes]`
  *
  * Pass `--yes` / `-y` / `--overwrite` when the destination may already exist but
  * you have no TTY (e.g. `sbt copyFileDemo` with a forked JVM: stdin often does
  * not reach `readLine` reliably even with `connectInput`).
  */
object CopyFilePoly extends IOApp:

  private def pathArgs(args: List[String]): List[String] =
    args.filterNot(a => a.startsWith("-"))

  private def overwriteFlag(args: List[String]): Boolean =
    args.exists {
      case "-y" | "--yes" | "--overwrite" => true
      case _                              => false
    }

  /** Pause between buffered read/write iterations so you can Ctrl-C during copy
    * and see [[inputStream]] / [[outputStream]] release logs here. Set to
    * `Duration.Zero` (or import `*` and use `0.seconds`) for normal/fast
    * copies.
    */
  private val transferSleepBetweenChunks: FiniteDuration = 2.seconds

  /** Same CLI and overwrite prompt as [[CopyFile.run]]; executes `copy` on the polymorphic path. */
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
        if overwrite then copy[IO](orig, dest) else IO.pure(0L)
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

  /** Cancelation
    *
    * IO instances execution can be canceled! Cancelation is a powerful but but
    * non-trivial CE feature. It shouldn't be ignored. In CE, some IO instances
    * can be canceled (e.g. by other IO instances running concurrently) meaning
    * that their evaluation will be aborted. If programmer is careful, an
    * alternative IO task will be run under cancelation, for example to deal
    * with potential cleaning up activities.
    *
    * Resource makes dealing with cancelation an easy task. If the IO inside a
    * Resource.use is canceled, the release section of that resource is run. In
    * our example this means the input/output streams will be properly closed.
    * Also, CE does not cancel code inside `IO.blocking` instances. In the case
    * of our transfer function this means the execution would be interrupted
    * only between two calls to IO.blocking. If we want the execution of an IO
    * instance to be interrupted when canceled, without waiting for it to
    * finish, we must instantiate it using `IO.interruptible`!
    *
    * It can be argued that using IO(java.nio.file.Files.copy(...)) would get an
    * IO with the same characterstics of purity as our function, but there is a
    * difference in that our IO is safely cancelable. So the user can stop the
    * running code at any time and our code will deal with safe resource release
    * (streams closing) even under circumstances (like Ctrl-c). The same will
    * apply if the copy function is run from other modules that require its
    * functionality. If the IO returned by this function is cancelled while
    * being run, resources will be properly released.
    *
    * Polymorphic cats-effect code & Sync/Async
    * ------------------------------------------------------------------------
    * There is an important characterstic of IO that we should be aware of. IO
    * is able to suspend side-effects async thanks to the existence of an
    * instance of Async[IO]. Because Async extends Sync, IO can also suspend
    * side-effects synchronously. On top of that Async extends typeclasses such
    * as MonadCancel, Concurrent or Temporal, which bring the possibility to
    * cancel an IO instance, to run serveral IO instances concurrently, to
    * timeout an execution, to force the execution to wait (sleep), etc ...
    *
    * For a polymorphic `transferPoly`, use `Async[F]` (kernel): it extends both
    * `Sync` (blocking reads/writes) and `Temporal` (`sleep`). One constraint
    * avoids ambiguous `Monad`/`FlatMap`/`MonadCancel` givens that you get when
    * you combine separate `Temporal` + `Sync` bounds. `IOApp` supplies
    * `Async[IO]` for `F = IO`.
    */
  def transfer[F[_]: Async](
      origin: InputStream,
      destination: OutputStream,
      buffer: Array[Byte],
      acc: Long
  ): F[Long] =
    for
      amount <- Async[F].blocking(origin.read(buffer, 0, buffer.length))
      count <-
        (
          if (amount > -1) then
            Async[F].sleep(transferSleepBetweenChunks) >>
              Async[F].blocking(destination.write(buffer, 0, amount)) >>
              transfer(origin, destination, buffer, acc + amount)
          // end of stream reached
          else Async[F].pure(acc)
        )
    yield count

  // Implementing the polymorphic versions of copy functions below
  def inputStream[F[_]: Sync](f: File): Resource[F, FileInputStream] =
    Resource.make(Sync[F].blocking(new FileInputStream(f))) { inStream =>
      Sync[F].delay(
        println(s"[release] closing input stream (${f.getPath})")
      ) >>
        Sync[F].blocking(inStream.close()).handleErrorWith(_ => Sync[F].unit)
    }
  def outputStream[F[_]: Sync](f: File): Resource[F, FileOutputStream] =
    Resource.make(Sync[F].blocking(new FileOutputStream(f))) { outStream =>
      Sync[F].delay(
        println(s"[release] closing output stream (${f.getPath})")
      ) >>
        Sync[F].blocking(outStream.close()).handleErrorWith(_ => Sync[F].unit)
    }
  def inputOutputStreams[F[_]: Sync](
      in: File,
      out: File
  ): Resource[F, (InputStream, OutputStream)] =
    for
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    yield (inStream, outStream)

  def copy[F[_]: Async](origin: File, destination: File): F[Long] =
    inputOutputStreams(origin, destination).use {
      case (in, out) => transfer(in, out, new Array[Byte](1024 * 10), 0L)
    }

/** TODO: Pending exercise: recursive folder copy (cancelable).
  *
  * Create a program that copies folders; if the origin has nested folders, copy
  * recursively. Cancellation should remain safe at any point (`Resource`, sleep
  * between chunks vs `IO.sleep` tradeoffs apply as with this tutorial).
  */
