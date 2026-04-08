package learning.fundamentals

/* 
Scala uses packages to create namespaces that let you modularize programs
and help prevent namespace collisions. Supports both package-naming style
used by Java and also the curly brace namespace used by C++ and C#

- Import packages, classes, objects, traits and methods
- Place import statements anywhere
- Hide and rename members when you import them 
 */


/*****************************************************************************
******************************************************************************
Creating A Package
------------------------------------
- created by declaring one or more packages names at the top of a scala file
- names should be all lower case
- formal naming convention is <top-level-domain>.<domain-name>.<project-name>.<module-name>.
- Although it’s not required, package names typically follow directory structure names
  Person class in this proj will be found in
        `MyApp/src/main/scala/com/acme/myapp/model/Person.scala` file.

For example, when your domain name is `acme.com` and you’re working in the 
`model` package of an application named `myapp`, your package declaration 
looks like this:

    package com.acme.myapp.model

    class Person ...

The syntax shown above applies to the entire source file:
    - all the defintions in the Person.scala belong to package com.acme.myapp.model

It is possible to write package clauses that apply only to the definitions they contain:

`package users:

  package administrators:  // the full name of this package is users.administrators
    class AdminUser        // the full name of this class is users.administrators.AdminUser

  package normalusers:     // the full name of this package is users.normalusers
    class NormalUser       // the full name of this class is users.normalusers.NormalUser`

Note package names are followed by a colon, and that the definitions within a package are indented.
This allows for 
    - package nesting
    - more obvious control of scope and encapsulation, especially within the same file.

------------------------------------
Import statements, Part 1
------------------------------------

Import statements fall into two main categories:

    - Importing classes, traits, objects, functions, and methods
    - Importing given clauses / instances

Examples
    mport users.*                                          // import everything from the `users` package
    import users.User                                      // import only the `User` class
    import users.{User, UserPreferences}                   // import only two selected members

     // rename a member as you import it
    import users.{UserPreferences as UPrefs}              
    import java.util.{List as JavaList}

    // Rename the Date and HashMap classes as shown, and import 
    // everything else in the java.util package without renaming any other members.
    import java.util.{Date as JDate, HashMap as JHashMap, *}

- Import clauses are not required for accessing members of the same package.
- You can also hide members during the import process.

    // hides the java.util.Random class, while importing everything else in the java.util package
    import java.util.{Random as _, *}

    val r = new Random   // won’t compile
    new ArrayList        // works
    
    // hiding multiple members
    import java.util.{List as _, Map as _, Set as _, *}
    // Because those Java classes are hidden, you can also use the Scala 
    // List, Set, and Map classes without having a naming collision

- You can use imports anywhere 
Example

`    package foo

    class ClassA:
    import scala.util.Random   // inside ClassA
    def printRandom(): Unit =
        val r = new Random
        // more code here...

    class ClassB:
    // the Random class is not visible here
    val r = new Random   // this code will not compile`

- If you want to "static" import, or in other words, importing while refering to
  the member names directly, w/o prefix, use the following approach

    import java.lang.Math.*
    val a = sin(0)    // 0.0
    val b = cos(PI)   // -1.0

- Packages imported by default:

    - java.lang.*
    - scala.*
    - members of the Scala object Predef

`If you ever wondered why you can use classes like List, Vector, Map, etc., 
without importing them, they’re available because of definitions in the Predef object.`

- In the rare event there’s a naming conflict and you need to import something 
from the root of the project, prefix the package name with _root_:

    package accounts
    import _root_.accounts.*

------------------------------------
Importing given Instances
------------------------------------
- a special form of the import statement is used to import given instances

Example

    object A:
      class TC
      given tc: TC
      def f(using TC) = ???

    object B:
      import A.*            // import all non-given members
      import A.given        // import the given instance
      import A.{given, *}   // One line version.

Note
- import A.* clause of object B  -  imports all members of A except the given instance tc
- import A.given                 -  imports only that given instance

Discussion
- The `wildcard` selector brings all definitions other than `givens` or `extensions` into scope
- A `given` selector brings all givens—including those resulting from extensions—into scope.

Benefits
- Clear where givens in scope are coming from. In particular, not possible to hide
  imported givens in a long list of other wildcard imports.
- Enables importing all givens without importing anything else. Particularly important
  since givens can be anonymous, so the usual use of named imports is not practical

- Since givens can be anonymous, it’s not always practical to import them by their name, 
  and wildcard imports are typically used instead.

By-type imports
----------------------
provide a more specific alternative, which makes it more clear what is imported. 

This imports any given in A that has a type which conforms to TC

        `import A.{given TC}`

This imports givens of several types T1,...,Tn

        `import A.{given T1, ..., given Tn}`

--

object Instances:
    given intOrd:                   Ordering[Int]
    given listOrd[T: Ordering]:     Ordering[List[T]]
    given ec:                       ExecutionContext = ...
    given im:                       Monoid[Int]

Importing all given instances of a parameterized type is expressed by wildcard arguments
This statement imports the intOrd, listOrd, and ec instances, but leaves out the im instance 

        `import Instances.{given Ordering[?], given ExecutionContext}`

By-type imports can be mixed with by-name imports. If both are present in an import clause, 
by-type imports come last. For instance, this import clause imports im, intOrd, and listOrd, 
but leaves out ec:

        `import Instances.{im, given Ordering[?]}`
******************************************************************************
******************************************************************************/

// Concrete Example

object MonthConversions:
  trait MonthConverter[A]:
    def convert(a: A): String

  given intMonthConverter: MonthConverter[Int] with
    def convert(i: Int): String =
      i match
        case 1 =>  "January"
        case 2 =>  "February"
        // more cases here ...

  given stringMonthConverter: MonthConverter[String] with
    def convert(s: String): String =
      s match
        case "jan" => "January"
        case "feb" => "February"
        // more cases here ...

// To Import those givens into the current scope, use these two import statements
import MonthConversions.*
import MonthConversions.{given MonthConverter[?]}

// Now you can create a method that uses those given instances:
def genericMonthConverter[A](a: A)(using monthConverter: MonthConverter[A]): String = 
    monthConverter.convert(a)

def importGivenExample() = 
    println(genericMonthConverter(1))
    println(genericMonthConverter("jan"))

/* 
As mentioned, one of the key design benefits of the “import given” syntax is to make 
it clear where givens in scope come from, and it’s clear in these import statements 
that the givens come from the MonthConversions object.
 */
