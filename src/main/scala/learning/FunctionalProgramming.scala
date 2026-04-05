package learning

/*************************************************************
 *****************What is Functional Programming**************
 ************************************************************* 

Essence of Scala is a fusion of functional and object-oriented 
programming in a typed setting:

    - Functions for the logic
    - Objects for the modularity

Functional programming is a programming paradigm where programs
are constructed by applying and composing functions. It is a 
declarative programming paradigm in which function definitions 
are trees of expressions that each return a value, rather than
a sequence of imperative statements which change the state of
the program.

Functions are treated as first-class citizens, just like any 
other data type:
    - can be bound to names (including local identifiers)
    - passed as arguments
    - returned from other functions

This allows programs to be written in a declarative and composable 
style, where small functions are combined in a modular manner.

It can also be helpful to know that experienced functional programmers
have a strong desire to see their code as math, that combining pure 
functions together is like combining a series of algebraic equations.

Once you understand the paradigm, you want to write pure functions 
that always return values, not exceptions or null values, so you can
combine them together to create solutions. That feeling that you're
writing math-like equations (expressions) is the driving desire that
leads you to use only pure functions and immutable values, because
thats what you use in algebra and other forms of math

*************************************************************
*****************Immutable Values****************************
************************************************************* 

In pure functional programming, only immutable values are used.
In scala this means
    - All variables are created as `val` fields
    - Only immutable collection classes are used, such as
      `List`, `Vector` and immutable `Map` & `Set` classes

Using only immutable variables, how does anything ever change?

When it comes to using collections, you apply a function to an
existing collection to create a new collection (like map & filter)

*/

def immutableValuesExample() = 
    /* 
    imagine that you have a list of names, a List[String], that are all 
    in lowercase, and you want to find all the names that begin with the
    letter "j", and then you want to capitalize those names. 
    In FP you write this code:
    */
    val a = List("jane", "jon", "mary", "joe")
    val b = a.filter(_.startsWith("j")).map(_.capitalize)
    println(b)

    /* 
    In FP you don’t create classes with mutable `var` constructor 
    parameters. That is, you don’t write classes like below.
        
       ` class Person(var firstName: String, var lastName: String)`

    Instead, you typically create `case` classes, whose constructor
    parameters are val by default.

    Then, when you need to make a change to the data, you use the 
    copy method that comes with a case class to, update the data 
    as you make a copy, like this:
    */

    case class Person(firstName: String, lastName: String)
    val reginald = Person("Reginald", "Dwight")

    val elton = reginald.copy(
        firstName = "Elton",   // update the first name
        lastName = "John"      // update the last name
    )

    /* 
    Depending on your needs, you may create `enums`, `traits`, 
    or `classes` instead of case classes. See the Data Modeling
    chapter for more details.
     */

/*************************************************************
*****************Pure Functions*******************************
**************************************************************
A pure function is a function that depends only on its declared
inputs and its implementation to produce its output. It only 
computes its output and does not depend on or modify the outside world.

A pure function is defined like this:
    - given the same input x, it always returns the same output f(x)
    - f's output depends only on its input variables and its implementation
    - f only computes the output and doesn't modify the world around it

This implies:
    - It doesn't modify its input parameters
    - It doesn't mutate any hidden state
    - It doesn't have any "back doors"
    - It doesn't read data from outside or write data to the outside world

Example, you can call a double function an infinite number of times with
the input value 2, and you’ll always get the result 4.

Given that definition, as you can imagine, methods like these in 
the scala.math._ package are pure functions:
    - abs
    - ceil
    - max

These String methods are also pure functions:
    - isEmpty
    - length
    - substring

Most methods on the Scala collections classes also work as pure functions, 
including 
    - drop
    - filter
    - map and many more.

In Scala, functions and methods are almost completely interchangeable,
so even though we use the common industry term “pure function,” this 
term can be used to describe both functions and methods. If you’re 
interested in how methods can be used like functions, see the 
`Eta Expansion` discussion.

Examples of Impure Functions
- `println` 
    -> methods that interact with console, files, db, web services, 
       senors, etc... are all impure
- `currentTimeMillis` 
    -> date and time related methods are all impure b/c their 
       output depends on something other than their input parameters
- `sys.error` 
    -> exception throwing methods are impure b/c they don't simply
       return a result

Impure functions often do one or more of these things:

    1. Read from hidden state
       i.e., they access variables and data not explicitly passed into 
       the function as input parameters
    2. Write to hidden state
    3. Mutate the parameters they’re given, or mutate hidden variables, 
       such as fields in their containing class
    4. Perform some sort of I/O with the outside world

In general, you should watch out for functions with a return type 
of `Unit`. Because those functions do not return anything, logically
the only reason you ever call it is to achieve some side effect. In 
consequence, often the usage of those functions is impure.

Impure functions are needed. Write the core of your application using
pure functions, and then write an impure “wrapper” around that core 
to interact with the outside world. As someone once said, this is like 
putting a layer of impure icing on top of a pure cake.

It’s important to note that there are ways to make impure interactions
with the outside world feel more pure. For instance, you’ll hear about
using an `IO` Monad to deal with input and output. These topics are b
beyond the scope of this, so to keep things simple it can help to think
that FP applications have a core of pure functions that are wrapped with
other functions to interact with the outside world.
*/

