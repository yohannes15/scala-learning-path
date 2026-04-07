package learning.types

/** **********************************************************

Variance

Type parameter `variance` controls the typing of parameterized types (like
classes or traits).

In general there are three modes of variance:

* `invariant` — the default, written like Pipeline[T] 
* `covariant` — annotated with a +, such as Producer[+T]
* `contravariant` —annotated with a -, like in Consumer[-T]

Producers are typically covariant, and mark their type parameter with +. 
This also holds for immutable collections.

Consumers are typically contravariant, and mark their type parameter with -.

Types that are both producers and consumers have to be invariant, 
and do not require any marking on their type parameter. 
Mutable collections like Array fall into this category.

  */

/**********************************************************************
***********************************************************************
Notation: `<:` (subtype)

`A <: B` means: **A is a subtype of B** — every value of type A can be used
where a B is required (A “is a” B, or extends / implements B).

Example below: `Book <: Buyable <: Item` reads as a chain:
  Book is a subtype of Buyable, and Buyable is a subtype of Item.

Related: `B >: A` is the same fact from the other side (**B is a supertype of A**).

For **simple** types like `Book` and `Buyable`, `A <: B` is easy to read. For
**parameterized** types like `Consumer[Item]` and `Consumer[Buyable]`, the same
symbol between the *whole* types is correct in Scala but **looks** as if it were
about `Item` vs `Buyable`. In the Contravariant section we mostly say **subtype /
supertype in words** instead of `Consumer[…] <: Consumer[…]`.
***********************************************************************
**********************************************************************/

// Lets assume the below type definitions
trait Item { def productNumber: String }
trait Buyable extends Item { def price: Int }
trait Book extends Buyable { def isbn: String }

/**********************************************************
***********************************************************
invariant type
--------------------
Pipeline is invariant in its type argument (T). This means types
like Pipeline[Item], Pipeline[Buyable], Pipeline[Book] are in no
subtyping relationship to eachother.

And rightfully so! Assume the below method `oneOf` that consumes
two values of type Pipeline[Buyable], and passes its argument b to
one of them, based on the price.

Now, recall that we have the following subtyping relationship
between our types:

  Book <: Buyable <: Item

We cannot pass a Pipeline[Book] to the method oneOf because in
its implementation, we call p1 and p2 with a value of type Buyable.
A Pipeline[Book] expects a Book, which can potentially cause a
runtime error.

We cannot pass a Pipeline[Item] because calling process on it
only promises to return an Item; however, we are supposed to return
a Buyable.

Why called invariant?

T appears in both *argument* and *return* positions **invariant**
is required. Same idea as `Array[T]` and `Set[T]` in the standard
library
 */
trait Pipeline[T]:
  def process(t: T): T

/** Picks the cheaper result of running the same `Buyable` through two
  * pipelines.
  */
def oneOf(
    p1: Pipeline[Buyable],
    p2: Pipeline[Buyable],
    b: Buyable
): Buyable =
  val b1 = p1.process(b)
  val b2 = p2.process(b)
  if b1.price < b2.price then b1 else b2

/** Sample data and `Pipeline[Buyable]` values for `invariantPipelineExample`.
  */
object InvariantPipelineDemo:
  case class Shoppable(productNumber: String, price: Int) extends Buyable
  case class Novel(productNumber: String, price: Int, isbn: String) extends Book

  /*
  Returns its input unchanged (still a legal `Pipeline[Buyable]`).
  anonymous concrete class that implements the trait. identity is of
  type Pipeline[Buyable]
  */
  val identity: Pipeline[Buyable] = new Pipeline[Buyable]:
    def process(t: Buyable): Buyable = t

  /* 
  Returns a `Buyable` with a small markdown price — result stays a `Buyable`. 
  */
  val markdown: Pipeline[Buyable] = new Pipeline[Buyable]:
    def process(t: Buyable): Buyable = 
      Shoppable(t.productNumber, math.max(0, t.price - 5))

/** Demo of `oneOf` + invariant `Pipeline[Buyable]`.
  *
  *   - We start with a `Novel` (a `Book`, hence a `Buyable`) at price 20.
  *   - `identity` is a pipeline that returns its input unchanged.
  *   - `markdown` is a pipeline that returns a `Shoppable` copy with `price - 5` (minimum 0).
  *   - `oneOf(p1, p2, b)` feeds the *same* `b` into both pipelines, then compares the two
  *     *outputs* by `.price` and keeps whichever is **cheaper** (strictly lower price wins).
  *     Here: 15 < 20, so the markdown result wins—not because we “prefer” the second pipeline,
  *     but because its output has the lower price.
  */
