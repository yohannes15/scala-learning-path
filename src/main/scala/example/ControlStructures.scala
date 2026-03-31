package example

import java.security.KeyException
import java.io.IOException

trait Animal:
  val name: String

case class Cat(name: String) extends Animal:
  def meow: String = "Meow"

case class Dog(name: String) extends Animal:
  def bark: String = "Bark"
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
  do println(s"i = $i, j = $j, k = $k")

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
  do println(i)

  // With Maps
  val states = Map(
    "AK" -> "Alaska",
    "AL" -> "Alabama",
    "AZ" -> "Arizona"
  )

  for (abbrev, fullName) <- states
  do println(s"$abbrev: $fullName")

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
    case 0    => println("1")
    case 1    => println("2")
    case what => println(s"You gave me: $what")

  // Handling check against actual variables
  val N = 42
  i match
    case 0 => println("1")
    case 1 => println("2")
    case N => println("42") // matching against the actual value of N (42)
    case n => println(s"You gave me: $n")

  // Handling multiple possible matches on one line
  val evenOrOdd = i match
    case 1 | 3 | 5 | 7 | 9  => println("odd")
    case 2 | 4 | 6 | 8 | 10 => println("even")
    case _                  => println("some other number")

  // If guards in case clauses
  i match
    case 1                     => println("one, a lonely number")
    case x if x == 2 || x == 3 => println("two’s company, three’s a crowd")
    case x if x > 3            => println("4+, that’s a party")
    case _ => println("Im guessing your number is zero or less")

  // against range of numbers
  i match
    case a if 0 to 9 contains a   => println(s"0-9 range: $a")
    case b if 10 to 19 contains b => println(s"10-19 range: $b")
    case c if 20 to 50 contains c => println(s"20-50 range: $c")
    case _ => println("Hmmm I don't think I have your range")

  /*
    You can also extract fields from case classes - and classes that have properly written
    apply / unapply methods and use those in your guard conditions.
   */
  def speak(p: Persona): Unit = p match
    case Persona(name) if name == "Fred" =>
      println(s"$name says, Yubb dybba doo")
    case Persona(name) if name == "Bam Bam" => println(s"$name says, Bam bam!")
    case _                                  => println("Watch the Flintstones!")

  speak(Persona("Fred"))
  speak(Persona("Bam Bam"))

  // binding matched patterns to variables. You can bind the matched pattern to a variable to use type-specific behaviour
  // bind variable using sytax `case v @ ...`

  def animalSpeak(animal: Animal) = animal match
    case c @ Cat(name) if name == "Felix" =>
      println(s"$name says, ${c.meow}")
    case d @ Dog(name) if name == "Rex" => println(s"$name says, ${d.bark}")
    case _                              => println("I don't know you")

  animalSpeak(Cat("Felix"))
  animalSpeak(Dog("Rex"))

  // Using match expression as body of method. Match expressions return a value, they can be used as body of method
  // Using a match expression as the body of a method is a very common use.
  // The input parameter a is defined to be the Matchable type—which is the root of all Scala types that pattern matching can be performed on.
  def isTruthy(a: Matchable): Boolean = a match
    case 0 | "" | false => false
    case _              => true

  isTruthy(0) // false
  isTruthy(false) // false
  isTruthy("") // false
  isTruthy(1) // true
  isTruthy(" ") // true
  isTruthy(2f) // true

  /*
    Match expressions support many different types of patterns

    There are many different forms of patterns that can be used to write match expressions. Examples include:

        Constant patterns (such as case 3 => )
        Sequence patterns (such as case List(els : _*) =>)
        Tuple patterns (such as case (x, y) =>)
        Constructor pattern (such as case Person(first, last) =>)
        Type test patterns (such as case p: Person =>)

    All of these kinds of patterns are shown in the following pattern method, which takes an input parameter of type Matchable and returns a String:
   */

  def pattern(x: Matchable): String = x match

    // constant patterns
    case 0       => "zero"
    case true    => "true"
    case "hello" => "you said 'hello'"
    case Nil     => "an empty List"

    // sequence patterns
    case List(0, _, _) => "a 3-element list with 0 as the first element"
    case List(1, _*)   => "list, starts with 1, has any number of elements"
    case Vector(1, _*) => "vector, starts w/ 1, has any number of elements"

    // tuple patterns
    case (a, b)    => s"got $a and $b"
    case (a, b, c) => s"got $a, $b, and $c"

    // constructor patterns
    case Person(first, "Alexander") => s"Alexander, first name = $first"
    case Dog("Zeus")                => "found a dog named Zeus"

    // type test patterns
    case s: String         => s"got a string: $s"
    case i: Int            => s"got an int: $i"
    case f: Float          => s"got a float: $f"
    case a: Array[Int]     => s"array of int: ${a.mkString(",")}"
    case as: Array[String] => s"string array: ${as.mkString(",")}"
    case d: Dog            => s"dog: ${d.name}"
    case list: List[?]     => s"got a List: $list"
    case m: Map[?, ?]      => m.toString

    // the default wildcard pattern
    case _ => "Unknown"

  println(pattern(Dog("Zeus")))
  println(pattern(List(0)))
  println(pattern((1, 2, 3)))

  def multilineExample() =
    val count = -1
    count match
      case 1 =>
        println("one, a lonely number")
      case x if x == 2 || x == 3 =>
        println("two's company, three's a crowd")
      case x if x > 3 =>
        println("4+, that's a party")
      case _ =>
        println("i'm guessing your number is zero or less")

  multilineExample()

  // Match expressions can be chained
  def chainedExample(i: Matchable) =
    val result = i match
      case odd: Int if odd % 2 == 1   => "odd"
      case even: Int if even % 2 == 0 => "even"
      case _                          => "not an integer"
    match
      case "even" => true
      case _      => false

    println(result)

  chainedExample(5)
  chainedExample(6)
  chainedExample("test")

  // Match expression can also follow a period, which simplifies matching on results returned by chained method calls

  List(1, 2, 3)
    .map(_ * 2)
    .headOption
    .match
      case Some(value) => println(s"The head is $value")
      case None        => println("The list is empty")

