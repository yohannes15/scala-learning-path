import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue

/** Producer-Consumer (Concurrency and Fibers):
  *
  * The producer-consumer pattern is often found in concurrent setups. Here one
  * or more producers insert data on a shared data structure like a queue while
  * one or more consumers extract data from it. Readers and writers run
  * concurrently. If the queue is empty then readers will block until data is
  * available, if the queue is full then writers will wait for some 'bucket' to
  * be free. Only one writer at a time can add data to the queue to prevent data
  * corruption. Also only one reader can extract data from the queue so no two
  * readers get the same data item.
  *
  * Variations of this problem exist depending on whether there are more than
  * one consumer/producer, or whether the data structure sitting between them is
  * size-bounded or not. The solutions discussed here are suited for
  * multi-consumer and multi-reader settings. Initially we will assume an
  * unbounded data structure, and later present a solution for a bounded one.
  */

/** First (and inefficient) implementation
  * --------------------------------------------------------------------------
  * Lets assume a simple `Queue`. Initially there will be only one producer and
  * one consumer. Producer will generate a sequence of integers `{1, 2, 3...}`,
  * consumer will just read that sequence. Our shared queue will be instance of
  * immutable `Queue[Int]`
  *
  * Accesses to the queue can (and will!) be concurrent, thus we need some way
  * to protect the queue so only one fiber at a time is handling it. This is a
  * good case for `Ref` (ensures an ordered access to some shared data). A `Ref`
  * instance wraps some given data and implements methods to manipulate that
  * data in a safe manner.
  *
  * The `Ref` wrapping our Queue will be `Ref[F, Queue[Int]]` for Some F[_]
  */
object InefficientProducerConsumer extends IOApp:

  /** Instantiates the shared queue wrapped in a Ref and boots the producer and
    * consumer in parallel. Uses parMapN, that creates and runs the fibers that
    * will run the IOs passed as parameter. Then it takes the output of each
    * fiber and applies a given function to them. In our case both producer and
    * consumer shall run forever until the user presses CTRL-C which will
    * trigger a cancelation.
    *
    * `parMapN` promotes any error it finds to the caller and takes care of
    * canceling the other running fibers. As a result parMapN is simpler to use,
    * more concise, and easier to reason about than using `start/join` as seen
    * in the `runStartJoin` method below. Because of that, unless you have some
    * specific and unusual requirements you should prefer to use higher level
    * commands such as parMapN or parSequence to work with fibers.
    */
  def run(args: List[String]): IO[ExitCode] =
    for
      queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      res <- (consumer(queueR), producer(queueR, 0))
        // run producer/consumer in parallel until done (stop by Ctrl + C)
        .parMapN((_, _) => ExitCode.Success)
        .handleErrorWith {
          t =>
            Console[IO].errorln(
              s"Error caught: ${t.getMessage}"
            ).as(ExitCode.Error)
        }
    yield res

  /** Alternatively we could have used `start` method to explicitly create new
    * Fiber instances that will run the producer and consumer, then use `join`
    * to wait for them to finish. However it is not adviseable to handle fibers
    * manually as they are not trivial to work with.
    *
    * For example, if there is an error in a fiber the `join` call to that fiber
    * will not raise it, it will return normally and you must explicitly check
    * the `Outcome` instance returned by the `.join` call to see if it errored.
    * Also, the other fibers will keep running unaware of what happened.
    *
    * Cats Effect provides additional `joinWith` or `joinWithNever` methods to
    * make sure at least that the error is raised with the usual MonadError
    * semantics (i.e., short-circuiting). Now that we are raising the error, we
    * also need to cancel the other running fibers. We can easily get ourselves
    * trapped in a tangled mess of fibers to keep an eye on. On top of that the
    * error raised by a fiber is not promoted until the call to `joinWith` or
    * `.joinWithNever` is reached.
    *
    * So in our example if consumerFiber raises an error then we have no way to
    * observe that until the producer fiber has finished. Alarmingly, note that
    * in our example the producer never finishes and thus the error would never
    * be observed! And even if the producer fiber did finish, it would have been
    * consuming resources for nothing.
    */
  def runStartJoin(args: List[String]): IO[ExitCode] =
    for
      queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      producerFiber <- producer(queueR, 0).start
      consumerFiber <- consumer(queueR).start
      _ <- producerFiber.join
      _ <- consumerFiber.join
    yield ExitCode.Error

  def producer[F[_]: Sync: Console](
      queueR: Ref[F, Queue[Int]],
      counter: Int
  ): F[Unit] =
    for
      // prints some log msg every 10000 items, so we know producer is 'alive'
      // similar to if(cond) then Console... else Sync[F].unit but better
      // Console[_] tc brings capacity to print and read strings (IO.println
      // just invokes Console[IO].println under the hood)
      _ <- Sync[F].whenA(counter % 10000 == 0)(
        Console[F].println(s"Produced $counter items")
      )
      // Add data into queue. getAndUpdate provides the current queue, then
      // .enqueue inserts next value and then returns a new queue with the val
      // added that is stored by the ref instance. If some other fiber is
      // accessing queueR then the fiber (but no thread) is blocked.
      _ <- queueR.getAndUpdate(q => q.enqueue(counter + 1))
      // recursive call
      _ <- producer(queueR, counter + 1)
    yield ()

  def consumer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]]): F[Unit] =
    for
      /** modify allows to modify the wrapped data (our queue), computes a new
        * queue and stores that new queue into the Ref in one atomic step. It
        * then returns a separate result value, in this case, it returns an
        * `Option[Int]` that will be None if queue was empty. F is Option[Int]
        * -> modify[B](f: Queue[Int] => (Queue[Int], B)): F[B]
        */
      iO <- queueR.modify { queue =>
        /** fold here means -> if Option is empty, use default value, else
          * transform it with this function. Shape is like this:
          * `option.fold(defaultValue)(value => resultIfSome)`
          */
        queue.dequeueOption.fold(
          ifEmpty = (queue, Option.empty[Int])
        ) {
          // successfully dequed an item. Return remQueue and Some(value)
          case (i, remQueue) => (remQueue, Option(i))
        }

        /** Fold is a compact replacement for below:
          *
          * {{{
          *    queue.dequeueOption match
          *    case None => (queue, None)
          *    case Some((i, remQueue)) => (remQueue, Some(i))
          * }}}
          */
      }
      // log a message in console every 10000 read items.
      _ <- Sync[F].whenA(
        iO.exists(_ % 10000 == 0)
      )(Console[F].println(s"Consumed ${iO.get} items"))
      // recursive call
      _ <- consumer(queueR)
    yield ()

/** Why is above program Inefficient? What are the Issues?
  * -------------------------------------------------------
  *
  * Issue 1: Producer outpaces the consumer
  *
  * If you run the program you will notice that almost no consumer logs are
  * shown, if any. This is a signal that the producer is running way faster than
  * the consumer. And why is that? Well, this is because how `Ref.modify` works.
  * It gets the current value, then it computes the update, and finally it tries
  * to set the new value if the current one has not been changed (by some other
  * fiber), otherwise it starts from the beginning. Unfortunately the producer
  * is way faster running its `queueR.getAndUpdate` call than the consumer is
  * running its `queueR.modify` call. So the consumer gets 'stuck' trying once
  * and again to update the `queueR` content.
  */