def pureFunctionsExample() = 
    // example pure functions
    def double(i: Int): Int = i * 2
    def sum(xs: List[Int]): Int = xs match
        case Nil => 0
        case head :: tail => head + sum(tail)
    
    val doubledNumber = double(4)
    println(doubledNumber)


/*************************************************************
*****************Functions Are Values*************************
**************************************************************

While every programming language ever created probably lets you
write pure functions, a second important Scala FP feature is that
you can create functions as values, just like you create String
and Int values

Benefits
1. You can define methods to accept function parameters
2. You can pass functions as parameters into methods

Technically, a function that takes another function as an input
parameter is known as a Higher-Order Function..

The ability to pass functions into other functions helps you 
create code that is concise and still readable — expressive.
*/


def functionAsValuesExample() = 
    val nums = (1 to 10).toList
    // map and filter accept anonymous funcs / lambdas
    val doubles = nums.map(_ * 2)           // double each value
    val lessThanFive = nums.filter(_ < 5)   // List(1,2,3,4)

    // two methods
    def double(i: Int): Int = i * 2
    def underFive(i: Int): Boolean = i < 5
    // map and filter accept methods as args as well
    val underFiveDoubles = nums.filter(underFive).map(double)
    println(underFiveDoubles)

    /* 
    In most scenarios it doesn’t matter if double is a function 
    or a method; Scala lets you treat them the same way. Behind 
    the scenes, the Scala technology that lets you treat methods
    just like functions is known as Eta Expansion.
    */
    
    /* 
    If you’re not comfortable with the process of passing functions
    as parameters into other functions, here are a few more examples
    you can experiment with: 
    */
    val bobJoeUpper = List("bob", "joe").map(_.toUpperCase)          // List(BOB, JOE)
    val bobJoeCap = List("bob", "joe").map(_.capitalize)             // List(Bob, Joe)
    val plumBananaLengths = List("plum", "banana").map(_.length)     // List(4, 6)

    val fruits = List("apple", "pear")
    fruits.map(_.toUpperCase)       // List(APPLE, PEAR)
    fruits.flatMap(_.toUpperCase)   // List(A, P, P, L, E, P, E, A, R)

    val numbers = List(5, 1, 3, 11, 7)
    numbers.map(_ * 2)         // List(10, 2, 6, 22, 14)
    numbers.filter(_ > 3)      // List(5, 11, 7)
    numbers.takeWhile(_ < 6)   // List(5, 1, 3)
    numbers.sortWith(_ < _)    // List(1, 3, 5, 7, 11)
    numbers.sortWith(_ > _)    // List(11, 7, 5, 3, 1)
    numbers.takeWhile(_ < 6).sortWith(_ < _)   // List(1, 3, 5)

/*************************************************************
*****************Functional Error Handling********************
**************************************************************

Functional programming is like writing a series of algebraic 
equations, and because algebra doesn’t have null values or 
throw exceptions, you don’t use these features in FP.

In the situations where you might normally use a null value or
exception in OOP code, what do you do?

Scala’s solution is to use constructs like:

    - Option
        -> Some
        -> None

- Some and None classes are subclasses of Option.
- Option when discussed below referes to the classes Option/Some/None
*/

