package learning

// Scala has most features expected in a FP language including:
// Lambdas (anonymous functions)
// Higher-order functions (HOFs) -> Func that takes a function as a parameter
// Immutable collections in the standard library
// don’t mutate the collection they’re called on; instead, they return a new collection with the updated data.

// lambda into the map method:
// map method: applies a given function to every element in a list, yielding a new list that contains the resulting values.
val a = List(1, 2, 3).map(i => i * 2)   // List(2,4,6)
val b = List(1, 2, 3).map(_ * 2)        // List(2,4,6)

// Those examples are also equivalent to the following code, which uses a named method instead of a lambda:
def doubleInt(i: Int): Int = i * 2
val a1 = List(1, 2, 3).map(i => doubleInt(i))   // List(2,4,6)
val b1 = List(1, 2, 3).map(doubleInt)           // List(2,4,6)


def higherOrderFunc(): Unit = 
    val nums = (1 to 10).toList
    // methods can be chained together
    val newNums = nums.filter(_ > 3).filter(_ < 7).map(i => i * 10)
    println(newNums)
