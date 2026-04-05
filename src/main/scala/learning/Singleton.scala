package learning

// In Scala, the object keyword creates a Singleton object. Put another way, 
// an object defines a class that has exactly one instance. Its init lazily when
// its members are referenced, similar to a `lazy val`. 

// Objects can also contain fields, which are also accessed like static members:
object MathConstants:
    val PI = 3.14159
    val E = 2.71828

// Use cases

/////////////////////////////////
//// 1) UTILITY METHODS /////////
/////////////////////////////////

// because obj is a singleton, its methods can be accessed like static methods. 

object StringUtils:
    def isNullOrEmpty(s: String): Boolean = s == null || s.trim().isEmpty()
    def leftTrim(s: String): String = s.replaceAll("^\\s+", "")
    def rightTrim(s: String): String = s.replaceAll("\\s+$", "")
    /**
     * Returns true if the given string contains any whitespace
     * at all. Assumes that `s` is not null.
    */
    def containsWhitespace(s: String): Boolean = s.matches(".*\\s.*")
    /**
     * Returns a string that is the same as the input string, but
     * truncated to the specified length.
    */
    def truncate(s: String, length: Int): String = s.take(length)
    /**
    * Returns true if the string contains only letters and numbers.
    */
    def lettersAndNumbersOnly_?(s: String): Boolean =
        s.matches("[a-zA-Z0-9]+")

end StringUtils

// Importing in Scala is very flexible, and allows us to import all members of an object:
import StringUtils.*
// Or import just some members
import StringUtils.{truncate, containsWhitespace}

def singeltonExample(): Unit =
    val x = null
    println(s"$x is ${if isNullOrEmpty(x) then "Empty" else "Not Empty"}")

    val y = "Yohan"
    println(s"$y is ${if isNullOrEmpty(y) then "Empty" else "Not Empty"}")

    println(truncate("Chuck Bartowski", 5))  // "Chuck"

    println(MathConstants.PI)   // 3.14159

///////////////////////////////////
//// 2) Companion Objects /////////
///////////////////////////////////

/* 
 Companion objects and companion classes:

 - A companion object is an `object` with the same name as a `class` in the same file.
 - The class and its companion object can access each other's private members.
 - Use a companion object for: 
    a. factory methods, constants, or utilities that are related to the class but don't belong to any particular instance.
    b. group "Static" methods under a namespace. Methods can be public or private
    c. holding `apply` methods -> which thanks to some syntactic sugar, work as factor methods to construct new instances
    d. holding `unapply` methods -> used to deconstruct objects, such as with pattern matching. 

 - Companion objects are used for methods and values that are not specific to instances of the companion class. 

 This example shows:
 1) `Circle` (a class) calling `calculateArea` which is defined as a private method
    in the companion `object Circle`.
 2) An `import Circle.*` inside the class which brings the companion's members into
    the class scope so they can be referenced without qualifying with `Circle.`.
    (The import is optional here because companions already have access to each
     other's private members; the import simply allows unqualified use.)
*/

import scala.math.*

// The class represents an instance of a circle with a radius.
// It uses the companion's private calculateArea method to compute the area.
// the class Circle has a member named area which is specific to each instance,
class Circle(radius: Double):
    // Bring companion members into scope so we can call `calculateArea` without
    // qualifying it as `Circle.calculateArea`. This is a convenience, not a
    // requirement for companion access.
    import Circle.*
    def area: Double = calculateArea(radius)

// Companion object for `Circle`.
// Holds helper/utility code related to Circle instances. Its `calculateArea`
// is private to the companion pair but accessible from the `Circle` class.
// companion object has a method named calculateArea that’s 
// - (a) not specific to an instance, and
// - (b) is available to every instance

object Circle:
    // Private helper used by the class above. Marked private because it's an
    // implementation detail that shouldn't be exposed outside the companion pair.
    // because calculateArea is private, it can’t be accessed by other code, but as shown, 
    // it can be seen by instances of the Circle class.
    private def calculateArea(radius: Double): Double =
        Pi * pow(radius, 2.0)

// Heres a look at how `apply` methods can be used as factory methods to create new objects
class Human:
    var name = ""
    var age = 0
    override def toString = s"$name is $age years old"

object Human:

    // a one-arg factory method
    def apply(name: String): Human = 
        var p = new Human
        p.name = name
        p 

    // a two-arg factory method
    def apply(name: String, age: Int): Human =
        var p = new Human
        p.name = name
        p.age = age
        p

def companionExample(): Unit =
    val circle = Circle(5.0)
    println(circle.area)

    val joe = Human("Joe")
    val fred = Human("Fred", 29)
    println(joe)
    println(fred)
/////////////////////////////////////
//// 3) Modules from Traits /////////
/////////////////////////////////////

// Traits in Scala are like interfaces that may also provide default implementations.
// They define behavior that multiple concrete implementations can share.
//
// Define small traits that provide single responsibilities (good modular design).
trait AddService:
    def add(a: Int, b: Int) = a + b

trait MultiplyService:
    def multiply(a: Int, b: Int) = a * b

// An `object` can extend one or more traits and provide a single shared implementation.
// Because `object` is a singleton, `MathService` becomes a module exposing add/multiply.
// This is a common pattern for grouping related services or utilities.
object MathService extends AddService, MultiplyService

// Example usage: import the module's members to call them unqualified.
def modulesFromTraits(): Unit = 
    // `import MathService.*` brings add/multiply into local scope for convenience.
    import MathService.* 
    println(add(1,1))       // prints 2
    println(multiply(2,2))  // prints 4
