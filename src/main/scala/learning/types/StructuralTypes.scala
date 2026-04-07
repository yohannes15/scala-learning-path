package learning.types

// Scala 2 has a weaker form of structural types based on Java reflection, 
// achieved with import scala.language.reflectiveCalls 

/**********************************************************************
**********************************************************************
                    Structural Types
-----------------------------------------------------------------------
- Structural types help in situations where you’d like to support simple
  dot notation in dynamic contexts without losing the advantages of static
  typing. They allow developers to use dot notation and configure how 
  fields and methods should be resolved.
- Some use cases, such as modeling db access, are more awkward in
  statically typed languages than in dynamically typed languages
- With dynamically typed languages, it is natural to model a row as a
  record or object and to select entries with simple dot notation
  e.g `row.columnName`
- Achieving the same experience would require defining a class for every
  possible row arising from db including joins and projections :( and 
  setting up a scheme to map b/n row and class representing it
- This requires a large amount of boilerplate, which leads developers 
  to trade the advantages of static typing for simpler schemes where
  column names are represented as strings and passed to other operators, 
  e.g. row.select("columnName"). This approach forgoes the advantages 
  of static typing, and is still not as natural as the dynamically typed
  version.
**********************************************************************
**********************************************************************/

/* 
Here is an example of structural type `Person`

The `Person` type adds a refinement to its parent type Record that defines
name and age fields. We say the refinement is `structural` since name 
and age are not defined in the parent type. But they exist nevertheless 
as members of class Person. For instance, structuralTypeExample shows how 
print "Emma is 42 years old." is done in both ways.

The parent type Record in this example is a generic class that can represent
arbitrary records in its elems argument. This argument is a sequence of 
pairs of labels of type String and values of type Any. When you create a 
Person as a Record you have to assert with a typecast that the record 
defines the right fields of the right types. 

Record itself is too weakly typed, so the compiler cannot know this without
help from the user. In practice, the connection between a structural type 
and its underlying generic representation would most likely be done by a 
database layer, and therefore would not be a concern of the end user.

Record extends the marker trait scala.Selectable and defines a method 
selectDynamic, which maps a field name to its value. Selecting a structural 
type member is done by calling this method. 

The person.name and person.age selections are translated by the Scala compiler to:
 */


/* 
Record is a generic container for any set of key-value pairs.

`elems: (String, Any)*` 
    — varargs of pairs, e.g. "name" -> "Emma", "age" -> 42
    - Asterisk symbol `*` is the repeated parameter syntax. 
    - It indicates that a method can accept >=0 arguments of 
      the preceding type.
`Selectable` 
    — a marker trait that tells the compiler this class supports 
      structural member access via selectDynamic.
*/
class Record(elems: (String, Any)*) extends Selectable:
    private val fields = elems.toMap
    // selectDynamic is the hook the compiler calls when you write `record.someField`.
    // It looks up the field by name in the map and returns the value as Any.
    def selectDynamic(name: String): Any = fields(name)

/* 
`Person` is a structural type — a type alias that refines `Record` with two
named fields. No new class is created; this is purely a compile-time constraint.

The refinement `{ val name: String; val age: Int }` tells the compiler:
"treat this Record as if it has a .name: String and .age: Int field"

This is what lets you write emmaPerson.name instead of emmaPerson.selectDynamic("name").
The compiler knows the expected return type from the refinement, so it can
insert the cast for you automatically.
*/
type Person = Record {
    val name: String
    val age: Int
}

def structuralTypeExample() =
    // Step 1: create a raw Record — no type safety on field names or types yet
    val emma = Record("name" -> "Emma", "age" -> 42)
    println(emma)

    // Step 2: access fields the untyped way
    println(s"${emma.selectDynamic("name")} is ${emma.selectDynamic("age")} years old.")

    // Step 3: cast to the structural type Person.
    // asInstanceOf[Person] doesn't change the runtime object at all —
    // it just tells the compiler "trust me, this Record has name and age".
    // From this point on, the compiler knows .name returns String and .age returns Int.
    val emmaPerson = emma.asInstanceOf[Person]

    // Step 4: dot notation now works — this is the "structural" access
    println(s"${emmaPerson.name} is ${emmaPerson.age} years old.")

    // What the compiler actually emits under the hood for emmaPerson.name and emmaPerson.age:
    //   emmaPerson.name  →  emmaPerson.selectDynamic("name").asInstanceOf[String]
    //   emmaPerson.age   →  emmaPerson.selectDynamic("age").asInstanceOf[Int]
    //
    // The cast to the concrete type (String, Int) is safe because the structural
    // type refinement declared those types — the programmer takes responsibility
    // for the asInstanceOf[Person] cast being correct.
    emmaPerson.selectDynamic("name").asInstanceOf[String]
    emmaPerson.selectDynamic("age").asInstanceOf[Int]



