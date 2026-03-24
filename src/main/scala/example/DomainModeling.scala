// When writing code in an OOP style, your two main tools for data encapsulation are traits and classes.

package example

// Traits

// Scala traits can be used as simple interfaces, but they can also contain abstract and concrete methods and fields, 
// and they can have parameters, just like classes. They provide a great way for you to organize behaviors into small,
// modular units. Later, when you want to create concrete implementations of attributes and behaviors, classes and
// objects can extend traits, mixing in as many traits as needed to achieve the desired behavior.
trait Speaker:
    def speak(): String // has no body so its abstract

trait TailWagger:
    def startTail(): Unit = println("tail is wagging")
    def stopTail(): Unit = println("tail is stopped")

trait Runner:
    def startRunning(): Unit = println("I'm running")
    def stopRunning(): Unit = print("Stopped running")

// Given those traits, here’s a Dog class that extends all of those traits while providing a behavior
// for the abstract speak method:

class Dog(name: String) extends Speaker, TailWagger, Runner:
    def speak(): String = s"$name Woofed!"

class Cat(name: String) extends Speaker, TailWagger, Runner:
    def speak(): String = s"$name Meowed!"
    override def startRunning(): Unit = println("Yeah ... I don't run")
    override def stopRunning(): Unit = println("No need to stop")


def exampleTraitClasses() =
    val d = Dog("Rover")
    println(d.speak())

    val c = Cat("Morris")
    println(c.speak())
    c.startRunning()
    c.stopRunning()

