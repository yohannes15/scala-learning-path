package learning.fundamentals

import java.io.StringWriter
import java.io.PrintWriter

/* 
In Scala 2, methods can be defined inside classes, traits, objects, case classes, and case objects. But it gets better: 
In Scala 3 they can also be defined outside any of those constructs; we say that they are “top-level” definitions, 
since they are not nested in another definition. In short, methods can now be defined anywhere.

def methodName(param1: Type1, param2: Type2): ReturnType =
    body goes here
end methodName   // this is optional. recommended for long methods

Notice that there’s no need for a return statement at the end of the method. Because almost everything in Scala is 
an expression—meaning that each line of code returns (or evaluates to) a value there’s no need to use return.
*/

def addInts(a: Int, b: Int): Int = a + b
def concatenate(s1: String, s2: String): String = s1 + s2

// converts its Throwable input parameter into a well-formatted String:
def getStackTraceAsString(t: Throwable): String =
    val sw = StringWriter()
    t.printStackTrace(new PrintWriter(sw))
    sw.toString
    
// default values
def makeConnection(url: String, timeout: Int = 5000): Unit = 
    println(s"url=$url, timeout=$timeout")

// extension keyword declares that youre about to define one or more extension methods on the parameter
// thats put in parentheses

extension (i: Int)
    def makeStringWithZeros(extraZeros: Int): String = i.toString() + ("0" * extraZeros)

case class Rectangle(length: Double, width: Double)
extension (r: Rectangle)
  def area: Double = r.length * r.width

def testExtension(): Unit = 
    println(1.makeStringWithZeros(5))
    println(10.makeStringWithZeros(5))

/* A suggestion about methods that take no parameters

When a method takes no parameters, it’s said to have an arity level of arity-0. 
Similarly, when a method takes one parameter it’s an arity-1 method. 

When you create arity-0 methods:
    - If the method performs side effects, such as calling println, declare the method with empty parentheses
    - If the method does not perform side effects, such as getting the size of a collection, 
    which is similar to accessing a field on the collection, leave the parentheses off

For example, this method performs a side effect, so it’s declared with empty parentheses:

Doing this requires callers of the method to use open parentheses when calling the method:

speak     // error: "method speak must be called with () argument"
speak()   // prints "hi"

While this is just a convention, following it dramatically improves code readability:
It makes it easier to understand at a glance that an arity-0 method performs side effects.
*/

def speak() = println("hi") // has side effect
def speakA = "hi" 


/* Controlling Visibility in classes

In classes, objects, traits, and enums, Scala methods are public by default

Methods can also be marked as private. This makes them private to the current class, 
so they can’t be called nor overridden in subclasses:

If you want to make a method private to the current class and also allow subclasses to call it or override it, 
mark the method as protected, as shown with the speak method in this example:

The `protected` means:

    The method (or field) can be accessed by other instances of the same class
    It is not visible by other code in the current package
    It is available to subclasses

*/

class Tiger:
    def speak() = println("rarr") // public method

val t = Tiger()
// t.speak()      // prints "rarr"  // instance created here can access the speak method:

class Mammal:
  private def breathe() = println("I’m breathing") // private method only accessible in this class
  def walk() =
    breathe()
    println("I’m walking")
  protected def speak() = println("Hello?") // subclass can call / ovverride it

class Lion extends Mammal:
  override def speak() = println("Lion Meow")


// this method won’t compile
// class Cat extends Animal:
//   override def breathe() = println("Yo, I’m totally breathing")


def methodVisibility() =
    val lion = Lion()
    lion.walk()
    lion.speak()
    // lion.breathe()   // won’t compile because it’s private


/* 
There’s even more to know about methods, including how to:
    - Call methods on superclasses
    - Define and use by-name parameters
    - Write a method that takes a function parameter
    - Create inline methods
    - Handle exceptions
    - Use vararg input parameters
    - Write methods that have multiple parameter groups (partially-applied functions)
    - Create methods that have type parameters
 */
