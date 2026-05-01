import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue
import scala.concurrent.duration.DurationInt
import cats.effect.std.AtomicCell

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
  *
  * By the way, you may be tempted to speed up the `queueR.modify` call in the
  * consumer by using a mutable `Queue` instance. Do not! as `Ref` must be used
  * with immutable data only!
  *
  * Potential Solutions
  *
  * A) Make the producer artifically slower -> by introducing a call to
  * `Async[F].sleep` (e.g. for 1 microsecond). In a real world example, a
  * producer will not be as fast as our example btw. Note that to be able to use
  * `sleep` now `F` requires an implicit `Async[F]` instance.
  *
  * B) Replace `Ref` with `AtomicCell` to keep the `Queue` instance.
  * `AtomicCell` like `Ref` is a concurrent data structure to keep a reference
  * to some data. But unlike `Ref`, it ensures that only 1 fiber can operate on
  * that reference at any given time. Thus the consumer won't have to try once
  * and again to modify its content. `AtomicCell` is slower than `Ref`, as it
  * blocks calling fibes to ensure only one operates on its content. `Ref` is
  * nonblocking for fibers.
  *
  * C) Make the queue bound by size so producers are forced to wait for
  * consumers to extract data when the queue is full. This bounded example will
  * be shown at end
  *
  * Issue 2: Consumer runs even if there are no elements in the queue
  *
  * The consumer will be continually running regardless if there are elements in
  * the queue, which is far from ideal. If we have several consumers competing
  * for the data the problem gets even worse.
  *
  * Potential Solutions
  *
  * A) Using `Deferred` (we will see this at the end as well)
  */
object InefficientAtomicSleepProducerConsumer extends IOApp:

  def producer[F[_]: Async: Console](
      queueR: AtomicCell[F, Queue[Int]],
      counter: Int
  ): F[Unit] =
    for
      _ <- Async[F].whenA(counter % 10000 == 0)(
        Console[F].println(s"Produced $counter items")
      )
      _ <- Async[F].sleep(1.microsecond) // To prevent overwhelming consumers
      _ <- queueR.getAndUpdate(_.enqueue(counter + 1))
      _ <- producer(queueR, counter + 1)
    yield ()

  def consumer[F[_]: Sync: Console](queueR: AtomicCell[F, Queue[Int]])
      : F[Unit] =
    for
      iO <- queueR.modify { queue =>
        queue.dequeueOption.fold((queue, Option.empty[Int])) {
          case (i, queue) => (queue, Option(i))
        }
      }
      _ <- Sync[F].whenA(
        iO.exists(_ % 10000 == 0)
      )(Console[F].println(s"Consumed ${iO.get} items"))
      _ <- consumer(queueR)
    yield ()

  override def run(args: List[String]): IO[ExitCode] =
    for
      queueR <- AtomicCell[IO].of(Queue.empty[Int])
      res <- (consumer(queueR), producer(queueR, 0))
        .parMapN((_, _) =>
          ExitCode.Success
        )
        // Run producer and consumer in parallel until done (likely by user
        // cancelling with CTRL-C)
        .handleErrorWith { t =>
          Console[IO].errorln(
            s"Error caught: ${t.getMessage}"
          ).as(ExitCode.Error)
        }
    yield res

