package learning.collections

// There are both immutable and mutable collections.
// This file shows common operations on immutable `List`. Lists are persistent:
// operations return a new List rather than mutating the original.

////////////////////////////
////////// LIST ////////////
////////////////////////////

/* 
- List in Scala is an immutable, linked-list class.
- Because List is immutable, you generally don't add new elements to it. 
- You can also append elements to a List, but because List is a singly-linked list, 
  you should generally only prepend elements to it; appending elements to it is a 
  relatively slow operation, especially when you work with large sequences.
- If you want to prepend and append elements to an immutable sequence, use Vector instead.
- Because List is a linked-list, you shouldn’t try to access the elements of 
  large lists by their index value
- If you have a large collection and want to access elements by their index, 
  use a Vector or ArrayBuffer instead.
 */


def listCollection(): Unit =
    val simpleList = List(1, 2, 3)

    // another way to create a list
    // similar to the List from the Lisp programming language
    // :: is a List method that works like Lisp’s “cons” operator.
    val names: List[String] = "Joel" :: "Chris" :: "Ed" :: Nil

    // mixed types
    val things: List[String | Int | Double] = List(1, "two", 3.0) // with union types
    val thingsAny: List[Any] = List(1, "two", 3.0)                // with any

    // Adding elements to list 
    val a = List(1,2,3)
    // prepend one element with ::
    val b = 0 :: a              // List(0, 1, 2, 3)
    // prepend another List with :::
    val c = List(-1, 0) ::: a   // List(-1,0,1,2,3)
    /* 
    Another way to add elements is with the : character. It represents the side that 
    the sequence is on, so when you use +: you know that the list needs to be on the right.
    Also, a good thing about these symbolic method names is that they’re consistent. 
    The same method names are used with other immutable sequences, such as Seq and Vector. 
    You can also use non-symbolic method names to append and prepend elements, if you prefer.
    */
    val d = 0 +: a             // List(0, 1, 2, 3)
    val e = a :+ 4             // List(0, 1, 2, 3, 4)

    // Range methods — convenient ways to build lists from ranges.
    val rangeToFive = (1 to 5).toList             // List(1, 2, 3, 4, 5)
    val rangeByTwo  = (1 to 10 by 2).toList       // List(1, 3, 5, 7, 9)
    val untilFive    = (1 until 5).toList          // List(1, 2, 3, 4)
    val range1to5    = List.range(1, 5)            // List(1, 2, 3, 4)
    val rangeStep3   = List.range(1, 10, 3)        // List(1, 4, 7)

    // List Methods — none of these mutate `sampleList`; they return new Lists.
    val sampleList = List(10, 20, 30, 40, 10)    // example list

    sampleList.drop(2)           // List(30, 40, 10)     // drops first 2 elements
    sampleList.dropWhile(_ < 25) // List(30, 40, 10)     // drops while condition true
    sampleList.filter(_ < 25)    // List(10, 20, 10)     // keeps elements matching predicate
    sampleList.slice(2, 4)       // List(30, 40)         // elements in index range [2,4)
    sampleList.tail              // List(20, 30, 40, 10) // all but the head
    sampleList.take(3)           // List(10, 20, 30)     // first 3 elements
    sampleList.takeWhile(_ < 30) // List(10, 20)         // take while predicate true

    // flatten — collapse nested lists into a single list
    val nested = List(List(1,2), List(3,4))
    nested.flatten               // List(1, 2, 3, 4)

    // map, flatMap — transform and flatten/transform respectively
    val nums = List("one", "two")
    nums.map(_.toUpperCase)      // List("ONE", "TWO")
    nums.flatMap(_.toUpperCase)  // List('O', 'N', 'E', 'T', 'W', 'O')

    // Reductions and folds
    val firstTen = (1 to 10).toList
    firstTen.reduceLeft(_ + _)   // 55
    firstTen.foldLeft(100)(_ + _)// 155 (100 is a "seed" / initial accumulator)

/* 
The Scala collections also include a LazyList, which is a lazy immutable linked list. 
It’s called “lazy”—or non-strict—because it computes its elements only when they are needed.

In all of the below examples, nothing happens. Indeed, nothing will happen until you force it 
to happen, such as by calling its foreach method:
*/

def lazyListExample() = 
  val x = LazyList.range(1, Int.MaxValue)
  x.take(1)      // LazyList(<not computed>)
  x.take(5)      // LazyList(<not computed>)
  x.map(_ + 1)   // LazyList(<not computed>)

  x.take(5).foreach(println)

/////////////////////////////////////////////////////
///////////////// Reductions & Folds //////////////////
/////////////////////////////////////////////////////

// Provide a shared demo function showing reduce/fold steps.
def foldReduceDemo(): Unit =
  val nums = List(1, 2, 3, 4)
  println(s"nums: $nums")

  println("\nreduceLeft steps:")
  val red = nums.reduceLeft { (a, b) =>
    println(s"$a + $b = ${a + b}")
    a + b
  }
  println(s"reduceLeft result: $red")

  println("\nfoldLeft steps (seed = 0):")
  val fld = nums.foldLeft(0) { (acc, x) =>
    println(s"acc=$acc + x=$x -> ${acc + x}")
    acc + x
  }
  println(s"foldLeft result: $fld")

  println("\nfoldLeft with different accumulator type (sum of string lengths):")
  val words = List("a", "bb", "ccc")
  val lenSum = words.foldLeft(0) { (acc, s) =>
    println(s"acc=$acc + len(${s})=${s.length} -> ${acc + s.length}")
    acc + s.length
  }
  println(s"length sum result: $lenSum")

  println("\nempty reduceOption (safe alternative to reduce on empty lists):")
  val empty = List.empty[Int]
  println(s"empty.reduceOption(_ + _): ${empty.reduceOption(_ + _)}")


//////////////////////////////////
/////////////// TUPLES ///////////
//////////////////////////////////

// Tuple is a type that lets you easily put a collection of different types in the same container
// nice for times when you want to put a collection of heterogeneous types in a little collection-like structure.
case class Persona(name: String)

def tupleExample(): Unit =
    val t = (11, "Eleven", Persona("Eleven"))
    println(t(0))
    println(t(1))
    println(t(2))

    val (num, str, person) = t
    
