// When writing code in an OOP style, your two main tools for data encapsulation are traits and classes.

package learning.domain

/////////////////////////////////////////////////
///////// Traits ///////////////////////////////
/////////////////////////////////////////////////

// Scala traits can be used as simple interfaces, but they can also contain abstract and concrete methods and fields,
// and they can have parameters, just like classes. They provide a great way for you to organize behaviors into small,
// modular units. Later, when you want to create concrete implementations of attributes and behaviors, classes and
// objects can extend traits, mixing in as many traits as needed to achieve the desired behavior.
trait Speaker:
  def speak(): String // no body = abstract; subclasses must implement

trait TailWagger:
  def startTail(): Unit = println(
    "tail is wagging"
  ) // Unit = like void, no useful return
  def stopTail(): Unit = println("tail is stopped")

trait Runner:
  def startRunning(): Unit = println("I'm running")
  def stopRunning(): Unit = print("Stopped running")

// Given those traits, here’s a Dog class that extends all of those traits while providing a behavior
// for the abstract speak method:
// "extends A, B, C" = mix in multiple traits (unlike Java, you can mix many)

class GermanSheperd(name: String) extends Speaker, TailWagger, Runner:
  def speak(): String = s"$name Woofed!"

class Jaguar(name: String) extends Speaker, TailWagger, Runner:
  def speak(): String = s"$name Meowed!"
  override def startRunning(): Unit = println(
    "Yeah ... I don't run"
  ) // override = replace trait's default
  override def stopRunning(): Unit = println("No need to stop")

def exampleTraitClasses() =
  val d = GermanSheperd("Rover") // "new" is optional for regular classes too
  println(d.speak())

  val c = Jaguar("Morris")
  println(c.speak())
  c.startRunning()
  c.stopRunning()

/////////////////////////////////////////////////
///////// CLASSES ///////////////////////////////
/////////////////////////////////////////////////

// Scala classes are used in OOP style. Constructor params with var become mutable fields.
// (Use val for immutable, or no prefix if param is only used during construction.)
// if you want them immutable (read-only), create them as val fields or use a `case` class

class Movie(var name: String, var director: String, var year: Int = 2000):
  println("initialization begins")
  // additional fields that are not part of constructors
  val rating = year % 10

  // method
  def getRating(): Int =
    rating

  println("initialization ends")

/*
Auxiliary Constructors - You can define a class to have multiple constructors so consumers of your class can build it in
different ways. For example, lets assume you need to write some code to model students in a college system. You need to be able
to construct `Student` instance in three ways:
  - With a name and government ID, -> when they first start the admissions process
  - With a name, goverment ID and an additional application date -> when they submit their application
  - wiht a name, government ID and their studFent ID, -> after they have been admitted
 */

import java.time.*

// [1] the primary constructor
class Student(var name: String, var govtId: String):
  private var _applicationDate: Option[LocalDate] = None
  private var _studentId: Int = 0

  // [2] constructor for when student has completed their application
  def this(name: String, govtId: String, applicationDate: LocalDate) =
    this(name, govtId)
    _applicationDate = Some(applicationDate)

  // [3] a constructor for when the student is approved
  // and now has a student id
  def this(name: String, govtId: String, studentId: Int) =
    this(name, govtId)
    _studentId = studentId

def auxiliaryClassConstructor() =
  val s1 = Student("Mary", "123")
  val s2 = Student("Mary", "123", LocalDate.now())
  val s3 = Student("Mary", "123", 456)

  println(s1)
  println(s2)
  println(s3)

/////////////////////////////////////////////////
///////// ABSTRACT CLASSES //////////////////////
/////////////////////////////////////////////////

// When you want to write a class, but you know it will have abstract members, you can use `trait` or `abstract class`
// Prior to Scala 3, when a base class needed to take constructor arguments, you’d declare it as an abstract class
// However, with Scala 3, traits can now have parameters, so you can now use traits in the same situation:

/*
Traits are more flexible to compose—you can mix in multiple traits, but only extend one class—and should be preferred to
classes and abstract classes most of the time. The rule of thumb is to use classes whenever you want to create
instances of a particular type, and traits when you want to decompose and reuse behaviour.
 */

