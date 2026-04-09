package learning.collections

/* 
3 main categories to choose from
    Sequences - sequential collection of elements and may be indexed (like an array) or linear (like a linked list)
    Maps - collection of key/value pairs, like a Java Map, Python dictionary, or Ruby Hash
    Sets - unordered collection of unique elements

All of those are basic types, and have subtypes for specific purposes, 
such as concurrency, caching, and streaming. In addition to those three
main categories, there are other useful collection types, including 
ranges, stacks, and queues.

https://docs.scala-lang.org/overviews/collections-2.13/performance-characteristics.html

`src/main/scala/learning/static/scala.collection-high-level.svg`
    - high level abstract classes or traits (generally have immutable and mutable implementations)
    - Iterable 
        -> Seq
            -> IndexedSeq
            -> LinearSeq
        -> Set
            -> SortedSet
                -> BitSet
        -> Map
            -> Sorted Map

`src/main/scala/learning/static/scala.collection.immutable.svg`
    - immutable collections / implementations 
    - Iterable 
        -> Seq
            -> IndexedSeq
                -> Vector
                -> ArraySeq
                -> NumericRange
                -> String
                -> Range
            -> LinearSeq
                -> List
                -> LazyList
                -> Queue
        -> Set
            -> HashSet
            -> ListSet
            -> SortedSet
                -> BitSet
                -> TreeSet
        -> Map
            -> SortedMap
                -> TreeMap
            -> HashMap
            -> SeqMap
                -> ListMap
                -> VectorMap

`src/main/scala/learning/static/scala.collection.mutable.svg`
    - mutable collections / implementations 
    - Iterable 
        -> PriorityQueue
        -> Seq
            -> IndexedSeq
                -> ArraySeq
                -> StringBuilder
                -> ArrayDeque
                    -> Stack
                    -> Queue
                -> ArrayBuffer
            -> Buffer
                -> ArrayDeque
                    -> Stack
                    -> Queue
                -> ArrayBuffer
                -> ListBuffer
        -> Set
            -> HashSet
            -> LinkedHashSet
            -> SortedSet
                -> BitSet
        -> Map
            -> HashMap
            -> WeakHashMap
            -> ListMap
            -> TreeMap
            -> MultiMap
            -> SeqMap
                -> LinkedHashMap

----------------------------------------------------------
Common Collections

CollectionType 	Immutable 	Mutable 	Description
----------------------------------------------------------
List 	         ✓ 	  	                A linear (linked list), immutable sequence
Vector 	         ✓ 	  	                An indexed, immutable sequence
LazyList 	     ✓ 	  	                A lazy immutable linked list, its elements are computed only when they’re needed; Good for large or infinite sequences.
ArrayBuffer 	 ✓ 	                    The go-to type for a mutable, indexed sequence
ListBuffer 	  	 ✓ 	                    Used when you want a mutable List; typically converted to a List
Map 	         ✓ 	        ✓ 	        An iterable collection that consists of pairs of keys and values.
Set 	         ✓ 	        ✓ 	        An iterable collection with no duplicate elements

- Map and Set come in both immutable and mutable versions.
- In Scala, a buffer, such as ArrayBuffer and ListBuffer, is a sequence that can grow and shrink.

Type/Category 	        Immutable 	Mutable
----------------------------------------------------------
Indexed 	            Vector 	    ArrayBuffer
Linear (Linked lists) 	List 	    ListBuffer

- List and Vector are often used when writing code in a functional style. 
- ArrayBuffer is commonly used when writing code in an imperative style. 
- ListBuffer is used when you’re mixing styles, such as building a list.
- Look at CollectionsTasteOfScala.scala for List info and examples
 */

/**********************************************************************
***********************************************************************
                    Vector
-----------------------------------------------------------------------
Vector is an indexed, immutable sequence. 

The “indexed” part of the description means that it provides random access
and update in effectively constant time, so you can access Vector elements
rapidly by their index value, such as accessing listOfPeople(123_456_789)

In general, except for the difference that 
    (a) Vector is indexed and List is not, and 
    (b) List has the :: method, 

the two types work the same, so we’ll quickly run through examples.

Because Vector is immutable, you can’t add new elements to it.
Instead, you create a new sequence by appending or prepending elements 
to an existing Vector

In addition to fast random access and updates, Vector provides 
fast append and prepend times.
***********************************************************************
***********************************************************************/

def vectorExample() = 
    val nums = Vector(1, 2, 3, 4, 5)
    val strings = Vector("One", "Two")

    case class Person(name: String)

    val people = Vector(
        Person("Bert"),
        Person("Ernie"),
        Person("Grover")
    )

    // appending elements/Vector to create new Vector
    val a = Vector(1, 2, 3)
    val b = a :+ 4              // Vector(1, 2, 3, 4)
    val c = a :+ Vector(4, 5)   // Vector(1, 2, 3, Vector(4, 5))
    val d = a ++ Vector(4, 5)   // Vector(1, 2, 3, 4, 5)

    // prepending elements/Vector to create new Vector
    val e = Vector(1,2,3)         // Vector(1, 2, 3)
    val f = 0 +: a                // Vector(0, 1, 2, 3)
    val g = Vector(-1, 0) ++ a   // Vector(-1, 0, 1, 2, 3)

    println(d)
    println(g)


