package learning.types

/************************************************************
*************************************************************
Union Types (Scala 3 only)

The | operator creates a so-called union type. The type A | B
represents values that are either of the type A or of the type B.

In the following example, the help method accepts a parameter
named id of the union type `Username | Email`, that can be
either a Username or an Email.

As shown, union types can be used to represent alternatives
of several different types, without requiring those types to
be part of a custom-crafted class hierarchy, or requiring
explicit wrapping.

Union is also commutative: A | B is the same type as B | A.
************************************************************
************************************************************/

case class Username(name: String)
case class Email(email: String)

def help(id: Username | Email): Unit =
    id match
        case Username(name)  => println(s"You have a name $name")
        case Email(email)    => println(s"You have an email $email")
        // case 1.0 => ???   // ERROR: does not match Username | Email

def unionTypeExample() =
    val email    = Email("test@email.com")
    val username = Username("YB")
    help(email)
    help(username)
    // help("hi")   // error: Found: ("hi" : String) Required: Username | Email

/*
Without union types, you would need to pre-plan a class hierarchy:

    trait UsernameOrEmail
    case class Username(name: String) extends UsernameOrEmail
    case class Email(name: String) extends UsernameOrEmail
    def help(id: UsernameOrEmail) = ...

Pre-planning does not scale very well since, for example, requirements
of API users might not be foreseeable. Additionally, cluttering the
type hierarchy with marker traits like UsernameOrEmail makes
the code more difficult to read.

Inference of Union Types
--------------------------------
The compiler assigns a union type to an expression only if such a type
is explicitly given. For instance given these values:
 */

val name  = Username("Eve")
val email2 = Email("eve@email.com")

// ac is inferred as Object (common supertype), NOT Username | Email
val ac = if true then name else email2

// To get the union type you must annotate it explicitly
val bc: Email | Username = if true then name else email2
