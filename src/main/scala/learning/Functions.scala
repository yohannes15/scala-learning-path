package learning

/*
FUNCTIONS

- Anonymous Functions
- Function Variables
- Partial Functions
- Eta-Expansion
- Higher-order Functions (HOFs)
- Custom Map method
- Method that returns a function
*/


/************************************************************
 ************************************************************
 Anonymous Functions / Lambda
 --------------------------------
 - a block of code that’s passed as an argument to a higher-order function.
 - a function definition that is not bound to an identifier
 - is because it’s not assigned to a variable, and therefore doesn’t have a name.
************************************************************
************************************************************/

val ints = List(1, 2, 3)
val doubledInts = ints.map(_ * 2)
val doubledInts2 = ints.map((i: Int) => i * 2)

/* 
If you’re not familiar with this syntax, it helps to think of the => symbol as a transformer,
because the expression transforms the parameter list on the left side of the symbol
(an Int variable named i) into a new result using the algorithm on the right side
of the => symbol (in this case, an expression that doubles the Int). 
*/

def anonymousExample() =
    // lambdas
    ints.foreach((i: Int) => println(i))
    ints.foreach(i => println(i))
    // When i is used only once in the body of the function, it can be simplified to `_`
    ints.foreach(println(_))
    // if an anonymous function consists of one method call that takes a single argument, 
    // you don’t need to explicitly name and specify the argument
    ints.foreach(println)

/************************************************************
 ************************************************************
 Function Variables
 --------------------------------
 - an anonymous function, aka function literal, can be assigned 
   to a variable to create a function variable.
 ************************************************************
 ************************************************************/

// a function variable named double
// val variableName = Tuple(parameter: T) => function literal

val doubleFunc = (i: Int) => i * 2
val triple = (i: Int) => i * 3

/* 
scala> val double = (i: Int) => i * 2
val double: Int => Int = ...

You can see in the REPL that double has the type Int => Int, 
meaning that it takes a single Int parameter and returns an Int

Once you have a function, you can treat it like any other variable, i.e., like a String or Int variable
 */

def functionVariableExample() =
    val x = doubleFunc(2)   // 4
    // You can also pass double into a map call
    val doubled = List(1, 2, 3).map(doubleFunc)
    // a List that contains functions of the type `Int => Int`
    val functionList: List[Int => Int] = List(doubleFunc, triple)
    // a Map whose keys have the type `String`, and whose
    // values have the type `Int => Int`
    val functionMap: Map[String, Int => Int] = Map(
        "2x" -> doubleFunc,
        "3x" -> triple
    )
    println(x)
    println(doubled)
    println(functionList)
    println(functionMap)

/************************************************************
 ************************************************************
 Partial Functions
 --------------------------------
 - Function that may not be defined for all values of its argument type
 - Unary functions that implement the `PartialFunction[A, B]` trait, 
   where `A` is the argument type `B` is the result type
 - use `case` like match expressions
 ************************************************************
 ************************************************************/

val doubledOdds: PartialFunction[Int, Int] = {
    case i if i % 2 == 1 => i * 2
}

// Partial functions can be passed as an argument to a method

def partialFunctionExample() = 
    // check if partial func is defined for an arg
    println(doubledOdds.isDefinedAt(3))
    println(doubledOdds.isDefinedAt(4))
    try
        doubledOdds(4)
    catch
        case me: scala.MatchError => println(me.toString())
        case _ => println("some other error")
    
    // as argument to a method
    val res = List(1,2,3).collect({case i if i % 2 == 1 => i * 2})
    println(res)

    // define a default value for arguments not in domain with applyOrElse
    println(doubledOdds.applyOrElse(4, _ + 1))

    // Two partial function can be composed with orElse, 
    // the second function will be applied for arguments where the first one is not defined:

    val incrementedEvens: PartialFunction[Int, Int] = {
        case i if i % 2 == 0 => i + 1
    }

    val res2 = List(1,2,3,4,5).collect(doubledOdds.orElse(incrementedEvens))
    println(res2)

