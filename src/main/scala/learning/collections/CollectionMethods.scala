package learning.collections

/*
A great strength of Scala collections is that they come with dozens
of methods out of the box, and those methods are consistently available
across the immutable and mutable collections types.

some of the most commonly-used methods are:
    - map
    - filter
    - foreach
    - head
    - tail
    - take, takeWhile
    - drop, dropWhile
    - reduce

Those methods work on all of the sequence types, including
List, Vector, ArrayBuffer, etc. Examples here show List.
As a very important note, none of the methods on List mutate the list.
They return a new collection with the modified results
 */

def collectionMethodsExample() =
  val a = List(10, 20, 30, 40, 10) // List(10, 20, 30, 40, 10)
  // First, here are some methods that don’t use lambdas
  a.distinct // List(10, 20, 30, 40)
  a.drop(2) // List(30, 40, 10)
  a.dropRight(2) // List(10, 20, 30)
  a.head // 10
  a.headOption // Some(10)
  a.init // List(10, 20, 30, 40)
  a.intersect(List(19, 20, 21)) // List(20)
  a.last // 10
  a.lastOption // Some(10)
  a.slice(2, 4) // List(30, 40)
  a.tail // List(20, 30, 40, 10)
  a.take(3) // List(10, 20, 30)
  a.takeRight(2) // List(40, 10)

  /*
    Higher-order functions and lambdas
    ----------------------------------
    Next, well show some commonly used HOFs that acept lambdas.
    Here are several variations of lambda syntax, longest -> concise form

    It’s important to note that HOFs also accept methods and functions
    as parameters—not just lambda expressions. some examples below use
    a method named double. Several variations of the lambda syntax are shown
   */

  // these functions are all equivalent and return
  // the same data: List(10, 20, 10)

  a.filter((i: Int) => i < 25) // 1. most explicit form
  a.filter((i) => i < 25) // 2. `Int` is not required
  a.filter(i => i < 25) // 3. the parens are not required
  a.filter(_ < 25) // 4. `i` is not required

  // Other HOFs examples

  /*
    dropWhile: remove elements from the *front* only, while the predicate is true.
    Stops at the first element where the predicate is false; keeps that element and everything after.
    Here a = List(10, 20, 30, 40, 10): 10 and 20 are < 25 so they are dropped; 30 is not < 25, so
    the prefix stops — result List(30, 40, 10). (Contrast: filter removes *all* elements < 25 everywhere.)
   */
  a.dropWhile(_ < 25) // List(30, 40, 10)
  a.filter(_ > 100) // List()
  a.filterNot(_ < 25) // List(30, 40)
  a.find(_ > 20) // Some(30)
  /*
    takeWhile: keep elements from the *front* only, while the predicate is true.
    Stops at the first element where the predicate is false; that element and the rest are *not* included.
    Here: 10 and 20 are < 30, so they are kept; 30 is not < 30, so we stop — result List(10, 20).
    (Opposite of dropWhile: takeWhile keeps the prefix that matches; dropWhile discards that prefix.)
   */
  a.takeWhile(_ < 30) // List(10, 20)

  def double(i: Int) = i * 2
  // these all return `List(20, 40, 60, 80, 20)`
  a.map(i => double(i))
  a.map(double(_))
  a.map(double)

  // you can combine
  a.filter(_ < 40).takeWhile(_ < 30).map(_ * 10) // `List(100, 200)`

val oneToTen = (1 to 10).toList
val names = List("adam", "brandy", "chris", "david")

/** **************************************************************** `map` The
  * map method steps through each element in the existing list, applying the
  * function you supply to each element, one at a time; it then returns a new
  * list with all of the modified elements.
  * ------------------------------------------------------------------
  */

def collectionMethodsMapExample() =
  val doubles = oneToTen.map(_ * 2)
  // doubles: List[Int] = List(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
  val capNames = names.map(_.capitalize)
  // capNames: List[String] = List(Adam, Brandy, Chris, David)
  val nameLengthsMap = names.map(s => (s, s.length)).toMap
  println(nameLengthsMap)
  // nameLengthsMap: Map[String, Int] = Map(adam -> 4, brandy -> 6, chris -> 5, david -> 5)
  val isLessThanFive = oneToTen.map(_ < 5)
  // isLessThanFive: List[Boolean] = List(true, true, true, true, false, false, false, false, false, false)

/** ************************************************************************
  * `filter`
  *
  * creates a new list containing the elements that satisfy the provided
  * predicate A predicate, or condition, is a function that returns a Boolean
  * (true or false)
  * ----------------------------------------------------------------------------
  */

