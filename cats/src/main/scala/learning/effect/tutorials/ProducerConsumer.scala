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
  *
 */