/************************************************************
 ************************************************************
 ETA-Expansion
 --------------------------------
 When you look at the Scaladoc for the `map` method on Scala collections classes, 
 you see that it’s defined to accept a function value:
    def map[B](f: A => B): List[B]
//                 ^^^^^^ function type from `A` to `B`

Indeed, the Scaladoc clearly states, 
    “f is the function to apply to each element.” 
But despite that, somehow you can pass a method into map, and it still works.

eta-expansion: 
    converts an expression of method type to an equivalent expression of function type, seamlessly and quietly
    helpful desugaring that lets you use methods just like functions,

Difference b/n methods and functions
-------------------------------------------------------------
The key d/f b/n methods and functions is that a function is an object, 
    -> i.e. it is an instance of a class, and in turn 
    -> has its own methods (e.g try `f.apply` on a function `f`)

Methods aren't values that can be passed around, i.e. they can only be called
via method application e.g foo(arg1, arg2, ...). Methods can be converted to a value
by creating a function value that will call the method when supplied with the required
arguments. 

More concretely: with automatic eta-expansion, the compiler automatically converts any
method reference, without supplied arguments, to an equivalent anonymous function that
will call the method. 

For example, the reference to times10 in the code above gets rewritten to x => times10(x), as seen below

When does eta-expansion happen?
-------------------------------------------------------------
Automatic eta-expansion is a desugaring that is context-dependent 
(i.e the expansion conditionally activates, depending on the surrounding code of the method reference)

In Scala 2 eta-expansion only occurs automatically when the expected type is a function type. 
For example, the following will fail:

    def isLessThan(x: Int, y: Int): Boolean = x < y
    val methods = List(isLessThan)
    // error: missing argument list for method isLessThan
    // Unapplied methods are only converted to functions when a function type is expected.
    // You can make this conversion explicit by writing `isLessThan _` or `isLessThan(_,_)` 

New to Scala 3, method references can be used everywhere as a value, they will be automatically
converted to a function object with a matching type. e.g.

    def isLessThan(x: Int, y: Int): Boolean = x < y
    val methods = List(isLessThan)       // List[(Int, Int) => Boolean]

    eta expansion converts it: 
        val methods = List((x: Int, y: Int) => isLessThan(x, y))
************************************************************
************************************************************/

def times10(i: Int) = i * 10
def isLessThan(x: Int, y: Int): Boolean = x < y
def isGreaterThan(x: Int, y: Int): Boolean = x > y
def isEqualTo(x: Int, y: Int): Boolean = x == y


def etaExpansionExample() =
    val res = List(1, 2, 3).map(times10)
    // eta-expansion -> written as List(1,2,3).map(x => times10(x)) 
    val comparators: List[(Int, Int) => Boolean] = List(isLessThan, isGreaterThan, isEqualTo)
    val results = comparators.map(f => f(3, 5)) // List(true, false, false)
    // Manual eta-expansion
    // You can always manually eta-extend a method to a func value. Examples:
    val methodsA = List(isLessThan(_, _))
    val methodsB = List((x, y) => isLessThan(x, y))

    val resultsA = methodsA.map(f => f(3, 5)) // List(true)
    val resultsB = methodsB.map(f => f(3, 5)) // List(true)

/************************************************************
 ************************************************************
                Higher-Order Functions
 --------------------------------------------------------------------

 A higher-order function (HOF) is often defined as a function that 
    (a) takes other functions as input parameters or 
    (b) returns a function as a result. 
 
 In Scala, HOFs are possible because functions are first-class values.
 This phrase applies to both methods and functions due to Scala’s Eta Expansion

-------------------------------------------------------------------
Understanding `filter’s` Scaladoc
-------------------------------------------------------------------
Here’s the filter definition in the List[A] class:

  `def filter(p: A => Boolean): List[A]`  
  // uses the predicate p to create and return the List[A]

This states that filter is a method that takes a function parameter named p. 
By convention, p stands for a predicate: is a function that takes one or more 
arguments and returns a Boolean value, which is either true or false.

Returns a List[A], where A is the type held in the list; if you call filter 
on a List[Int], A is the type Int.

`p: A => Boolean`
    -> func must take the type A as input and return a Boolean

So if your list is a List[Int], you can replace the type parameter A with Int, 
and read that signature like this:
    `p: Int => Boolean`

Because isEven has this type—it transforms an input Int into a resulting Boolean
it can be used with filter.

************************************************************
************************************************************/

/*
-------------------------------------------------------------------
Writing methods that take function parameters
--------------------------------------------------------------------

- f is the name of the function input parameter.
- The type signature of f specifies the type of the functions this method will accept.
- The () portion of f’s signature (on the left side of the => symbol): f takes no input parameters
- The Unit portion of the signature (on the right side of the => symbol): f should not return a meaningful result.
- Looking below at the body of the sayHello method (on the right side of the = symbol), 
  the f() statement there invokes the function that’s passed in.
*/

