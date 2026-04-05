package learning

// All values have a type, including numerical values and functions. 
// See `src/main/scala/learning/static/type-hierarchy.svg` (also in docs/diagrams.md).

/* 
`Any`:
    - supertype of all types, also called the `top type`. 
    - It defines certain universal methods such as `equals`, `hashCode` and `toString`

`Matchable`:
    - Any has a subtype Matchable
    - marks all types that we can perform pattern matching on.
    - Important to guarantee a property call "parametricity"
    - CAN'T match on values of type `Any`, but only on values that are a subtype of `Matchable`
    - Has two important subtypes: `AnyVal` & `AnyRef`

`AnyVal`:
    - represents value types. They are a couple predefined value types and they are non-nullable
    - Double, Float, Long, Int, Short, Byte, Char, Unit and Boolean. Unit is a value type which 
      carries no meaningful info. There is exactly one instance of Unit which can refer as: `()`
    - Numeric types extend AnyVal
    - Char is sub class of AnyVal (a value type)

`AnyRef`:
    - represents reference types (all non value types)
    - Every user-defined type in scala is a subtype of AnyRef (in a JVE env, AnyRef -> java.lang.Object)
    - String is sub class of AnyRef (a ref type)

`Nothing`:
    - is a subtype of all types (bottom type). No value has the type Nothing
    - Common use is to signal non-termination, (thrown exception, program exit, infinite loop)
    - is type of an expression which does not evaluate to a value, or a method that does not return normally.

`Null`:
    - a subtype of all reference types (AnyRef)
    - has single value identified by the keyword `null`
    - useage of keyword null is considered bad practice and shouldn't be used. Only used for interoperability 
      with other JVM languages
    
Type Casting
-> Value types can be cast in the following way: `src/main/scala/learning/static/type-casting-diagram.svg`.
   Byte -> Short -> Int -> Long -> Float -> Double.
                     ^
                    |||
                    Char
-> You can only cast to a type if there is no loss of information, otherwise explict cast needed
 */

def instancesOfAnyExample(): Unit =
    val list: List[Any] = List(
        "a string", // string
        732, // integer
        'c', // character
        true, // boolean
        () => "an anonymous function returning a string"
    )

    list.foreach(element => println(element))

def typeCastExample(): Unit = 
    val b: Byte = 127
    val i: Int  = b // cast to 127

    val face: Char = '☺'
    val number: Int = face  // 9786

    // Explict cast example and error

    // val x: Long = 987654321
    // val y: Float = x.toFloat  // 9.8765434E8 (note that `.toFloat` is required because the cast results in precision loss)
    // val z: Long = y // error
