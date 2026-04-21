package learning.typeclasses

import cats.Show

/**
  * Show is an alternative to the Java `toString` method. It is defined
  * by a single function:
            
            def show[A](a: A): String = ???

  * toString is defined on `Any`(Java's Object) and can therefore be 
  * called on anything! This is unwanted behaviour most often, as the
  * standard implementation of `toString` on non case classes is mostly
  * gibberish: eg `toStringIsGiberish` function below.
  * 
  * The fact that toStringIsGiberish code compiles is a design flaw of the
  * Java API. We want to make things like this impossible, by offering the
  * `toString` equivalent as a type class, instead of the root of the class
  * hierarchy. 
  * 
  * In short, `Show` type class allows us to only have String-conversions
  * defined for the data types we actually want.
  */
  object LearningShow:
    /* Helper Functions 
    -------------------------
    To make things easier, Cats defines a few helper functions to make
    creating `Show` instances easier:
        1. creates an instance of Show using the provided function
        2. creates an instance of Show using object toString
    */
    def show[A](f: A => String): Show[A] = f(_) // 1
    def fromToString[A]: Show[A] = _.toString   // 2

    case class Person(name: String, age: Int)
    given showPerson: Show[Person] = 
        show(person => person.name)

    case class Department(id: Int, name: String)
    given showDep: Show[Department] = 
        fromToString

    /*
    This still may not seem useful to you, because case classes already 
    automatically implement `toString`, while `show` would have to be implemented
    manually for each case class. Thankfully there are two options to make this easier:
        
    1) Cats offers `Show` syntax to make working with it easier. This includes the
       `.show` method which can be called on anything with a Show instance in scope
        
    2) `kittens` library offers a lot of type class instances including `Show` 
       can be derived automatically!

    It also includes a String interpolator, which works just like the standard s"..."
    interpolator, but uses `Show` instead of `toString`
    **/

@main def showExamples(): Unit = 
    def toStringIsGiberish() = 
        println((new {}).toString)
        //String = "learning.typeclasses.Show$package$$anon$1@6956de9"

    import LearningShow.{*, given}

    val person = Person("John", 20)
    println(s"showing person: ${showPerson.show(person)}")
    val dept = Department(15, "Tech")
    println(s"showing department: ${showDep.show(dept)}")

    import cats.syntax.all.*
    println(s"simpler show syntax person.show: ${person.show}")
    println(s"simpler show syntax dept.show: ${dept.show}")
    
    val showInterpolator = show"$person works at $dept"
    println(s"showInterpolator example: $showInterpolator")
    

    