/* 
Option: A First Example
-------------------------
Example doesn't deal with null values, its a good intro to `Option` classes. 

Imagine that you want to write a method that makes it easy to convert
strings to integer values, and you want an elegant way to handle the
exception that’s thrown when your method gets a string like "Hello"
instead of "1". 

A first guess at such a method might look like below. 

If the conversion works, this method returns the correct Int value, 
but if it fails, the method returns 0. This might be okay for some 
purposes, but it’s not really accurate. For instance, the method might
have received "0", but it may have also received "foo", "bar", or an
infinite number of other strings that will throw an exception. This is
a real problem: How do you know when the method really received a "0",
or when it received something else? 

The answer is that with this approach, there’s no way to know.
*/

def makeIntImperative(s: String): Int =
  try
    Integer.parseInt(s.trim)
  catch
    case e: Exception => 0

/* 
Using Option/Some/None
--------------------------
A common solution to this problem in Scala is to use a trio of classes 
known as Option, Some, and None. The Some and None classes are subclasses 
of Option, so the solution works like this:

    - You declare that makeInt returns an Option type
    - If makeInt receives a string it can convert to an Int, 
      the answer is wrapped inside a Some
    - If makeInt receives a string it can’t convert, 
      it returns a None

This code can be read as, 

When the given string converts to an integer, return the Int wrapped 
inside a Some, such as Some(1). When the string can’t be converted to
an integer, an exception is thrown and caught, and the method returns
a None value.

This technique is used so methods can return values instead of exceptions.
In other situations, Option values are also used to replace null values.

Two notes:

    1. This approach is used throughout Scala & third-party Scala libraries.
    2. Functional methods don’t throw exceptions; instead they return 
       values like Option.

 */
def makeInt(s: String): Option[Int] =
    try
        Some(Integer.parseInt(s.trim))
    catch
        case e: Exception => None

/* 
Being a consumer of makeInt
----------------------------
Now imagine that you’re a consumer of the makeInt method. You know that 
it returns a subclass of Option[Int], so the question becomes:

How do you work with these return types?

There are two common answers, depending on your needs:
    - Use a match expression
    - Use a for expression


*/
def optionExample() = 
    val a = makeInt("1")      // Some(1)
    val b = makeInt("one")    // None
    /* 
    Using a `Match` Expression
    ---------------------------- 
    In this example, if x can be converted to an Int, the expression on 
    the right-hand side of the first case clause is evaluated; if x can’t
    be converted to an Int, the expression on the right-hand side of the 
    second case clause is evaluated.
    */

    def convertStringToInt(x: String): Unit =
        makeInt(x) match
            case Some(i) => println(s"$i was converted to int")
            case None => println("That didn't work.")
        
    convertStringToInt("1")
    convertStringToInt("one")

    /* 
    Using a `for` Expression
    ---------------------------- 
    Another common solution is to use a for expression — i.e., the for/yield 
    combination that was shown earlier in this book. 
    
    For instance, imagine that you want to convert three strings to integer values, 
    and then add them together. 
    
    Below is how you do that with a for expression and makeInt.

    After that expression runs, the result will be one of two things:

    - If all three strings convert to Int values, res will be a Some[Int], 
      i.e., an integer wrapped inside a Some
    - If any of the three strings can’t be converted to an Int, res will be a None

    */

    def convertThreeStringsToIntegers(
        stringA: String, stringB: String, stringC: String
    ): Option[Int] =
        for
            a <- makeInt(stringA)
            b <- makeInt(stringB)
            c <- makeInt(stringC)
        yield
            a + b + c

    val res = convertThreeStringsToIntegers("1", "2", "3")  // Some(6)
    val invalid = convertThreeStringsToIntegers("sa", "1", "2") // None
    println(res)
    println(invalid)

/* 
Thinking of Option as a container
-------------------------------------
Mental models can often help us understand new situations, 
so if you’re not familiar with the Option classes, one way 
to think about them is as a container:

    - `Some` is a container with one item in it
    - `None` is a container, but it has nothing in it

If you prefer to think of the Option classes as being like a box, 
None is like an empty box. It could have had something in it, but it doesn’t.

Using Option to replace null
------------------------------------
Getting back to null values, a place where a null value can silently 
creep into your code is with a class like Address below.

While every address on Earth has a street1 value, the street2 value
is optional. As a result, the street2 field can be assigned a null value

Historically, developers have used blank strings and null values in this
situation, both of which are hacks to work around the root problem: 
    
    `street2` is an optional field. 
    
In Scala—and other modern languages—the correct solution is to declare 
up front that street2 is optional:
 */