def invariantPipelineExample(): Unit =
  import InvariantPipelineDemo.*
  val book = Novel("SKU-42", price = 20, isbn = "978-0")
  val out1 = identity.process(book)
  val out2 = markdown.process(book)

  println("--- invariant Pipeline[Buyable] / oneOf ---")
  println(s"1) Input b (one Buyable shared by both pipelines): $book  price=${book.price}")
  println(s"2) p1 = identity  => p1.process(b) = $out1  price=${out1.price}  (unchanged)")
  println(s"3) p2 = markdown  => p2.process(b) = $out2  price=${out2.price}  (Shoppable, price - 5)")

  val cheaper = oneOf(identity, markdown, book)
  println(s"4) oneOf(identity, markdown, book) == $cheaper  price=${cheaper.price}")

/**********************************************************************
***********************************************************************
Covariant Types

Marked as covariant by prefixing the type parameter with a `+`
This is valid since the type parameter is only used in a return position

Marking it as covariant means that we can pass or return a Producer[Book]
where a Producer[Buyable] is expected (Producer[Buyable] <: Producer[Item]). 
This is valid. The type of Producer[Buyable].make only promises to return 
a Buyable. As a caller of `make`, we will be happy to also accept a Book, 
which is a subtype of Buyable - that is, it is at least a Buyable.

You will encounter covariant types a lot when dealing with immutable
containers, like List, Seq, Vector ...
  `class List[+A] ...`
  `class Vector[+A] ...`

This way you can use a List[Book] where a List[Buyable] is expected.
It also makes sense intuitively. If you are expecting a collection of 
things that can be bought, it should be fine to give you a collection of
books. They have an additional ISBN method in our example, but you are 
free to ignore these additional capabilities
***********************************************************************
**********************************************************************/
trait Producer[+T]:
  def make: T

/** Uses `Producer[Buyable]`: `make` must return *at least* a `Buyable` (has `.price`). */
def makeTwo(p: Producer[Buyable]): Int =
  p.make.price * 2

/** `Producer[+T]` is **covariant**: `Producer[Book] <: Producer[Buyable]` because `Book <: Buyable`.
  * So you may pass a `Producer[Book]` wherever a `Producer[Buyable]` is expected — `make` may return
  * a more specific type; callers who only need `Buyable` are still safe.
  * The call to price within makeTwo is still valid also for books.
  */
def covariantExample(): Unit =
  import InvariantPipelineDemo.Novel

  val bookProducer: Producer[Book] = new Producer[Book]:
    def make: Book = Novel("cov-demo", price = 9, isbn = "978-covariant")

  val doubled = makeTwo(bookProducer)

  println(s"makeTwo(bookProducer) => bookProducer.make.price * 2 == 9 * 2 == $doubled")

/**********************************************************************
***********************************************************************
Why covariance vs contravariance “flips” (same chain Book <: Buyable <: Item)

**Outputs (+T, covariant)** — `make` **returns** `T` to the caller.
The caller needs “at least a Buyable.” If the implementation actually
returns a Book, that is still a Buyable — **more specific is OK**.
So when `Book <: Buyable`, **Producer[Book] is a subtype of Producer[Buyable]**
(the **same direction** as the element subtyping for producers).

**Inputs (-T, contravariant)** — `take` **accepts** `T` from the caller.
The callee must be ready for **every** value of that type. A consumer
that only accepts **Book** cannot replace one that must accept **any**
Buyable (callers may pass a non-book Buyable). But a consumer that
accepts **any Item** can accept every Buyable (a Buyable is an Item).

So among `Consumer[X]` types, the order is the **reverse** of 
`Book <: Buyable <: Item`: 
  
**Consumer[Item] <: Consumer[Buyable]** 
— wider *accepted* input (`Item`) makes a *narrower* consumer type
  that can stand in for a *wider* obligation (`Buyable`).

Mnemonic: **pro**ducer, **+**, value **out** → follows the chain.
**Con**sumer, **-**, value **in** → chain is reversed for the wrapper type.
***********************************************************************
**********************************************************************/

