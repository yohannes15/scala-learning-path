package learning.domain

/* introduction to domain modeling using functional programming (FP) in Scala 3 

When modeling the world around us with FP, you typically use these Scala constructs:
    - Enumerations
    - Case classes
    - Traits

If you’re not familiar with algebraic data types (ADTs) and their generalized version (GADTs), 
you may want to read the Algebraic Data Types https://docs.scala-lang.org/scala3/book/types-adts-gadts.html 
section before reading this section.

In FP, the data and the operations on that data are two separate things; 
you aren’t forced to encapsulate them together like you do with OOP.

The concept is similar to numerical algebra. When you think about whole numbers whose values are
greater than or equal to zero, you have a set of possible values that looks like this:

    `0, 1, 2 ... Int.MaxValue`

Ignoring the division of whole numbers, the possible operations on those values are:

    `+, -, *`

In FP, business domains are modeled in a similar way:

    You describe your set of values (your data)
    You describe operations that work on those values (your functions)

Data in FP simply is! 
Separating functionality from your data lets you inspect your data without having to worry about behavior.

In Scala, describing the data model of a programming problem is simple:

    (Sum Types)     => Data types that describe different alternatives (like CrustSize).
                    =>  Aka `Describing Alternatives`

    (Product Types) => Data Types that aggregate multiple components (like Pizza) are also sometimes referred to as product types.
                    => Aka `Describing Compound Data`


Here we’ll model the data and operations for a “pizza” in a pizza store
 */

enum Topping:
  case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions

import CrustSize.*, CrustType.*

case class Pizza(
    crustSize: CrustSize,
    crustType: CrustType,
    toppings: Seq[Topping]
)

/* 
And that’s it. That’s the data model for an FP-style pizza system. 
This solution is very concise because it doesn’t require the operations on a pizza to be combined with the data model.
The data model is easy to read, like declaring the design for a relational database. 
It is also very easy to create values of our data model and inspect them:

    val myFavPizza = Pizza(Small, Regular, Seq(Cheese, Pepperoni))
    println(myFavPizza.crustType) // prints Regular

We might go on in the same way to model the entire pizza-ordering system. 
Here are a few other case classes that are used to model such a system:
 */

case class Address(
  street1: String,
  street2: Option[String],
  city: String,
  state: String,
  zipCode: String
)

case class Customer(
  name: String,
  phone: String,
  address: Address
)

case class Order(
  pizzas: Seq[Pizza],
  customer: Customer
)

/* 
In his book, Functional and Reactive Domain Modeling, Debasish Ghosh states that where OOP practitioners describe their 
classes as “rich domain models” that encapsulate data and behaviors, FP data models can be thought of as 
“skinny domain objects.” This is because—as this lesson shows—the data models are defined as case classes with attributes, 
but no behaviors, resulting in short and concise data structures.
 */

/*******************************************************************
 Modeling the Operations
******************************************************************/

// we use methods / functions that operate on values of the data for operations on models
/* 
You can notice how the implementation of the function simply follows the shape of the data: 
since Pizza is a case class, we use pattern matching to extract the components and call helper
functions to compute the individual prices.

An important point about all functions shown below is that they are pure functions: 
    - they do not mutate any data or have other side-effects (like throwing exceptions or writing to a file). 
    - All they do is simply receive values and compute the result.
 */

def pizzaPrice(p: Pizza): Double = p match
    case Pizza(crustSize, crustType, toppings) =>
        val base = 6.00
        val crust = crustPrice(crustSize, crustType)
        val tops = toppings.map(toppingPrice).sum
        base + crust + tops

def toppingPrice(t: Topping): Double = 
    import Topping.*
    t match
        case Cheese | Onions => 0.5
        case Pepperoni | BlackOlives | GreenOlives => 0.75

def crustPrice(s: CrustSize, t: CrustType): Double = 
    (s, t) match
        // if the crust size is small or medium,
        // the type is not important
        case (Small | Medium, _) => 0.25
        case (Large, Thin) => 0.50
        case (Large, Regular) => 0.75
        case (Large, Thick) => 1.00