def sayHello(f: () => Unit): Unit = f()

/* 
Now that we’ve defined sayHello, let’s create a function to match f’s signature so we can test it.
The following function takes no input parameters and returns nothing, so it matches f’s type signature:
*/

def helloJoe(): Unit = println("Hello, Joe")
def bonjourJulien(): Unit = println("Bonjour, Julien")


def hofExample() =
    // sayHello cantake any function that matches f’s signature
    sayHello(helloJoe)   // prints "Hello, Joe"
    sayHello(bonjourJulien) // prints "Bonjour, Julien"
/*
-------------------------------------------------------------------
General syntax for defining function input parameters in HOFs
--------------------------------------------------------------------
Because functional programming is like creating and combining a series of algebraic equations, 
it’s common to think about types a lot when designing functions and applications. 
You might say that you “think in types.”

    `variableName: (parameterTypes ...) => returnType`

To demonstrate more type signature examples, heres a function that takes
a String parameter and returns an Int

    `f: String => Int`

Examples: stringLength, checkSum are examples

    `f: (Int, Int) => Int`

Examples: any function that takes two input ints and retuns an int
    1. def add(a: Int, b: Int): Int = a + b
    2. def subtract(a: Int, b: Int): Int = a - b
    3. def multiply(a: Int, b: Int): Int = a * b

-------------------------------------------------------------------
Taking a function parameter along with other parameters
-------------------------------------------------------------------

For HOFs to be really useful, they also need some data to work on. For a class like List, 
its map method already has data to work on: the data in the List. But for a standalone HOF
that doesn’t have its own data, it should also accept data as other input parameters.

For instance, here’s a method named executeNTimes that has two input parameters: a function, and an Int:

*/

def executeNTimes(f: () => Unit, n: Int): Unit = 
    for i <- 1 to n do f()

// To test executeNTimes, define a method that matches f’s signature:

def helloWorld(): Unit = println("Hello World!")

def hofExample2() =
    // executeNTimes method executes the helloWorld function three times.
    executeNTimes(helloWorld, 3)

/* 
Your methods can continue to get as complicated as necessary. 
For example, this method takes a function of type (Int, Int) => Int, along with two input parameters:

Because the sum and multiply methods match that type signature, 
they can be passed into executeAndPrint along with two Int values:
 */

def executeAndPrint(f: (Int, Int) => Int, i: Int, j: Int): Unit = 
    println(f(i, j))

def sum(x: Int, y: Int) = x + y
def multiply(x: Int, y: Int) = x * y


def hofExample3() =
    // executeNTimes method executes the helloWorld function three times.
    executeAndPrint(sum, 3, 11) // prints 14
    executeAndPrint(multiply, 3, 9) // prints 27

/* 

A great thing about learning about Scala’s function type signatures is that the syntax you use
to define function input parameters is the same syntax you use to write function literals.

    type signature: (Int, Int) => Int
    input parameters: (a, b)
    body: a + b
*/

//  function that calculates the sum of two integers. You can see the type matches f in executeAndPrint
val f: (Int, Int) => Int = (a, b) => a + b


/************************************************************
 ************************************************************
 Writing your own map method
 ---------------------------
 Imagine for a moment that the List class doesn’t have its own map method.
 A good first step when creating functions is to accurately state the problem
 Focusing only on a List[Int], you state

    => I want to write a `map` method that can be used to apply a function to 
       each element in a List[Int] that it’s given, returning the transformed 
       elements as a new list.

Steps:

1. First you know that you want to accept a function as a parameter and that function
that should transform an Int into some type A, so you write

    `def map(f: (Int) => A)`

2. The syntax for using a type parameter requires declaring it in square brackets [] 
before the parameter list, so you add that:
    
    `def map[A](f: (Int) => A)`

3. Next you know map should accept a List[Int]

    `def map[A](f: (Int) => A, xs: List[Int])`

4. You also know that map returns a transformed List that contains elements of the type A

    `def map[A](f: (Int) => A, xs: List[Int]): List[A] = ???`

5. Apply body. Method applies the function its given to every element in the list
   to produce a new, transformed list
    
    def map[A](f: (Int) => A, xs: List[Int]): List[A] = 
        for x <- xs yield f(x)
        
6. As a bonus, notice for expression doesn't do anything that depends on the type inside
   the `List` being `Int`. So you can replace `Int` in the type signaute with the type B

    def map[A, B](f: (B) => A, xs: List[B]): List[A] =
        for x <- xs yield f(x)
***********************************************************
************************************************************/

