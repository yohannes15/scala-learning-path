package learning

import scala.util.CommandLineParser

/*
Adding a @main annotation to a method turns it into entry point of an executable program:

@main def entryPoint() = println("Hello, World")

// scala run file.scala
// Hello, World

A @main annotated method can be written either at the top-level (as shown),
or inside a statically accessible object. In either case, the name of the program is
in each case the name of the method, without any object prefixes.

- @main method can handle command line arguments, and those arguments can have different types
    `scala run mainMethods.scala -- 23 Lisa Peter`

- @main method can have an arbitrary number of parameters. 

- @main method’s parameter list can end in a repeated parameter like String* that takes 
    all remaining arguments given on the command line.

- @main method's parameter types (Int, String ...) -> there must be a given instance of the 
    scala.util.CommandLineParser.FromString type class that converts an argument 
    String to the required parameter type.

- @main methods are the recommended way to generate programs that can be invoked from the command line in Scala 3

The program implemented from @main method checks that there are enough arguments on the command line
to fill in all parameters, and that the argument strings can be converted to the required types. 
If a check fails, the program is terminated with an error message:

    $ scala run happyBirthday.scala -- 22
    Illegal command line after first argument: more arguments expected

    $ scala run happyBirthday.scala -- sixty Fred
    Illegal command line: java.lang.NumberFormatException: For input string: "sixty"
 */


@main
def happyBirthday(age: Int, name: String, others: String*) =
  val suffix = (age % 100) match
    case 11 | 12 | 13 => "th"
    case _            =>
      (age % 10) match
        case 1 => "st"
        case 2 => "nd"
        case 3 => "rd"
        case _ => "th"

  val sb = StringBuilder(s"Happy $age$suffix birthday, $name")
  for other <- others do sb.append(" and ").append(other)
  println(sb.toString)

def testMainMethods() =
  happyBirthday(23, "List", "Peter")


/************************************************************
 USER DEFINED TYPES AS PARAMETERS
 --------------------------------
 compiler looks for a given instance of the 
 scala.util.CommandLineParser.FromString 
 typeclass for the type of the argument.
 ************************************************************/

given CommandLineParser.FromString[Color] with
    def fromString(s: String): Color = 
        // matches the string to an enum constant by name
        Color.valueOf(s)

@main def run(color: Color): Unit =
  println(s"The color is ${color.toString}")

/* 

The Scala compiler generates a program from an `@main` method `f` as follows:

    - It creates a `class` named f in the package where the @main method was found.
    - The class has a static method main with the usual signature of a Java main method: 
        it takes an Array[String] as argument and returns Unit.
    - The generated main method calls method f with arguments converted using
      `scala.util.CommandLineParser` helpers, which use `FromString` type class instances
      for each parameter type.

For instance, the happyBirthday method above generates additional code *roughly* like the following
(illustrative only — the real bytecode uses a JVM `static` entry point and compiler-internal names):

    final class happyBirthday {
      import scala.util.{CommandLineParser as CLP}
      <static> def main(args: Array[String]): Unit =
        try
          happyBirthday(
              CLP.parseArgument[Int](args, 0),
              CLP.parseArgument[String](args, 1),
              CLP.parseRemainingArguments[String](args, 2)*
            )
        catch {
          case error: CLP.ParseError => CLP.showError(error)
        }
    }

    The `main` body parses each CLI segment using `FromString` instances (see
    `scala.util.CommandLineParser`), not a single "FromString object".
*/


/* 
NOTES

If programs need to cross-build between Scala 2 and Scala 3, it’s recommended to use an 
object with an explicit main method and a single Array[String] argument instead:


object happyBirthday {
  private def happyBirthday(age: Int, name: String, others: String*) = {
    ... // same as before
}

def main(args: Array[String]): Unit =
    happyBirthday(args(0).toInt, args(1), args.drop(2).toIndexedSeq:_*)
}

`scala run happyBirthday.scala -- 23 Lisa Peter`
// Happy 23rd Birthday, Lisa and Peter!
 */