/*******************************************************************
 How to Organize Functionality
 There are several different ways to implement and organize behaviors:

    Define your functions in `companion` objects
    Use a `modular` programming style
    Use a `functional objects` approach
    Define the functionality in `extension` methods

******************************************************************/

/* --- Companion Object -----
- A first approach is to define the behavior, the functions, in a companion object.
- a companion object is an object that has the same name as a class, and is declared in the same file as the class.
- With this approach, in addition to the enumeration or case class you also define an equally named companion object that contains the behavior.
 
 Benefits
- It associates functionality with data and makes it easier to find for programmers (and the compiler).
- It creates a namespace and for instance lets us use price as a method name without having to rely on overloading.
- The implementation of Topping.price can access enumeration values like Cheese without having to import them.

 Disadvantages
- Tightly couples the func to your data model, the companion obj needs to be defined in the same file as your case class
- Might be unclear where to define functions like crustPrice that could equally well be placed in a companion object of 
  CrustSize or CrustType
 */ 

// the companion object of case class Pizza
object Pizza:
    // the implementation of `pizzaPrice` from above here
    def price(p: Pizza): Double = p match
        case Pizza(crustSize, crustType, toppings) =>
            val base = 6.00
            val crust = crustPrice(crustSize, crustType)
            val tops = toppings.map(toppingPrice).sum
            base + crust + tops


// the companion object of enumeration Topping
object Topping:
    // the implementation of `toppingPrice` above
    def price(t: Topping): Double = t match
        case Cheese | Onions => 0.5
        case Pepperoni | BlackOlives | GreenOlives => 0.75


/* --- Modules (Modular approach) -----

The book, Programming in Scala, defines a module as, “a ‘smaller program piece’ with a 
well-defined interface and a hidden implementation.” Let’s look at what this means.

First thing to think about are the Pizzas “behaviors”. 
When doing this, you sketch a PizzaServiceInterface trait like this:

When you write a pure interface like this, you can think of it as a contract that states, 
“all non-abstract classes that extend this trait must provide an implementation of these services.”

In the first step, you sketch the contract of your API as an interface. 
In the second step you create a concrete implementation of that interface.
In some cases you’ll end up creating multiple concrete implementations of the base interface.

While this two-step process of creating an interface followed by an implementation isn’t always necessary, 
explicitly thinking about the API and its use is a good approach.

With everything in place you can use your Pizza class and PizzaService:
*/

trait PizzaServiceInterface:
  def price(p: Pizza): Double
  def addTopping(p: Pizza, t: Topping): Pizza
  def removeAllToppings(p: Pizza): Pizza
  def updateCrustSize(p: Pizza, cs: CrustSize): Pizza
  def updateCrustType(p: Pizza, ct: CrustType): Pizza

object PizzaService extends PizzaServiceInterface:

  def price(p: Pizza): Double =  p match
    case Pizza(crustSize, crustType, toppings) =>
        val base = 6.00
        val crust = crustPrice(crustSize, crustType)
        val tops = toppings.map(toppingPrice).sum
        base + crust + tops

  def addTopping(p: Pizza, t: Topping): Pizza =
    p.copy(toppings = p.toppings :+ t)

  def removeAllToppings(p: Pizza): Pizza =
    p.copy(toppings = Seq.empty)

  def updateCrustSize(p: Pizza, cs: CrustSize): Pizza =
    p.copy(crustSize = cs)

  def updateCrustType(p: Pizza, ct: CrustType): Pizza =
    p.copy(crustType = ct)

end PizzaService

/* --- Functional Objects -----

In the book, Programming in Scala, the authors define the term, “Functional Objects” as
“objects that do not have any mutable state”. This is also the case for types in scala.collection.immutable. 
For example, methods on List do not mutate the interal state, but instead create a copy of the List as a result.

You can think of this approach as a “hybrid FP/OOP design” because you:

    Model the data using immutable case classes.
    Define the behaviors (methods) in the same type as the data.
    Implement the behavior as pure functions: They don’t mutate any internal state; rather, they return a copy.

    This really is a hybrid approach: 
        like in an OOP design, the methods are encapsulated in the class with the data, 
        but as typical for a FP design, methods are implemented as pure functions that don’t mutate the data

Notice that unlike the previous approaches, because these are methods on the Pizza class, 
they don’t take a Pizza reference as an input parameter. Instead, they have their own reference to the current pizza instance as this.
*/