def collectionMethodsFilterExample() =
  val lessThanFive = oneToTen.filter(_ < 5)
  // lessThanFive: List[Int] = List(1, 2, 3, 4)
  val evens = oneToTen.filter(_ % 2 == 0)
  // evens: List[Int] = List(2, 4, 6, 8, 10)
  val shortNames = names.filter(_.length <= 4)
  println(shortNames)
  // shortNames: List[String] = List(adam)
  val chainedWithMap = oneToTen.filter(_ < 4).map(_ * 10)
  println(chainedWithMap)
  // List[Int] = List(10, 20, 30)

/** ************************************************************************
  * `foreach`
  *
  * used to loop over all elements in a collection. Note that foreach is used
  * for side-effects, such as printing information
  * ----------------------------------------------------------------------------
  */

def collectionMethodsforeachExample() =
  names.foreach(println)
  oneToTen.foreach(println)

/** ************************************************************************
  * `head`
  *
  * comes from Lisp and other earlier functional programming languages. It’s
  * used to access the first element (the head element) of a list.
  *
  * head is a great method to work with, but as a word of caution it can throw
  * an exception when called on an empty collection.
  *
  * Because of this you may want to use headOption instead of head, especially
  * when programming in a functional style.
  *
  * headOption simply returns the type Option that has the value None
  * ----------------------------------------------------------------------------
  */

def collectionMethodsheadExample() =
  println(oneToTen.head) // 1
  println(names.head) // adam
  println("foo".head) // 'f'
  println("bar".head) // 'b'

  val emptyList = List[Int]() // emptyList: List[Int] = List()

  try emptyList.head // throw an exception when called on an empty collection
  catch
    case ne: java.util.NoSuchElementException => println(ne.toString)
    case _                                    => println("Unknown error")

  // Because of this you may want to use headOption instead of head,
  // especially when programming in a functional style

  emptyList.headOption // None

/** ************************************************************************
  * `tail`
  *
  * comes from Lisp and other earlier functional programming languages. used to
  * get every element in a list after the head element.
  *
  * tail throws a java.lang.UnsupportedOperationException if the list is empty.
  *
  * no option like headOption for tail.
  *
  * To safely work with the tail of a collection, you can use pattern matching
  * or methods such as drop(1)
  * ----------------------------------------------------------------------------
  */

def collectionMethodstailExample() =
  println(oneToTen.head) // 1
  println(oneToTen.tail) // List(2, 3, 4, 5, 6, 7, 8, 9, 10)
  println(names.head) // adam
  println(names.tail) // List(brandy, chris, david)
  println("foo".tail) // "oo"
  println("bar".tail) // "ar"

  /*
    Pattern x :: xs means: “this list is non-empty; the first element is x, the rest is xs.”
        - same information as names.head and names.tail, but written as a pattern so
          you can bind both names in one step.
    A List is built from :: (“cons”) and Nil.
    `::` is the “cons” pattern: same structure as writing List("a","b") as "a" :: "b" :: Nil.
    `: @unchecked` is used to remove warning because `List` could be empty, here not the case
   */
  val x :: xs = names: @unchecked
  // val x: String = adam
  // val xs: List[String] = List(brandy, chris, david)

  /*
    Same pattern in recursion (from the Scala 3 Book): peel off one element until the list is empty.
       - `Nil` → nothing left, base case (sum of empty list is 0).
       - `x :: xs` → first element is `x`, rest is `xs`; add `x` to the sum of `xs`.
   */
  def sum(list: List[Int]): Int = list match
    case Nil     => 0
    case x :: xs => x + sum(xs)

/** ************************************************************************
  * `take, takeRight, takeWhile`
  *
  *   - a nice way of “taking” the elements from a list that you want for new
  *     list.
  *
  * ----------------------------------------------------------------------------
  * *************************************************************************
  */

def collectionMethodstakeExample() =
  oneToTen.take(1) // List(1)
  oneToTen.take(2) // List(1, 2)
  oneToTen.takeRight(1) // List(10)
  oneToTen.takeRight(2) // List(9, 10)

  // Allowed to work with edge cases or ask for zero elements
  oneToTen.take(Int.MaxValue) // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  oneToTen.takeRight(Int.MaxValue) // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  oneToTen.take(0) // List()
  oneToTen.takeRight(0) // List()

  // takeWhile works with a predicate function
  println(oneToTen.takeWhile(_ < 5)) // List(1, 2, 3, 4)
  println(names.takeWhile(_.length < 5)) // List(adam)

/** ************************************************************************
  * `drop, dropRight, dropWhile`
  *
  *   - a nice way of "dropping" the elements from a list to build a new list
  *
  * ----------------------------------------------------------------------------
  * *************************************************************************
  */