/**********************************************************************
***********************************************************************
                    Array
-----------------------------------------------------------------------
Scala Array elements are mutable, indexed, and have a fixed size.
If you need a resizable sequence, consider using ArrayBuffer instead.
***********************************************************************
***********************************************************************/

def arrayExample() = 
    // Can update and mutate arrays. Can index. But have fixed size
    val a = Array(1, 2, 3)
    a(0)                  // 1
    a(0) = 10             // Array(10, 2, 3)


/**********************************************************************
***********************************************************************
                    ArrayBuffer
-----------------------------------------------------------------------
- general-purpose mutable indexed resizeabl;e sequence
***********************************************************************
***********************************************************************/

def arrayBufferExample() = 
    import scala.collection.mutable.ArrayBuffer

    case class Person(name: String)
    // If you need to start with an empty ArrayBuffer, specify its type
    var strings = ArrayBuffer[String]()
    var ints = ArrayBuffer[Int]()
    var people = ArrayBuffer[Person]()

    // you can create it with an initial size
    // ready to hold 100,000 ints
    val buf = new ArrayBuffer[Int](100_000)

    val nums = ArrayBuffer(1,2,3)
    val peoples = ArrayBuffer(
        Person("Bert"),
        Person("Ernie"),
        Person("Grover")
    )

    /*
    Append new elements to an ArrayBuffer with the += and ++= methods
    if you prefer methods with textual names you can also use 
        `append`, 
        `appendAll`,
        `insert`,
        `insertAll`,
        `prepend`,
        `prependAll`
    */

    val numbers = ArrayBuffer(1, 2, 3)   // ArrayBuffer(1, 2, 3)
    numbers += 4                         // ArrayBuffer(1, 2, 3, 4)
    numbers ++= List(5, 6)               // ArrayBuffer(1, 2, 3, 4, 5, 6)
    numbers ++= ArrayBuffer(7, 8)        // ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8)       
    println(numbers)

    // ArrayBuffer is mutable, so it has methods like 
    // -=, --=, clear, remove, and more
    val a = ArrayBuffer.range('a', 'h')   // ArrayBuffer(a, b, c, d, e, f, g)
    a -= 'a'                              // ArrayBuffer(b, c, d, e, f, g)
    a --= Seq('b', 'c')                   // ArrayBuffer(d, e, f, g)
    a --= Set('d', 'e')                   // ArrayBuffer(f, g)

    // Update elements in an ArrayBuffer by either reassigning 
    // the desired element, or use the update method

    val e = ArrayBuffer.range(1,5)        // ArrayBuffer(1, 2, 3, 4)
    e(2) = 50                             // ArrayBuffer(1, 2, 50, 4)
    e.update(0, 10)                       // ArrayBuffer(10, 2, 50, 4)
    println(e)


/**********************************************************************
***********************************************************************
                    Maps
-----------------------------------------------------------------------
An iterable collection that consists of pairs of keys and values.
has both mutable and immutable Map types

Scala has many more specialized Map types, including 
CollisionProofHashMap, HashMap, LinkedHashMap, ListMap, SortedMap, 
TreeMap, WeakHashMap, and more.

This section demonstrates how to use the immutable Map
***********************************************************************
***********************************************************************/

def mapExample() = 
    val states = Map(
        "AK" -> "Alaska",
        "AL" -> "Alabama",
        "AZ" -> "Arizona"
    )

    for (k, v) <- states do println(s"key: $k, value: $v")

    val ak = states("AK")
    val al = states("AZ")

    /* 
    In practice, you’ll also use methods like keys, keySet, keysIterator, 
    for loops, and higher-order functions like map to work with Maps.
     
    Add elements to an immutable map using + and ++, remembering to 
    assign the result a new variable 
    */

    val a = Map(1 -> "one")     // a: Map(1 -> one)
    val b = a + (2 -> "two")    // b: Map(1 -> one, 2 -> two)
    // c: Map(1 -> one, 2 -> two, 3 -> three, 4 -> four)
    val c = b ++ Seq(
        3 -> "three",
        4 -> "four"
    )
    val d = c ++ Map(5 -> "five")
    println(d)

    /* 
    Remove elements from an immutable map using - or -- and the key values
    to remove, remembering to assign the result to a new variable
    */

    val numsss = Map(
        1 -> "one",
        2 -> "two",
        3 -> "three",
        4 -> "four"
    )

    val numss = numsss - 4       // b: Map(1 -> one, 2 -> two, 3 -> three)
    val nums = numss - 4 - 3     // c: Map(1 -> one, 2 -> two)
    
    println(nums)

    /* 
    update elements in an immutable map, use the updated method 
    (or the + operator) while assigning the result to a new variable
    */

    val vals = Map(
        1 -> "one",
        2 -> "two",
        3 -> "three"
    )

    val updatedVals = vals.updated(3, "THREE!")     // Map(1 -> one, 2 -> two, 3 -> THREE!)
    val updatedValsAnotherWay = vals + (2 -> "TWO....")  // Map(1 -> one, 2 -> TWO..., 3 -> three)
    println(updatedVals)
    println(updatedValsAnotherWay)

