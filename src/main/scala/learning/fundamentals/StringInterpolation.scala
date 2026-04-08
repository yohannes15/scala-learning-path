package learning.fundamentals

// The s interpolator

def sInterpolator(): Unit = 
    val name = "James"
    val age = 30
    println(s"$name is $age years old")   // "James is 30 years old"

    println(s"2 + 2 = ${2 + 2}")   // "2 + 2 = 4"
    val x = -1
    println(s"x.abs = ${x.abs}")   // "x.abs = 1"

    // special characters
    println(s"New offers starting at $$14.99")   // "New offers starting at $14.99"

    // double quotes
    println(s"""{"name":"James"}""")     // `{"name":"James"}`

    // Multiline strings
    println(s"""name: "$name",
           |age: $age""".stripMargin)

// The f interpolator 
// all variable references should be followed by a printf-style format string, like %d.
// is typesafe and compiler will raise an issue if format can't be applied
// The f interpolator makes use of the string format utilities available from Java.
// The formats allowed after the % character are outlined in the Formatter javadoc.
def fInterpolator(): Unit =
    val height = 1.9d
    val name = "James"
    println(f"$name%s is $height%2.2f meters tall")  // "James is 1.90 meters tall"

    // Typesafety compiler error example when trying to format double example
    // val height: Double = 1.9d

    // scala> f"$height%4d"
    // -- Error: ----------------------------------------------------------------------
    // 1 |f"$height%4d"
    // |   ^^^^^^
    // |   Found: (height : Double), Required: Int, Long, Byte, Short, BigInt
    // 1 error found
    

    // special characters
    println(f"3/19 is less than 20%%")  // "3/19 is less than 20%"

// The raw interpolator
// Similar to the s interpolator except that it performs no escaping of literals within the string

def rawInterpolator(): Unit =
    val s = s"a\nb"
    println(s) // a newline b

    val foo = 42
    val r = raw"a\n$foo"
    println(r) // a\n42

/* 
Users can define their own interpolators (Custom Interpolators)
- All processed string literals are simple code transformations. Anytime the compiler encounters a processed
  string literal of the form: `id"string content"`
- It transforms it into a method call `id(...)` on an instance of `StringContext`
- To define our own string interpolation, we need to create an `implicit` class (Scala2) or an `extension`
  method (Scala3) that adds a new method to StringContext
 */

case class Point(x: Double, y: Double)

//// StringContext Extension /////////
// sc.parts => literal fragments (text b/n the interpolated expressions)
// args => evaluated expression values passed into the interpolator
//  parts.length == args.length + 1 

def customInterpolatorExample(): Unit =
    val pt = p"1,-2" 
    println(pt) // Point(1.0, -2.0)

    val x = 12.0
    val pt2 = p"${x/5}, $x"
    println(pt2) // Point(2.4, 12.0)
    
    val name = "Bob"
    val interpolatedName = dbg"Hi $name"
    

/////////////////////////////////////////
//////////// Pattern Matching ///////////
/////////////////////////////////////////

// It is also possible to use string interpolation in patterns, 
// for both built-in and user-defined interpolators:

/*
def patternMatchingExample(): Unit = 
    "Test String".match
        // built in interpolator
        case s"Hello $name" => // Executes for Strings which start with "Hello, " and end in "!"

        // hypothetical custom interpolator:
        case p"$a, 0" => // Executes for example for Points whose second coordinate is 0

        // no extractors by default for the f and raw interpolators so neither case f"..." nor 
        // case raw"..." will work.
*/

/* 
Custom Extractors are needed for pattern matching that is custom
To define our own string interpolation, we need to create an implicit class (Scala 2) or a Conversion instance (Scala 3) that adds an extractor member to StringContext.

As an example, let’s assume we have a Point class and want to create a custom pattern p"$a,$b" that extracts the coordinates of a Point object.
 */

extension (sc: StringContext)
    // Simple custom interpolator `p"X,Y"` that parses two comma-separated numbers
    // and returns a Point. It joins the StringContext parts (there are no args
    // for a plain literal), splits on comma, trims and converts to Double.
    def p(args: Double*): Point =
        val raw = sc.s(args*)
        val tokens = raw.split(",", 2).map(_.trim)
        if tokens.length < 2 then
            throw IllegalArgumentException(s"Expected two numbers in: '$raw'")
        val pts = tokens.map(_.toDoubleOption.getOrElse(0.0))
        Point(pts(0), pts(1))

    def dbg(args: Any*): Unit =
        println(sc.parts)   // List("Hi ", "!")
        println(args.toList) // List("Bob")

    def p = PointExtractor(sc)

class PointExtractor(sc: StringContext):
    // Extractor for pattern matching with `p"$x,$y"`.
    //
    // How it works:
    // 1) A pattern like `p"$x,$y"` is compiled into a StringContext whose
    //    `parts` are the literal fragments between placeholders. For `p"$x,$y"`
    //    that becomes Seq("", ",", "") (empty before first, "," between, empty after).
    // 2) The compiler calls the parameterless `p` (defined earlier) to obtain
    //    an extractor object: `PointExtractor(sc)`.
    // 3) It then invokes `unapply` on the extractor with the value being matched.
    // 4) `unapply` should return `Some(...)` to bind the pattern variables, or
    //    `None` to indicate the pattern does not match. Throwing an exception
    //    is discouraged because it aborts matching; returning `None` cleanly fails.
    def unapply(point: Point): Option[(Double, Double)] = 
        sc.parts match
            // checks if the pattern is p"$a,$b" or p"$a, $b"
            case Seq("", "," | ", ", "") =>
                Some((point.x, point.y))
            case _ =>
                // Pattern not recognized -> pattern match fails (return None)
                None

def patternMatchingCustomExtractorExample(): Unit =
    val pt: Point = Point(2,3)
    pt match
        case p"$x,$y" => println(x + y) // x = 2, y = 3
    

    