def map[A, B](f: (B) => A, xs: List[B]): List[A] =
  for x <- xs yield f(x)

// Now you have a map method that works with any List.

// These methods match the type f accepts
def timesTwo(i: Int): Int = i * 2
def strlen(s: String): Int = s.length

def customMapMethodExample() = 
    println(map(timesTwo, List(1,2,3)))             // List(2, 4, 6)
    println(map(strlen,List("a", "bb", "ccc")))   // List(1, 2, 3)


/************************************************************
 ************************************************************
 Creating a Method That Returns a Function
 -----------------------------------------------------------

writing a method that returns a function is similar to everything you’ve seen
For example, imagine that you want to write a greet method that returns a function. 

Once again we start with a problem statement:

    `I want to create a greet method that returns a function. That function will take
     a string parameter and print it using println. To simplify this first example, 
     greet won’t take any input parameters; it will just build a function and return it.`
    
Steps:

1. It is a method

    `def greet()`

2. It will return a function that (a) takes a String parameter and (b) prints string

    `def greet(): String => Unit = ???`

3. Now you just need a body method

     def greet(): String => Unit = 
        (name: String) => println(s"Hello, $name")

4. We can also pass in a greeting to make it more useful

    def greet(theGreeting: String): String => Unit =
        (name: String) => println(s"$theGreeting, $name")

*************************************************************
*************************************************************/

def greet(theGreeting: String): String => Unit =
    (name: String) => println(s"$theGreeting, $name")

val sayHello2: String => Unit = greet("Hello") // Type is String => Unit
val sayCiao = greet("Ciao")
val sayHola = greet("Hola")

def methodReturningFuncExample() = 
    sayHello2("Joe")       // prints Hello, Joe
    sayCiao("Isabella")    // prints "Ciao, Isabella"
    sayHola("Carlos")      // prints "Hola, Carlos"


/* 
A More Real-World Example
---------------------------------------------------------
Very useful when your method returns one of many possible functions, like a factory that
returns custom-built functions.

Imagine you want to write a method that returns functions that greet people in different
languages, limited to English or French, depending on a parameter thats passed into method

Steps

1. You want to create a method 
    (a) takes a "desired languaged" as an input 
    (b) returns a function as its result

2. B/c that function prints a string that its given, you know it has type String => Unit

3. Next, because you know that the possible functions you’ll return take a string and print it, 
   you can write two anonymous functions for the English and French languages:

* Notice that returning a function from a method is no different than returning a string or integer value.
 */


def createGreetingFunction(desiredLanguage: String): String => Unit =
    val englishGreeting = (name: String) => println(s"Hello, $name")
    val frenchGreeting = (name: String) => println(s"Bonjour, $name")

    desiredLanguage match
        case "english" => englishGreeting
        case "french" => frenchGreeting

def methodReturningFuncExample2() = 
    val greetInFrench = createGreetingFunction("french")
    greetInFrench("Jonathan")   // prints "Bonjour, Jonathan"
    val greetInEnglish = createGreetingFunction("english")
    greetInEnglish("Joe")   // prints "Hello, Joe"


/*************************************************************
 *****************SUMMARY*************************************
 *************************************************************

This was a long chapter, so let’s review the key points that are covered.

A higher-order function (HOF) is often defined as a function that takes
other functions as input parameters or returns a function as its value.

In Scala this is possible because functions are first-class values.

Moving through the sections, first you saw:

    You can write anonymous functions as small code fragments
    You can pass them into the dozens of HOFs (methods) on the collections classes, i.e., methods like filter, map, etc.
    With these small code fragments and powerful HOFs, you create a lot of functionality with just a little code

After looking at anonymous functions and HOFs, you saw:

    Function variables are simply anonymous functions that have been bound to a variable

After seeing how to be a consumer of HOFs, you then saw how to be a creator of HOFs. Specifically, you saw:

    How to write methods that take functions as input parameters
    How to return a function from a method

A beneficial side effect of this chapter is that you saw many examples of how to declare type signatures for functions. 
The benefits of that are that you use that same syntax to define function parameters, anonymous functions, and function variables,
and it also becomes easier to read the Scaladoc for higher-order functions like map, filter, and others.

 */