abstract class Pet(name: String): // or in scala3:  `trait Pet(name String)`:
  def greeting: String
  def age: Int
  override def toString(): String =
    s"My name is $name, I Say $greeting, and I'm $age"

class Bird(name: String, var age: Int) extends Pet(name):
  val greeting = "kiikii"

val bird = Bird("Fido", 1) // My name is Fido, I Say kiikii, and I'm 1

/////////////////////////////
///////// ENUMS /////////////
/////////////////////////////

// An enumeration is a sum type: a value of type `Color` is exactly one of `Red`, `Green`, or `Blue`. Scala 3
// `enum` gives exhaustive `match`, `values`, and ordering without boilerplate.

// Parameterized enum: constructor args after the enum name are shared by all cases. Each case passes its own
// values via `extends Color(...)`. Marking a parameter as `val` (e.g. `val rgb`) exposes it as a field on every
// case—handy for small attached data (here a 24-bit `0xRRGGBB` color).

enum RgbColor(val rgb: Int):
  case Red extends RgbColor(0xff0000)
  case Green extends RgbColor(0x00ff00)
  case Blue extends RgbColor(0x0000ff)

// The enum body can declare methods, `private` helpers, and `val`s

enum Planet(val mass: Double, val radius: Double):
  private final val G = 6.67300e-11
  def surfaceGravity = G * mass / (radius * radius)
  def surfaceWeight(otherMass: Double) =
    otherMass * surfaceGravity

  case Mercury extends Planet(3.303e+23, 2.4397e6)
  case Venus   extends Planet(4.869e+24, 6.0518e6)
  case Earth   extends Planet(5.976e+24, 6.37814e6)

// enums also allow companion object 
object Planet:
  def main(args: Array[String]) = 
    val earthWeight = args(0).toDouble
    val mass = earthWeight / Earth.surfaceGravity
    for p <- values do println(s"Your weight on $p is ${p.surfaceWeight(mass)}")

// Compatibility with Java enums: `Enum[E]` is `java.lang.Enum` (on the classpath by default). Type parameter `E`
// is the enum type itself; the compiler supplies what the Java API expects.
enum Color extends Enum[Color]:
  case Red, Green, Blue

def enumExample() =
  import RgbColor.*
  println(Red.rgb)
  println(Green.rgb)
  println(Blue.rgb)
  import Planet.*
  println(Mercury.surfaceGravity)
  println(Earth.surfaceGravity)
  println(Color.Red.compareTo(Color.Green))
  Planet.main(args=Array("1000"))

///////////////////////////////////////////////
//////// CASE CLASSES /////////////////////////
///////////////////////////////////////////////

/*
Case classes are used to model immutable data structures.
A case class has func of a class and also has additional features baked in that make them useful for FP
When compiler sees the case keyword in front of a class it has these effects and benefits:
    A) constructor parameters are public val fields by default, so fields are immutable and
       accessor methods are generated for each param
    B) `unapply` method is generated, which lets you use case classes in more ways in `match` expressions `case Person(n, r) => ...`
    C) `copy` method is generated in the class. allows creation of updated copies of the obj w/o changing the original obj
    D) `equals` and `hashCode` methods are generated to implement equality, allowing you to use instances of case classes in Maps.
    E) default `toString` method generated

case classes support functional programming (FP):
  a. In FP, you try to avoid mutating data structures. It thus makes sense that constructor fields default to val.
  Since instances of case classes can’t be changed, they can easily be shared without fearing mutation or race conditions.

  b. Instead of mutating an instance, you can use the copy method as a template to create a new (potentially changed) instance.
  This process can be referred to as “update as you copy.”

  c. Having an unapply method auto-generated for you also lets case classes be used in advanced ways with pattern matching.
 */

case class Person(name: String, vocation: String)