case class PizzaModular(
  crustSize: CrustSize,
  crustType: CrustType,
  toppings: Seq[Topping]
):

  // the operations on the data model
  def price: Double = this match
    case PizzaModular(crustSize, crustType, toppings) =>
        val base = 6.00
        val crust = crustPrice(crustSize, crustType)
        val tops = toppings.map(toppingPrice).sum
        base + crust + tops

  def addTopping(t: Topping): PizzaModular =
    this.copy(toppings = this.toppings :+ t)

  def removeAllToppings: PizzaModular =
    this.copy(toppings = Seq.empty)

  def updateCrustSize(cs: CrustSize): PizzaModular =
    this.copy(crustSize = cs)

  def updateCrustType(ct: CrustType): PizzaModular =
    this.copy(crustType = ct)


/* --- Extension Methods -----

Finally, we show an approach that lies between the first one (defining functions in the companion object) 
and the last one (defining functions as methods on the type itself).

Extension methods let us create an API that is like the one of functional object, 
without having to define functions as methods on the type itself. This can have multiple advantages:

    Our data model is again very concise and does not mention any behavior.
    We can equip types with additional methods retroactively without having to change the original definition.
    Unlike companion objects or direct methods on the types, extension methods can be defined externally in another file.

In the below code, we define the different methods on pizzas as extension methods. 
With extension (p: Pizza) we say that we want to make the methods available on instances of Pizza. The receiver in this case is p.

Typically, if you are the designer of the data model, you will define your extension methods in the companion object. 
This way, they are already available to all users. Otherwise, extension methods need to be imported explicitly to be usable.
*/

extension (p: Pizza)
    def price: Double =
        pizzaPrice(p) // implementation from above

    def addTopping(t: Topping): Pizza =
        p.copy(toppings = p.toppings :+ t)

    def removeAllToppings: Pizza =
        p.copy(toppings = Seq.empty)

    def updateCrustSize(cs: CrustSize): Pizza =
        p.copy(crustSize = cs)

    def updateCrustType(ct: CrustType): Pizza =
        p.copy(crustType = ct)


def fpModelingExample() = 
    import CrustSize.*, CrustType.*, Topping.*
    val piz = Pizza(Large, Thin, List(Pepperoni, Cheese))
    println(s"The pizza price is ${{pizzaPrice(piz)}}")

    // Companion Object approach
    val pizza1 = Pizza(Small, Thin, Seq(Cheese, Onions))
    println(s"The pizza price is ${{Pizza.price(pizza1)}}")

    // Modular approach
    val p = Pizza(Small, Thin, Seq(Cheese))
    // use the PizzaService methods. Can import to make syntax simpler
    val p1 = PizzaService.addTopping(p, Pepperoni)
    val p2 = PizzaService.addTopping(p1, Onions)
    val p3 = PizzaService.updateCrustType(p2, Thick)
    val p4 = PizzaService.updateCrustSize(p3, Large)
    println(PizzaService.price(p4)) // prints 8.75

    // Functional object approach
    val pm = PizzaModular(Small, Thin, Seq(Cheese))
    pm.addTopping(Pepperoni)
    pm.updateCrustType(Thick)
    println(pm.price)

    // Extension methods
    val pe = Pizza(Small, Thin, Seq(Cheese))
    pe.addTopping(Pepperoni)
    pe.updateCrustType(Thick)
    println(pe.price)

/* 
Summary

Defining a data model in Scala/FP tends to be simple: Just model 
    - variants of the data with enumerations and
    - compound data with case classes. 
Then, to model the behavior: 
    - define functions that operate on values of your data model. We have seen different ways to organize your functions:

    You can put your methods in companion objects
    You can use a modular programming style, separating interface and implementation
    You can use a “functional objects” approach and store the methods on the defined data type
    You can use extension methods to equip your data model with functionality

 */
