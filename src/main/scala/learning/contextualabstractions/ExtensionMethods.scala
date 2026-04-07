package learning.contextualabstractions

// In Scala 2, a similar result could be achieved with implicit classes.

/* 
Extension methods let you add methods to a type after the type is defined
i.e. they let you add new methods to closed classes.
*/

case class Circle(x: Double, y: Double, radius: Double)

/*  
Imagine you need a circumference method.

Before the concept of term inference was introduced into programming languages, 
the only thing you could do was write a method in a separate class or object like this:
then you use the method like this
*/ 

// bad implementation. No use of term inference and extension methods
object CircleHelpers:
    def circumference(c: Circle): Double = c.radius * math.Pi * 2
    def diameter(c: Circle): Double = c.radius * 2
    def area(c: Circle): Double = math.Pi * c.radius * c.radius

// But with extension methods you can create a circumference method to work on Circle instances:
// Circle is the type that the extension method circumference will be added to
// The c: Circle syntax lets you reference the variable c in your extension method(s)
extension (c: Circle)
  def circumference: Double = c.radius * math.Pi * 2
  // other extension methods
  def diameter: Double = c.radius * 2
  def area: Double = math.Pi * c.radius * c.radius


def extensionExample() = 
    // Bad example 
    val aCircle = Circle(2, 3, 5)
    val res = CircleHelpers.circumference(aCircle)
    println(s"""
        The circumference is ${CircleHelpers.circumference(aCircle)}. 
        Diameter is ${CircleHelpers.diameter(aCircle)}
        area is ${CircleHelpers.area(aCircle)}
        """
    )
    // Extension example
    println(s"""
        The circumference is ${aCircle.circumference}. 
        Diameter is ${aCircle.diameter}
        area is ${aCircle.area}
        """
    )

/*
Importing extension method
------------------------------------
Imagine, that circumference is defined in package `lib`, you can import it by

    `import lib.circumference
     aCircle.circumference`

The compiler also supports you if the import is missing by showing a detailed 
compilation error message such as the following:

`value circumference is not a member of Circle, but could be made available 
 as an extension method. The following import might fix the problem:

   `import lib.circumference`
*/