def collectionMethodsdropExample() =
  oneToTen.drop(1) // List(2, 3, 4, 5, 6, 7, 8, 9, 10)
  oneToTen.drop(5) // List(6, 7, 8, 9, 10)

  oneToTen.dropRight(8) // List(1, 2)
  oneToTen.dropRight(7) // List(1, 2, 3)

  // Allowed to work with edge cases or ask for zero elements
  oneToTen.drop(Int.MaxValue) // List()
  oneToTen.dropRight(Int.MaxValue) // List()
  oneToTen.drop(0) // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  oneToTen.dropRight(0) // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // dropWhile works with a predicate function
  println(oneToTen.dropWhile(_ < 5)) // List(5, 6, 7, 8, 9, 10)
  println(names.dropWhile(_ != "chris")) // List(chris, david)

/** ************************************************************************
  * `reduce`
  *
  *   - “map reduce” the “reduce” part refers to methods like reduce
  *   - takes a function (or lambda) and applies that function to successive
  *     elements in the list
  *   - Best way to explain reduce is to create a little helper method you can
  *     pass into it
  *   - An important concept to know about reduce is that its used to reduce a
  *     collection down to a single value.
  *     ----------------------------------------------------------------------------
  *     *************************************************************************
  */

def add(x: Int, y: Int): Int =
  val theSum = x + y
  println(s"received $x and $y, their sum is $theSum")
  theSum

val a = List(1, 2, 3, 4)

// This is what happens when you pass the add method to reduce

def collectionMethodsreduceExample() =
  val res = a.reduce(add) // 10
  // received 1 and 2, their sum is 3
  // received 3 and 3, their sum is 6
  // received 6 and 4, their sum is 10

  /*
    As that result shows, reduce uses add to reduce the list a into a single value,
    in this case, the sum of the integers in the list.

    Once you get used to reduce, you’ll write a “sum” algorithm like this:
   */

  val resSum = a.reduce(_ + _) // 10
  println(s"$resSum is the reduce result for $a using List.reduce(_ + _)")

  // Product algorithm
  val resProduct = a.reduce(_ * _) // 24
  println(s"$resProduct is the reduce result for $a using List.reduce(_ * _)")

/** ************************************************************************
  * `groupBy`, `view`, and `mapValues`
  *
  * - `groupBy`: Groups a collection into a Map of "buckets" based on a function.
  * - `view`: Creates a lazy view of the collection. Operations are not evaluated 
  *   immediately, avoiding unnecessary intermediate collections.
  * - `mapValues`: A specialized map for Maps that only transforms the values, 
  *   leaving keys untouched. In Scala 3, it is most efficient when used on a `view`.
  * ----------------------------------------------------------------------------
  */
def collectionMethodsGroupingExample() =
  val list = List("apple", "apricot", "banana", "blueberry", "cherry")
  
  // Group by the first letter
  val grouped: Map[Char, List[String]] = list.groupBy(_.head)
  // Map(a -> List(apple, apricot), b -> List(banana, blueberry), c -> List(cherry))

  // Transform the values (the lists) into their sizes
  // We use .view to avoid creating an intermediate Map before calling .toMap
  val counts: Map[Char, Int] = grouped.view.mapValues(_.size).toMap
  // Map(a -> 2, b -> 2, c -> 1)

/** ************************************************************************
  * `count`
  *
  * - Returns the number of elements in the collection that satisfy a predicate.
  * - It's a more concise way of doing `.filter(p).size`.
  * ----------------------------------------------------------------------------
  */
def collectionMethodsCountExample() =
  val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  
  // Count how many numbers are even
  val evenCount = numbers.count(_ % 2 == 0) // 5
  
  val fruit = List("apple", "banana", "cherry", "date")
  // Count how many fruits have more than 5 characters
  val longFruitCount = fruit.count(_.length > 5) // 2 (banana, cherry)

/** *************************************************************************
  * SUMMARY
  *
  * There are literally dozens of additional methods on the Scala collections
  * types that will keep you from ever needing to write another for loop.
  *   - https://docs.scala-lang.org/overviews/collections-2.13/overview.html
  *   - https://docs.scala-lang.org/overviews/core/architecture-of-scala-213-collections.html
  *
  * As a final note, if you’re using Java code in a Scala project, you can
  * convert Java collections to Scala collections. By doing this you can use
  * those collections in `for` expressions, and can also take advantage of
  * Scala’s functional collections methods.
  *
  * See the Interacting with Java section for more details.
  *   - https://docs.scala-lang.org/scala3/book/interacting-with-java.html
  *     **************************************************************************
  */
