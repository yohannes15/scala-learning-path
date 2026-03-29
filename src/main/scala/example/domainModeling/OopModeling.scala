package example

import scala.compiletime.ops.string

// introduction to domain modeling using object-oriented programming (OOP) in Scala 3.

/* 
Scala provides all the necessary tools for object-oriented design:
------
`Traits`
    - let you specify (abstract) interfaces, as well as concrete implementations.
    - the primary tool of decomposition in Scala
    - a trait name is a *type* (use it in annotations, parameters, bounds, etc.);
      values are instances of concrete subtypes (`class` / `object` / anonymous)
      that extend or mix in the trait.
    - great to modularize components and describe interfaces (required and provided).
`Mixin Composition` gives you the tools to compose components from smaller parts.
`Classes` can implement the interfaces specified by traits.
`Instances` of classes can have their own private state.
`Subtyping` lets you use an instance of one class where an instance of a superclass is expected.
`Access modifiers` lets you control which members of a class can be accessed by which part of the code.
 */

/* 
 Ways to get a value whose type is a trait (a trait itself is not “constructed”
 with `new` until every abstract member has an implementation):

 - Named class — `class C(...) extends T` then `C(...)`. Use when you want
   constructor parameters, a stable name, or reuse.
 - Singleton object — `object name extends T` then use `name` as the value (one
   instance, lazy init).
 - Anonymous class — `new T { ... }` or Scala 3 `new T:` with an indented body
   implementing abstract members. Use for one-off implementations.
 - Empty anonymous subclass — `new T {}` only when `T` is already fully concrete
   (no abstract members left), e.g. a composed trait like `ComposedService`.

 This file shows: `Document` (named class), and `new ComposedService {}` (empty
 refinement on a concrete trait).
 */

/*******************************************************************
 Traits
******************************************************************/

trait Showable:
    def show: String // abstract
    /* 
    show has no body here—it’s abstract. Each concrete type that extends Showable 
    must supply what show returns (e.g. a plain string).
     */
    def showHtml: String = "<p>" + show + "</p>" // concrete implementations
    /* 
    showHtml does have a body. That body uses show: it concatenates "<p>", 
    whatever show returns, and "</p>".
     */

/* 
 Odersky and Zenger describe `traits` as components in composition. They use a
 "service-oriented" vocabulary (not HTTP or microservices):

 - Abstract members are *required services*: the trait does not supply them;
   a subclass (or mixin) must implement them so the component is complete.
 - Concrete members are *provided services*: the trait already supplies them;
   subclasses inherit them and need not reimplement unless they override.

 Here, `show` is required (subclass must define how to produce the plain string).
 `showHtml` is provided (subclasses get HTML wrapping for free). The provided
 service depends on the required one: `showHtml` is defined in terms of `show`,
 so the trait fixes *how* HTML is built (<p>...</p>) while each implementation
 fixes *what* text `show` returns.
 */

class Document(text: String) extends Showable:
    def show = text

/* 
 Abstract methods are not the only thing that can be left abstract in a trait.
    - abstract methods              =   def m(): T
    - abstract value                =   val x: T
    - abstract type                 =   type T
    - abstract type with bounds     =   type T <: S
    - abstract givens               =   given t: T          // scala only

Each of the above features can be used to specify some form of requirement on
the implementor of the trait.
 */

trait Greeting:
  def sayHello(name: String): Unit

// Instantiating with implementation / Anonymous Class Instantiation 
val greeter = new Greeting:
  def sayHello(name: String): Unit = println(s"Hello, $name!")


/*******************************************************************
 Mixin Composition
******************************************************************/

