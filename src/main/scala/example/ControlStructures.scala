package example 

/*
when every expression you write returns a value, that style is referred as expression-oriented programming,

`val minValue = if a < b then a else b`

lines of code that don't return values are called `statements`` and they are used for side-effects

if/else expressions always return a result. Because of this, there’s no need for a special ternary operator:
*/

//////////////////////////////////////////////////////////
//////////////////////// FOR LOOPS ///////////////////////
//////////////////////////////////////////////////////////

def forLoops(): Unit = 
    val ints = Seq(1, 2, 3)
    // The code i <- ints is referred to as a generator. 
    // In any generator p <- e, the expression e can generate zero or many bindings to the pattern p.
    for i <- ints
    do 
        println(i)
        // more lines


    // Multiple generators
    for
        i <- 1 to 2
        j <- 'a' to 'b'
        k <- 1 to 10 by 5
    do
        println(s"i = $i, j = $j, k = $k")

    // i = 1, j = a, k = 1
    // i = 1, j = a, k = 6
    // i = 1, j = b, k = 1
    // i = 1, j = b, k = 6
    // i = 2, j = a, k = 1
    // i = 2, j = a, k = 6
    // i = 2, j = b, k = 1
    // i = 2, j = b, k = 6

    // Guards - A for loop can have as many guards as needed.
    for
        i <- 1 to 10
        if i > 3
        if i < 6
        if i % 2 == 0
    do
        println(i)

    // With Maps
    val states = Map(
        "AK" -> "Alaska",
        "AL" -> "Alabama", 
        "AZ" -> "Arizona"
    )

    for 
        (abbrev, fullName) <- states
    do
        println(s"$abbrev: $fullName")

/*
important to know that you can also create for expressions that return values. using yield keyword
for expressions can be used any time you need to traverse all the elements in a collection and apply 
an algorithm to those elements for creating a new list or some other effect
*/
def forExpressions(): Unit = 
    val list =
        for i <- 10 to 12
        yield 10 * 2

    // list: IndexedSeq[Int] = Vector(20, 22, 24)
    // above is identical to the following map method call
    // val list = (10 to 12).map(i => i * 2)

    val names = List("_olivia", "_walter", "_peter")
    val capNames = for name <- names yield
        val nameWithoutUnderscore = name.drop(1)
        val capName = nameWithoutUnderscore.capitalize
        println(capName)
        capName

    // capNames: List[String] = List(Olivia, Walter, Peter)


/* 
Because a for expression yields a result, it can be used as the body of a method that returns a useful value. 
This method returns all the values in a given list of integers that are between 3 and 10:
 */

def between3and10(nums: List[Int]): List[Int] =
    for
        num <- nums
        if num >= 3
        if num <= 10
    yield num


/////////////////////////////////////////////////////////////////
//////////////////////// Match Expressions //////////////////////
/////////////////////////////////////////////////////////////////


// Since the cases are considered in the order they are written, and the first matching case is used, 
// the default case, which matches any value, must come last. 

// The name used in the pattern must begin with a lowercase letter. 
// A name beginning with an uppercase letter does not introduce a variable, but matches a value in scope:

def matchExpressionsExamples(): Unit = 
    val i = 42
    i match
        case 0 => println("1")
        case 1 => println("2")
        case what => println(s"You gave me: $what")
    
    // Handling check against actual variables
    val N = 42
    i match
        case 0 => println("1")
        case 1 => println("2")
        case N => println("42") // matching against the actual value of N (42)
        case n => println(s"You gave me: $n" )

    // Handling multiple possible matches on one line
    val evenOrOdd = i match
        case 1 | 3 | 5 | 7 | 9 => println("odd")
        case 2 | 4 | 6 | 8 | 10 => println("even")
        case _ => println("some other number")

    // If guards in case clauses
    i match
        case 1 => println("one, a lonely number")
        case x if x == 2 || x == 3 => println("two’s company, three’s a crowd")
        case x if x > 3 => println("4+, that’s a party")
        case _ => println("Im guessing your number is zero or less")

    // against range of numbers
    i match
        case a if 0 to 9 contains a => println(s"0-9 range: $a")
        case b if 10 to 19 contains b => println(s"10-19 range: $b")
        case c if 20 to 50 contains c => println(s"20-50 range: $c")
        case _ => println("Hmmm I don't think I have your range")

    