def caseClassExample() =
  // Case classes can be used as patterns
  val christina = Person("Christina", "niece")
  christina match
    case Person(n, r) => println("name is " + n)

  // `equals` and `hashCode` methods generated for you
  val hannah = Person("Hannah", "niece")
  println(christina == hannah) // false

  // `toString` method
  println(christina) // Person(Christina,niece)

  // bulit-in `copy` method
  case class BaseballTeam(name: String, lastWorldSeriesWin: Int)
  val cubs1908 = BaseballTeam("Chicago Cubs", 1908)
  val cubs2016 = cubs1908.copy(lastWorldSeriesWin = 2016)
  println(s"cubs $cubs2016 is not equal to $cubs1908")

///////////////////////////////////////////////
//////// CASE OBJECTS /////////////////////////
///////////////////////////////////////////////

/*
Case objects are to objects what case classes are to classes: they provide a number of automatically-generated
methods to make them more powerful. They’re particularly useful whenever you need a singleton object that needs
a little extra functionality, such as being used with pattern matching in match expressions.

case bojects are useful when you need to pass immutable messages around.
 */

// For instance, if you’re working on a music player project, you’ll create a set of commands or messages like this:
// the word sealed forces us to define all possible extensions of the trait in the same file
sealed trait Message
case class PlaySong(name: String) extends Message
case class IncreaseVolume(amount: Int) extends Message
case class DecreaseVolume(amount: Int) extends Message
case object StopPlaying extends Message

def caseObjectExamplehandleMessages(message: Message): Unit = message match
  // use pattern matching to handle the incoming message to call different methods
  case PlaySong(name)         => println("calling the playSong(name) method")
  case IncreaseVolume(amount) =>
    println("calling the IncreaseVolume(amount) method")
  case DecreaseVolume(amount) =>
    println("calling the DecreaseVolume(-amount) method")
  case StopPlaying => println("calling the StopPlaying method")

/////////////////////////////////////////////////
///////// ADTs & FP DOMAIN MODELING /////////////
/////////////////////////////////////////////////

// Algebraic Data Types (ADTs) => define the data / a way of structuring data (https://rockthejvm.com/articles/algebraic-data-types-in-scala)
// Traits for functionality on the data

// ADTs provide no functionality, only data

// ADTs are commonly used in Scala. Simply put, an algebraic data type is any data that uses the Product or Sum pattern.
// They’re widely used in Scala mostly to how well they work with pattern matching and how easy it is to use them to
// Key benefit: ADTs make illegal states impossible to represent.

// ADTs allow us to construct complex data types by combining simpler ones.

////////////////////////////////////////////////
//////// SUM / ENUMERATION TYPES ///////////////
////////////////////////////////////////////////

// Sum type enumerates all the possible instances of a type; used when data can be represented with d/f choices
// XOR or exclusive OR relationship
// E.g pizza has three main attribues: Crust Size, Crust Type
// They are concisely modeled with enumerations, which are sum types that only contain singleton values

enum CrustSize:
  case Small, Medium, Large

enum CrustType:
  case Thin, Thick, Regular

import CrustSize.*

def sumTypeExample(): Unit =
  val currentCrustSize = Small

  currentCrustSize match
    case Small  => println("Small crust size")
    case Medium => println("Medium crust size")
    case Large  => println("Large crust size")

// the word sealed forces us to define all possible extensions of the trait in the same file
sealed trait Weather

// Why did we use a set of case objects? The answer is straightforward. We don’t need to have
// more than one instance of each extension of Weather. Indeed, there is nothing that distinguishes
// two instances of the Sunny type. So, we use object types that are translated by the language as idiomatic singletons.

// Moreover, using a case object instead of a simple object gives us a set of useful features,
// such as the unapply method, which lets our objects to work very smoothly with pattern matching,
// the free implementation of the methods equals, hashcode, toString, and the extension from Serializable.

case object Sunny extends Weather
case object Windy extends Weather
case object Rainy extends Weather
case object Cloudy extends Weather
case object Foggy extends Weather

// type Weather = Sunny + Windy + Rainy + Cloudy + Foggy (Sum Type)

def sumTypeExample2(w: Weather): String = w match
  case Sunny  => "Oh, it's such a beautiful sunny day :D"
  case Cloudy => "It's cloudy, but at least it's not raining :|"
  case Rainy  => "I am very sad. It's raining outside :("
  case Windy  => "It's so windy!!"
  case Foggy  => "I can't see a thing in this fog!"