/* 
 Mixin composition: merge several traits into one type with multiple parents
 (e.g. `extends GreetingService, TranslationService`). Scala combines inherited
 members via linearization (trait order matters if members conflict).

 Here, `GreetingService` requires `translate` (abstract) and provides `sayHello`,
 which calls `translate("Hello")`. `TranslationService` provides a concrete
 `translate` with the same signature. When both traits are mixed, that counts as
 one logical `translate`: the abstract requirement is satisfied by the other
 trait’s implementation—no hand-written “bridge” forwarding.

 After composition, `ComposedService` has `sayHello` from `GreetingService` and
 `translate` resolved to `TranslationService`’s body (here a stub `"..."` for every
 string—illustrates wiring, not a real translator).

 Contrast `Document extends Showable`: one trait + class implementing an abstract
 member. Here, two traits compose: implementation comes from one mixin, the hook
 that uses it from another.

 If two mixins both supplied conflicting concrete `translate` definitions, you
 would need explicit overrides or a careful trait order.

 This matching works for methods and for other abstract members (types, vals,
 givens, etc.) when signatures are compatible.
 */

trait GreetingService:
    def translate(text: String): String
    def sayHello = translate("Hello")

trait TranslationService:
    def translate(text: String): String = "..."

// To compose the two services, we can simply create a new trait extending them
trait ComposedService extends GreetingService, TranslationService

def mixinCompositionExample(): Unit =
    val svc = new ComposedService {}
    println(s"sayHello -> ${svc.sayHello}, translate('Hi') -> ${svc.translate("Hi")}")

    greeter.sayHello("World")

/*******************************************************************
 Classes
******************************************************************/

/* 
When designing software in Scala, it’s often helpful to only consider using classes 
at the leafs of your inheritance model:

    Traits 	            T1, T2, T3
    Composed traits 	S1 extends T1, T2, 
                        S2 extends T2, T3
    Classes 	        C extends S1, T3
    Instances 	        C()

classes can extend multiple traits (but only one super class):
 */

class MyService(name: String) extends ComposedService, Showable:
  def show = s"$name says $sayHello"

// As mentioned before, it is possible to extend another class:
// However, since traits are designed as the primary means of decomposition, 
// it is not recommended to extend a class that is defined in one file from another file.

class Worker(name: String)
class SoftwareDeveloper(name: String, favoriteLang: String) extends Worker(name)

/* 
In Scala 3 extending non-abstract classes in other files is restricted. 

Marking classes with `open` is a new feature of Scala 3. Having to explicitly mark classes as `open`
avoids many common pitfalls in OO design. In particular, it requires library designers to explicitly
plan for extension and for instance document the classes that are marked as open with additional
extension contracts.
 */

open class OpenWorker(name: String)

def classExample() =
    // instance
    val s1: MyService = MyService("Service 1")
    /* 
     Subtyping: 
    - `MyService` extends `ComposedService` (which extends `GreetingService` and `TranslationService`) and 
      `Showable`, so value (`s1`) is a `GreetingService`, a `TranslationService`, and a `Showable`. 
       The same object can be used where any supertype is expected.
       Assignments below are safe upcasts: same object in memory; each `val` only exposes members of its declared type 
       (e.g. `s4` has `show` / `showHtml`, not `translate`; use `s1` or a narrower type to call those).
     */
    val s2: GreetingService = s1
    // above statement does not copy the object. It only says: “treat this value as a GreetingService.” 
    // The compiler checks that MyService is a subtype of GreetingService (it is), so the assignment is allowed.
    val s3: TranslationService = s1
    val s4: Showable = s1

/*******************************************************************
 Instances and Private Mutable State
******************************************************************/

/* 
 Every instance of the class Counter has its own private state that can only be observed through the method count, 
 as the following interaction illustrates:

    val c1 = Counter()
    c1.count // 0
    c1.tick()
    c1.tick()
    c1.count // 2

By default, all member definitions in Scala are publicly visible. To hide implementation details, it’s possible to
define members (methods, fields, types, etc.) to be `private` or `protected`. This way you can control how they are
accessed or overridden. 
- Private members are only visible to the `class/trait` itself and to its `companion` object. 
- Protected members are also visible to subclasses of the class.
*/

class Counter:
    // can only be observed by the method `count`
    private var currentCount = 0

    def tick(): Unit = currentCount += 1
    def count: Int = currentCount



