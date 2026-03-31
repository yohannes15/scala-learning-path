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

val double = (i: Int) => i * 2
val triple = (i: Int) => i * 3

/* 
scala> val double = (i: Int) => i * 2
val double: Int => Int = ...

You can see in the REPL that double has the type Int => Int, 
meaning that it takes a single Int parameter and returns an Int

Once you have a function, you can treat it like any other variable, i.e., like a String or Int variable
 */

def functionVariableExample() =
    val x = double(2)   // 4
    // You can also pass double into a map call
    val doubled = List(1, 2, 3).map(double)
    // a List that contains functions of the type `Int => Int`
    val functionList: List[Int => Int] = List(double, triple)
    // a Map whose keys have the type `String`, and whose
    // values have the type `Int => Int`
    val functionMap: Map[String, Int => Int] = Map(
        "2x" -> double,
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
 - 
 ************************************************************
 ************************************************************/