/**********************************************************************
***********************************************************************
                    Sets
-----------------------------------------------------------------------
An iterable collection with no duplicate elements.
Order of iteration of the elements is arbitrary.

Scala has both mutable and immutable Set types. 

This section demonstrates the immutable Set.
***********************************************************************
***********************************************************************/

def setExample() = 
    // create new empty sets
    val nums = Set[Int]()
    val letters = Set[Char]()

    // with initial data

    val initalNums = Set(1, 2, 3, 3, 3)                // Set(1, 2, 3)
    val initalLetters = Set('a', 'b', 'c', 'c', 'c')   // Set('a', 'b', 'c')

    // Add elements to an immutable Set using + and ++, 
    // remembering to assign the result to a new variable

    val a = Set(1, 2)                // Set(1, 2)
    val b = a + 3                    // Set(1, 2, 3)
    val c = b ++ Seq(4, 1, 5, 5)     // HashSet(5, 1, 2, 3, 4)
    println(c)

    // Remove elements from an immutable set using - and --, 
    // again assigning the result to a new variable

    val d = Set(1, 2, 3, 4, 5)   // HashSet(5, 1, 2, 3, 4)
    val e = d - 5                // HashSet(1, 2, 3, 4)
    val f = e -- Seq(3, 4)       // HashSet(1, 2)
    println(f)

/**********************************************************************
***********************************************************************
                    Range
-----------------------------------------------------------------------
Often used to populate data structures and to iterate over for loops.

This section demonstrates **Range** (`to` / `until` / `by`, and collection factories).
***********************************************************************
***********************************************************************/

def rangeExample() = 
    val a = 1 to 5         // Range(1, 2, 3, 4, 5)
    val b = 1 until 5      // Range(1, 2, 3, 4)
    val c = 1 to 10 by 2   // Range(1, 3, 5, 7, 9)
    val d = 'a' to 'c'     // NumericRange(a, b, c)

    // You can use ranges to populate collections:
    val xL = (1 to 5).toList     // List(1, 2, 3, 4, 5)
    val xA = (1 to 5).toBuffer   // ArrayBuffer(1, 2, 3, 4, 5)

    // looping
    for i <- 1 to 3 do println(i)

    // There are also range methods on Vector, List, Set
    val vector = Vector.range(1, 5)       // Vector(1, 2, 3, 4)
    val list = List.range(1, 10, 2)     // List(1, 3, 5, 7, 9)
    val set = Set.range(1, 10)         // HashSet(5, 1, 6, 9, 2, 7, 3, 8, 4)

    println(xL)
    println(xA)

    // Also useful for generating test collections

    val evens = (0 to 10 by 2).toList     // List(0, 2, 4, 6, 8, 10)
    val odds = (1 to 10 by 2).toList      // List(1, 3, 5, 7, 9)
    val doubles = (1 to 5).map(_ * 2.0)   // Vector(2.0, 4.0, 6.0, 8.0, 10.0)

    // create a Map
    val seq: IndexedSeq[(Int, String)] = (1 to 3).map(e => (e,s"$e"))
    val map: Map[Int, String] = seq.toMap
    println(map)

/**********************************************************************
SUMMARY
-----------------------------------------------------------------------
When you need more information about specialized collections, 
see the following resources:

Concrete Immutable Collection Classes
    - (https://docs.scala-lang.org/overviews/collections-2.13/concrete-immutable-collection-classes.html)
Concrete Mutable Collection Classes 
    - (https://docs.scala-lang.org/overviews/collections-2.13/concrete-mutable-collection-classes.html)
How are the collections structured? Which one should I choose? 
    - (https://docs.scala-lang.org/tutorials/FAQ/index.html)

When you need to see more details about the collections types shown in this chapter, 
see their Scaladoc pages:

    List - https://www.scala-lang.org/api/current/scala/collection/immutable/List.html
    Vector - https://www.scala-lang.org/api/current/scala/collection/immutable/Vector.html
    ArrayBuffer - https://www.scala-lang.org/api/current/scala/collection/mutable/ArrayBuffer.html
    Range - https://www.scala-lang.org/api/current/scala/collection/immutable/Range.html

Also mentioned are the immutable Map and Set:

    Map - https://www.scala-lang.org/api/current/scala/collection/immutable/Map.html
    Set - https://www.scala-lang.org/api/current/scala/collection/immutable/Set.html

and the mutable Map and Set:

    Map - https://www.scala-lang.org/api/current/scala/collection/mutable/Map.html
    Set - https://www.scala-lang.org/api/current/scala/collection/mutable/Set.html

************************************************************************/