/* 
Second Example 

To reinforce the above, here's another strucutral type name `Book` that represents a book
that you might read from a database
*/

type Book = Record {
    val title: String
    val author: String
    val year: Int
    val rating: Double
}

def structuralTypeExample2() = 
    // Step 1: create a raw Record — no type safety on field names or types yet
    val bookRecord = Record(
        // a -> b is just prettier syntax for the same thing as (a, b)
        "title" -> "The Catcher in the Rype",
        "author" -> "J.D.Salinger",
        "year" -> 1951,
        "rating" -> 4.5
    )
    println(bookRecord)

    // Step 2: access fields the untyped way
    println(s"${bookRecord.selectDynamic("title")} has rating of ${bookRecord.selectDynamic("rating")}")

    // Step 3: cast to the structural type Book.
    // asInstanceOf[Book] doesn't change the runtime object at all —
    // it just tells the compiler "trust me, this Record has title, author, year and rating".
    // From this point on, the compiler the types of each
    val bookBook = bookRecord.asInstanceOf[Book]
    println(bookBook)

    // Step 4: dot notation now works — this is the "structural" access
    println(s"${bookBook.title} has rating of ${bookBook.rating}")

    // What the compiler actually emits under the hood for bookBook.title and bookBook.rating:
    //   bookBook.title    →  bookBook.selectDynamic("title").asInstanceOf[String]
    //   bookBook.rating   →  bookBook.selectDynamic("rating").asInstanceOf[Int]

    bookBook.selectDynamic("title").asInstanceOf[String]
    bookBook.selectDynamic("rating").asInstanceOf[Int]

/* 
Selectable class
------------------------------------------------------
Besides `selectDynamic`, a `Selectable` class sometimes also defines a method applyDynamic.
This can then be used to translate function calls of structural members. So, if `a` is an
instance of Selectable, a structural call like `a.f(b, c)` translates to:

    a.applyDynamic("f")(b, c)

The difference between the two:
  selectDynamic  — translates field/val access:    a.name     → a.selectDynamic("name")
  applyDynamic   — translates method call access:  a.f(b, c)  → a.applyDynamic("f")(b, c)
*/

// Example: a dynamic method dispatcher that looks up functions by name
class MethodRecord(elems: (String, Any => Any)*) extends Selectable:
    private val methods = elems.toMap
    def selectDynamic(name: String): Any        = methods(name)
    def applyDynamic(name: String)(arg: Any): Any = methods(name)(arg)

/* 
The structural type refinement { def double(x: Int): Int; def shout(x: String): String } 
tells the compiler the expected method signatures, so dot-call syntax works and the 
return types are known at compile time — even though the actual dispatch happens 
dynamically at runtime via applyDynamic
 */
type Transformer = MethodRecord {
    def double(x: Int): Int
    def shout(x: String): String
}

def applyDynamicExample() =
    val t = MethodRecord(
        "double" -> ((x: Any) => x.asInstanceOf[Int] * 2),
        "shout"  -> ((x: Any) => x.asInstanceOf[String].toUpperCase)
    ).asInstanceOf[Transformer]

    // t.double(5) → t.applyDynamic("double")(5) → methods("double")(5) → 10
    println(t.double(5))        // 10
    // t.shout("hello") → t.applyDynamic("shout")("hello") → "HELLO"
    println(t.shout("hello"))   // HELLO

/* 
TODO: 
    I skipped `Dependent Function Types` 
        - advanced and rare to come across except when desigining libraries or using advanced libraries
        - (https://docs.scala-lang.org/scala3/book/types-dependent-function.html)
*/

/*************************************************
************************************************
Summary
------------------
Scala has several other advanced types that are not shown in this book, including:

    Type lambdas
    Match types
    Existential types
    Higher-kinded types
    Singleton types
    Refinement types
    Kind polymorphism

For more details on most of these types, refer to the Scala 3 Reference documentation. 
(https://docs.scala-lang.org/scala3/reference/overview.html)

For singleton types see the literal types (https://scala-lang.org/files/archive/spec/3.4/03-types.html#literal-types) 
section of the Scala 3 spec, and for refinement types (https://scala-lang.org/files/archive/spec/3.4/03-types.html), 
see the refined types section.

**************************************************
*************************************************/
