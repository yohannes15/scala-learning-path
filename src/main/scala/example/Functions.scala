package example

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

*/
