package example

import scala.io.StdIn.readLine
import java.io.IOException

@main
def hello(name: String): Unit =
  println(s"Scala ${name} dev container is ready.")

  ///// Basics.scala ////////

  // helloInteractive()
  // matching()
  // tryCatchFinally()
  // whileLoop()

  ///// DomainModeling.scala ////////

  // exampleTraitClasses()
  // sumTypeExample()
  // println(sumTypeExample2(Sunny))
  // productType()

  /////// Methods ///////
  // testExtension()

  /////// First Class Functions ////////
  // higherOrderFunc()

  /////// Singleton /////////
  // singeltonExample()
  // println(companionExample())
  // modulesFromTraits()

  ////// Collections ///////
  // foldReduceDemo()
  // tupleExample()

  ////// Types ////////////
  instancesOfAnyExample()

def helloInteractive() =
  println("Please enter your name")
  // Learning Interactive Input
  val name = readLine()
  // Control Structures (if then else if then else)
  if name.length > 4 then
    println("You have a long name")
  else
    println("You have a short name")
  // String interpolation
  println(s"Hello, $name!")

  ////////////////////////////////////////////////
  //////////////////// Map //////////////////////
  /////////////////////////////////////////////// 
  val allowedChoices = Map(
    "m" -> "Male",
    "f" -> "Female",
    "na" -> "Prefer not to say"
  )
  println(s"What is your gender? allowed choices: ${allowedChoices.keys}")
  val gender = readLine()

  if allowedChoices.contains(gender) then
    println(s"Thanks saving $name as gender ${allowedChoices(gender)}")
  else
    println(s"Invalid choice. Re run program")
    

  /////////////////////////////////////////////////////
  //////////////////// For Loops //////////////////////
  /////////////////////////////////////////////////////
  val ints = List(1, 2, 3, 4, 5)
  for
    i <- ints
    // guarding allowed
    if i > 2
  do
    println(i)

  for
    // You can use multiple generators and guards. 
    // This loop iterates over the numbers 1 to 3, and for each number it also iterates over 
    // the characters a to c. However, it also has two guards, so the only time the print 
    // statement is called is when i has the value 2 and j is the character b:
    i <- 1 to 3
    j <- 'a' to 'c'
    if i == 2
    if j == 'b'
  do
    println(s"i = $i, j = $j")

  // yield
  // When you use the yield keyword instead of do, you create for expressions 
  // which are used to calculate and yield results.

  val doubles = for i <- ints yield i * 2
  // val doubles = for (i <- ints) yield i * 2
  // val doubles = for (i <- ints) yield (i * 2)
  // val doubles = for { i <- ints } yield (i * 2)
  println(doubles) // List(2, 4, 6, 8, 10)

  val names = List("chris", "ed", "mark")
  val capNames = for name <- names yield name.capitalize
  println(capNames) // List("Chris", "Ed", "Mark")

  val fruits = List("apple", "banana", "lime", "orange")
  val fruitsLengths = for 
    f <- fruits
    if f.length > 4
  yield
    s"$f with length ${f.length}"
  
  print(fruitsLengths) // List(apple with length 5, banana with length 6, orange with length 6)
  
  

/////////////////////////////////////////////////////
//////////////////// Match //..//////////////////////
/////////////////////////////////////////////////////
def matching() =

  val i = 1

  // like a switch statement
  i match
    case 1 => println("one")
    case 2 => println("two")
    case _ => println("other")

  // but also can be used as an expression and return result that can bind to variable

  val result = i match
    case 1 => "one"
    case 2 => "two"
    case _ => "other"

  // match can used on any data type
  // case class Person(name: String) is a small data holder that gives you a constructor, fields, equality, toString, 
  // and built‑in pattern matching support, so you can focus on the data shape instead of boilerplate.
  case class Person(name: String)

  val p = Person("Fred")

  // pattern matching can be used on case classes
  // Scala checks each case in order; first match wins.
  // Person(name) extracts the name field into a variable; "if" adds a guard.
  p match
    case Person(name) if name == "Fred" =>
      println(s"$name says, Yubba dubba doo")

    case Person(name) if name == "Bam Bam" =>
      println(s"$name says, Bam bam!")

    case _ => println("Watch the Flintstones!")  // fallback: any other value

  // can be used to test a variable against many different types of patterns
  // Matchable is a type that can be matched against/ supports pattern matching
  def getClassAsString(x: Matchable): String = 
    x match
      case s: String => s"String: $s"
      case i: Int => s"Int: $i"
      case d: Double => s"Double: $d"
      case b: Boolean => s"Boolean: $b"
      case l: List[?] => s"List: $l"
      case m: Map[?, ?] => s"Map: $m"
      case _ => "Unknown"

  println(getClassAsString("Hello")) // String: Hello
  println(getClassAsString(123)) // Int: 123
  println(getClassAsString(123.45)) // Double: 123.45
  println(getClassAsString(true)) // Boolean: true
  println(getClassAsString(List(1, 2, 3))) // List: List(1, 2, 3)
  println(getClassAsString(Map("a" -> 1, "b" -> 2))) // Map: Map(a -> 1, b -> 2)
    
  // There’s much more to pattern matching in Scala. Patterns can be nested, results of patterns can be bound, 
  // and pattern matching can even be user-defined. 
  // See the pattern matching examples in the https://docs.scala-lang.org/scala3/book/control-structures.html


/////////////////////////////////////////////////////////////////
//////////////////// Try/Catch/Finally //..//////////////////////
/////////////////////////////////////////////////////////////////
def tryCatchFinally() =
  println("Type 'io' or 'nfe' to throw an exception otherwise no error thrown")
  val text = readLine()
  
  def writeTextToFile(text: String): Unit = 
    text match
      case "io" => throw new IOException("IO Exception thrown")
      case "nfe" => throw new NumberFormatException("NumberFormat Exception thrown")
      case _ => println("File written successfully!")

  try
    writeTextToFile(text)
  catch
    case ioe: IOException => println("Got an IOException")
    case nfe: NumberFormatException => println("Got a NumberFormatException")
  finally
    println("Clean up your resources here")

///////////////////////////////////////////////////////////
//////////////////// While Loops //..//////////////////////
///////////////////////////////////////////////////////////

def whileLoop() =
  // multiline
  println("While loop from [1, 3)")
  var x = 1

  while
    x < 3
  do
    println(x)
    x += 1

  // oneline
  // while x >= 0 do x = f(x)