/**********************************************************************
***********************************************************************
Contravariant Types

Marked as Contravariant by marking the type parameter with a `-`
This is valid, since the type parameter is only used in an argument position.

Marking it as contravariant means that we can pass (or return) a 
Consumer[Item] where a Consumer[Buyable] is expected. That is, we have 
the subtyping relationship Consumer[Item] <: Consumer[Buyable]. 
Remember, for type Producer, it was the other way around, and we had 
Producer[Buyable] <: Producer[Item].

**In words (no bracket confusion):** the type **Consumer[Item] is a subtype of**
the type **Consumer[Buyable]** — wherever a `Consumer[Buyable]` is required, you
may pass a `Consumer[Item]`. That does **not** say “Item is a subtype of Buyable.”
For *values* it is still **Buyable <: Item** (every Buyable is an Item). The type
parameter is in an **input** position, so the **Consumer[…]** subtyping order flips
relative to the value chain.

For Producer it is the other way around: **Producer[Buyable] is a subtype of**
**Producer[Item]** when `Buyable <: Item` (outputs follow the value chain).

And in fact, this is sound. The method Consumer[Item].take accepts an Item. 
As a caller of take, we can also supply a Buyable, which will be happily 
accepted by the Consumer[Item] since Buyable is a subtype of Item—that is, 
it is at least an Item.

Contravariant types are much less common than covariant types. 
As in our example, you can think of them as “consumers.” 

More on the same value chain `Book <: Buyable <: Item` — for Consumer types,
from **subtype toward supertype**: **Consumer[Item]**, then **Consumer[Buyable]**,
then **Consumer[Book]** (each is a subtype of the next).
So the consumer that accepts the **widest** inputs (any Item) is the **most
specific** Consumer type here.
A Consumer[Book] cannot safely stand in for Consumer[Buyable]: callers might
pass a Buyable that is not a Book.

Concrete code below: feedBuyable(c: Consumer[Buyable], ...) and passing
logSku: Consumer[Item] where Consumer[Buyable] is expected.
***********************************************************************
**********************************************************************/
trait Consumer[-T]:
  def take(t: T): Unit

/* API that only hands Buyables to the consumer — any Consumer[Buyable] is OK,
 * and Consumer[Item] is OK too (subtype). */
def feedBuyable(c: Consumer[Buyable], b: Buyable): Unit =
  c.take(b)

object ContravariantDemo:
  /*
  Consumer[Item] — implementation only needs Item members (productNumber).
  Safe when the call site passes Buyable: every Buyable is an Item.
  */
  val logSku: Consumer[Item] = new Consumer[Item]:
    def take(t: Item): Unit =
      println(s"  [Consumer[Item]] saw productNumber=${t.productNumber}")

  /*
  Consumer[Buyable] — uses .price. Not replaceable by Consumer[Book] if callers
  might pass a non-book Buyable (e.g. Shoppable).
  */
  val logPrice: Consumer[Buyable] = new Consumer[Buyable]:
    def take(t: Buyable): Unit =
      println(s"  [Consumer[Buyable]] price=${t.price}")

/* 
important type that you might come across that is marked contravariant:

Its argument type `A` is marked as contravariant A 
  —> it consumes values of type A. 
In contrast, its result type B is marked as covariant
  -> it produces values of type B.

Same idea as scala.Function1[-A, +B]: inputs vary contravariantly, outputs
covariantly. This file defines a small Function trait for teaching; use
`new Function[...]{ def apply ... }` in examples — plain `x => x` is the
library Function1, not this trait.

Here are some examples that illustrate the subtyping relationships 
induced by variance annotations on functions: 
*/
trait Function[-A, +B]:
  def apply(a: A): B

def contravariantExample(): Unit =
  import InvariantPipelineDemo.*
  import ContravariantDemo.*

  val gadget = Shoppable("G-100", price = 7)

  println("Consumer[Item] is a subtype of Consumer[Buyable] (contravariant flip vs value chain).")
  println("feedBuyable wants Consumer[Buyable]; logSku is Consumer[Item] — allowed.")
  print("feedBuyable(logSku, gadget): ")
  feedBuyable(logSku, gadget)
  print("feedBuyable(logPrice, gadget): ")
  feedBuyable(logPrice, gadget)
  println()

  println("--- Function[-A, +B] (variance like Scala function types) ---")
  val f: Function[Buyable, Buyable] = new Function[Buyable, Buyable]:
    def apply(a: Buyable): Buyable = a

  /* OK to treat result as Item: covariant in B; Buyable <: Item */
  val g: Function[Buyable, Item] = f

  /* OK to require Book at call site: contravariant in A; Book <: Buyable */
  val h: Function[Book, Buyable] = f

  val book = Novel("N-1", 20, "978-fn")
  println(s"f.apply(gadget) == $gadget")
  println(s"g: widened to Function[Buyable, Item], same runtime value: ${g.apply(gadget)}")
  println(s"h: narrowed argument to Book, apply(book): ${h.apply(book)}")

// TODO: Continue from Opaque Types https://docs.scala-lang.org/scala3/book/types-opaque-types.html