/** A more solid implementation of the producer/consumer pattern
  * -------------------------------------------------------------
  *
  * Instead of using `Option` to represent elements retrieved from a possibly
  * empty queue, we should instead block the caller fiber somehow if queue is
  * empty until some element can be returned. This prevents having consumer
  * fibers running when there is no element to consume. This is done by creating
  * and keeping instances of `Deferred`.
  *
  * A `Deferred[F, A]` instance can hold one single element of some type `A`.
  * Deferred instances are created empty, and can be filled only once. If some
  * fiber tries to read the element from an empty `Deferred` then it will wait
  * until some other fiber fills (completes) it. But recall this waiting doesn't
  * block any physical thread. THE BEAUTY OF FIBERS!
  *
  * Also we will improve our code to handle several producers/consumers in
  * parallel.
  *
  * Ok so knowing this we have to keep track of different things:
  *
  *   - Queue[Int] -> queue of produced but not yet consumed elements)
  *   - Queue[Deferred[F, A]] -> new queue of Deferred instances (created
  *     because consumers found an empty queue) that are waiting for elements to
  *     be available
  *
  * We will hold these 2 in a new type called `State`.
  *
  * Both producer and consumer will access the same shared state instance, which
  * will be carried and safely modified by an instance of `Ref`. Consumer will
  * work as follows:
  *   - If `Queue` is not empty, it will extract and return its head. The new
  *     state will keep the tail of the queue, no deferred hence no change on
  *     `takers`
  *   - If `Queue` is empty it will use a new `Deferred` instance as a new
  *     `taker`, add it to the `takers` queue, and *'block' the caller by
  *     invoking `taker.get`
  *
  * Multiple producer - consumer system using an unbounded concurrent queue
  */
object ProducerConsumer extends IOApp:
  final case class State[F[_], A](
      queue: Queue[A],
      takers: Queue[Deferred[F, A]]
  )

  object State:
    def empty[F[_], A]: State[F, A] = State(Queue.empty, Queue.empty)

  def consumer[F[_]: Async: Console](
      id: Int, // only used to identify the consumer in logs (multiple consumers now)
      stateR: Ref[F, State[F, Int]]
  ): F[Unit] =
    val take: F[Int] =
      Deferred[F, Int].flatMap { taker =>
        stateR.modify {
          case State(queue, takers) if queue.nonEmpty =>
            // Got element in queue
            val (i, rest) = queue.dequeue
            (State(rest, takers), Async[F].pure(i))
          case State(queue, takers) =>
            // No element in queue, must block caller until some is available
            (State(queue, takers.enqueue(taker)), taker.get)
        }.flatten
      }

    for
      i <- take
      _ <- Async[F].whenA(i % 10000 == 0)(
        Console[F].println(s"Consumer $id has reached $i items")
      )
      - <- consumer(id, stateR)
    yield ()

  def producer[F[_]: Async: Console](
      id: Int, // only used to identify the producer in logs (multiple producers now)
      counterR: Ref[F, Int],
      stateR: Ref[F, State[F, Int]]
  ): F[Unit] =

    def offer(i: Int): F[Unit] =
      stateR.modify {
        case State(queue, takers) if takers.nonEmpty =>
          val (taker, rest) = takers.dequeue
          (State(queue, rest), taker.complete(i).void)
        case State(queue, takers) =>
          State(queue.enqueue(i), takers) -> Async[F].unit
      }.flatten

    for
      i <- counterR.getAndUpdate(_ + 1)
      _ <- offer(i)
      _ <- Async[F].whenA(i % 100000 == 0)(
        Console[F].println(s"Producer $id has reached $i items")
      )
      _ <- Async[F].sleep(1.microsecond) // To prevent overwhelming consumers
      _ <- producer(id, counterR, stateR)
    yield ()

  /** Producers and consumers are created as two List[IO[...]]. All of them are
    * started in their own fiber by the call to `parSequence`, which will wait
    * for all of them to finish and then return the value. Runs forever until
    * CTRL-C
    */
  def run(args: List[String]): IO[ExitCode] =
    for
      stateR <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int])
      counterR <- Ref.of[IO, Int](1)
      // 10 producers and 10 consumers
      producers = List.range(1, 11).map(producer(_, counterR, stateR))
      consumers = List.range(1, 11).map(consumer(_, stateR))
      res <- (producers ++ consumers)
        .parSequence.as(ExitCode.Success)
        .handleErrorWith(t =>
          Console[IO].errorln(
            s"Error caught: ${t.getMessage}"
          ).as(ExitCode.Error)
        )
    yield res

/** Producer Consumer with bounded queue */