// If missing any matches, you will have the following warning

// [warn] [...] match may not be exhaustive.
// [warn] It would fail on the following inputs: Foggy, Windy
// [warn]     def feeling(w: Weather): String = w match {
// [warn]                                       ^

////////////////////////////////////////////////
//////// PRODUCT TYPES /////////////////////////
////////////////////////////////////////////////

// Product Type is an ADT that only has one shape, ex Singleton object, represented in Scala by a `case object`
// or an immutable structure with accessible fields, represented by `case class`
// Associated with the AND operator

def productType(): Unit =
  val p = Person("Yohannes Berhane", "Engineer")

  // a good default toString method
  // Person = Person("Yohannes Berhane", "Engineer")

  // can access its fields, which are immutable
  println(p.name) // "Yohannes Berhane"
  // p.name = "Joe"         // error: can’t reassign a val field

  // when you need to make a change, use the `copy` method
  // to “update as you copy”
  val p2 = p.copy(name = "Elton John")
  println(p2) // : Person = Person(Elton John, Engineer)

// Imagine having to model a request to our forecast service
case class ForecastRequest(val latitude: Double, val longitude: Double)

/*
In the language of types, we can write the constructor as (Long, Long) => ForecastRequest. In other words,
the number of possible values of ForecastRequest is precisely the cartesian product of the possible values
for the latitude property AND all the possible values for the longitude property:

    type ForecastRequest = Long x Long (Product Type)
 */

///////////////////////////////////////////////////////////////////////
//////// HYBRID TYPES / Sum of Product types //////////////////////////
///////////////////////////////////////////////////////////////////////

sealed trait ForecastResponse // ForecastResponse is a Sum type because it is an Ok OR a Ko

case class Ok(weather: Weather) extends ForecastResponse

// The Ko type is a Product type because it has an error AND a description.
case class Ko(error: String, description: String) extends ForecastResponse

/*
val weatherReporter: Behavior[ForecastResponse] =
  Behaviors.receive { (context, message) =>
    message match {
      case Ok(weather: Weather) =>
        context.log.info(s"Today the weather is $weather")
      case Ko(e, d) =>
        context.log.info(s"I don't know what's the weather like, $d")
    }
    Behaviors.same
  }
 */

////////////////////////////////////////////////////
////////////// SUMMARY / NOTES /////////////////////
////////////////////////////////////////////////////

/*
case classes are also known as products
sealed traits (or sealed abstract classes) are also known as coproducts
case objects and Int, Double, String (etc) are known as values
 */

// Sum type (coproduct) can only be one of its values
// Weather (coproduct) = `Sunny` XOR `Windy` XOR `Rainy` XOR ...

// Product contains every type that is composed of
// Ko product = String x String

/*
We can define the complexity of a data type as the number of values that can exist.
Data types should have the least amount of complexity they need to model the information they carry.
 */

// Example
// Imagine we have to model a data structure that holds mutually exclusive configurations.
// For the sake of simplicity, let this configuration be three Boolean values:

case class ProductTypeExampleConfig(a: Boolean, b: Boolean, c: Boolean)
// Above product type has a complexity of 8

sealed trait SumTypeExampleConfig
case object A extends SumTypeExampleConfig
case object B extends SumTypeExampleConfig
case object C extends SumTypeExampleConfig

/*
The Sum type Config has the same semantic as its Product type counterpart, plus it has a smaller complexity,
and it does not allow 5 invalid states to exist. Also, as we said, the lesser values a type admits,
the easier the tests associated with it will be. Less is better :)
 */

// Improving our Ko product type to avoid invalid states and limit the number of values type can admit
// error: String, is replaced with Sum type that enumerates the possible types of available errors:
sealed trait Error
case object NotFound extends Error
case object Unauthorized extends Error
case object BadRequest extends Error
case object InternalError extends Error
// And so on...
case class ImprovedKo(error: Error, description: String)
    extends ForecastResponse