//////////////////////////////////////////////////////////////////
//////////////////////// try/catch/finally ///////////////////////
//////////////////////////////////////////////////////////////////

/*
Like Java, Scala has a try/catch/finally construct to let you catch and manage exceptions. For consistency,
Scala uses the same syntax that match expressions use and supports pattern matching on the different
possible exceptions that can occur.
 */

def tryCatch() =
  var text = ""
  try throw KeyException("KeyError")
  catch
    case ke: KeyException => ke.printStackTrace()
    case ioe: IOException => ioe.printStackTrace()
  finally println("came to the finally clause")

//////////////////////////////////////////////////////////////////////////
//////////////////////// Custom Control Structures ///////////////////////
//////////////////////////////////////////////////////////////////////////

/*
Scala allows you to define your own control strucutres. You can create methods that can be
used in a way similar to built-in control strucutres such as if / then / else or while loops.
This is primiarily achieved using `by-name parameters`

a `by-name` parameter is specified by prepending => to the type, like `body: => Unit`. Unlike a
normal by-value parameter, a by-name parameter is not evaluated when the method is called. Instead,
it is evaluated every time it is referenced within the method

This allows us to accept a block of code that is run on demand, which is essential for defining control logic
 */

def repeat(n: Int)(body: => Unit): Unit =
  if n > 0 then
    body
    repeat(n - 1)(body)

/*
Scala 3.3 introduces `boundary` and `break` in the scala.util package to provide a clearer, more structured way to
handle non-local returns or to “break” out of nested loops and control structures. This replaces the older
scala.util.control.Breaks and creating methods that throw exceptions for control flow.

To use it, you define a `boundary` block. Inside that block, you can call `break` to
effectively return a value from that block immediately.

Here is an example of a method that searches for the first index of a target element in a list of integers:
 */

import scala.util.boundary
import scala.util.boundary.break

def firstIndex(ls: List[Int], target: Int): Int =
  // boundary establishes a scope
  boundary:
    for (x, i) <- ls.zipWithIndex do
      if x == target then
        break(i) // break immediately exits the boundary block returns 1
    -1 // if the loop finishes without breaking, the code after the loop -1 is returned

// The above mechanism provides a structured alternative to using exceptions for control flow
// Note that the compiler will optimize `boundary` and `break` to simple jumps if possible
// (for example, when the break doesn’t happen within a nested method call). This avoids the
// overhead of exception handling