def optionToReplaceNullExample() = 
    class UnOptimizedAddress(
        var street1: String,
        var street2: String,
        var city: String,
        var state: String,
        var zip: String
    )

    val unoptimizedSanta = UnOptimizedAddress(
        street1 = "1 Main Street",
        street2 = null,               // <-- D’oh! A null value!
        city = "North Pole",
        state = "Alaska",
        zip = "99705"
    )
    println(s"$unoptimizedSanta address has street 2 explicity as null")

    // correct solution is to declare up front that street2 is optional:
    class StreetAddress(
        var street1: String,
        var street2: Option[String],   // an optional value
        var city: String, 
        var state: String, 
        var zip: String
    )

    // More accurate code then written like below
    val santa = StreetAddress(
        street1 = "1 Main Street",
        street2 = None,           // 'street2' has no value
        city = "North Pole",
        state = "Alaska",
        zip = "99705"
    )
    // or this
    val santa2 = StreetAddress(
        street1 = "123 Main Street",
        street2 = Some("Apt. 2B"),
        city = "Talkeetna",
        state = "Alaska",
        zip = "99676"
    )

    println(s"$santa2 and $santa have valid street2")

/* 
Try/Success/Failure
-------------------------------------------
While this section focuses on the Option classes, Scala has a few other alternatives.

For example, a trio of classes known as `Try/Success/Failure` work in the same manner

- (a) you primarily use these classes when your code can throw exceptions, and 
- (b) you want to use the `Failure` class because it gives you access to the exception message. 

For example, these `Try` classes are commonly used when writing methods that interact
with files, databases, and internet services, as those functions can easily throw exceptions.

- Try makes it very simple to catch exceptions
- Failure contains the exception

There are quite a few ways to work with the results of a Try — including the ability to 
“recover” from the failure — but common approaches still involve using match and for expressions:
*/

def trySuccessFailureExample() = 
    import scala.util.{Try,Success,Failure}
    // that’s quite a bit shorter than the Option/Some/None approach
    def toInt(s: String): Try[Int] = Try { Integer.parseInt(s.trim) }

    val a = toInt("1")  // Success(1)
    println(s"Converting using Try/Success/Failure '1' got $a")

    val b = toInt("boo") // Failure(java.lang.NumberFormatException: For input string: "boo")
    println(s"Converting using Try/Success/Failure 'boo' got $b")

    def convertStringToInt(x: String): Unit =
        toInt(x) match {
            case Success(i) => println(i)
            case Failure(s) => println(s"Failed. Reason $s")
        }

    convertStringToInt("1")
    convertStringToInt("boo")

    def convertThreeStringsToIntegers(
        stringA: String, stringB: String, stringC: String
    ): Try[Int] =
        for
            a <- toInt(stringA)
            b <- toInt(stringB)
            c <- toInt(stringC)
        yield
            a + b + c

    val res = convertThreeStringsToIntegers("1", "2", "3")      // Success(6)
    val invalid = convertThreeStringsToIntegers("sa", "1", "2") // Failure(...)
    println(res)
    println(invalid)

/* 
Option/Some/None & Try/Success/Failure aren't the only solutions
----------------------------------------------------------------
There are other classes that work in a similar manner, including Either/Left/Right
in the Scala library, and other third-party libraries, but Option/Some/None and 
Try/Success/Failure are commonly used, and good to learn first.

You can use whatever you like, but Try/Success/Failure is generally used when dealing
with code that can throw exceptions — because you almost always want to understand 
the exception — and Option/Some/None is used in other places, such as to avoid using 
null values.

A Quick Review
----------------------------------------------------------------
This section was long, so let’s give it a quick review:

    Functional programmers don’t use `null` values
    A main replacement for `null` values is to use the `Option` classes
    Functional methods don’t throw exceptions; instead they return values like `Option`, `Try`, or `Either`
    Common ways to work with `Option` values are `match` and `for` expressions
    Options can be thought of as containers of one item (Some) and no items (None)
    Options can also be used for optional constructor or method parameters
    Use Try/Success/Failure when working with exception prone code and require the Failure message

*/
