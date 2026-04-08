package learning.domain

/*******************************************************************
 Advanced example: service-oriented design with a *family of types*

 Intended to show how abstract type members, nested traits, and self-types
 work together so one component (`SubjectObserver`) can be implemented more
 than once (here: `SensorReader`) with different concrete subject/observer types.
******************************************************************/

/*
 We define a reusable *component* SubjectObserver as a trait with abstract type members `S` and
 `O`. Implementations (like `SensorReader`) *fix* those types to concrete
 classes (`Sensor`, `Display`). That is the “family of types”: the abstract
 trait names the roles; each object extending `SubjectObserver` supplies its own
 pair of types that work together.
 */

trait SubjectObserver:

    /*
     Abstract type members (with upper bounds)

     `type S <: Subject` means: “there is some type `S`—chosen later—that must be
     a subtype of the nested trait `Subject`.” Same for `O` and `Observer`.

     So `S` is not “any `Subject`”; it is the *particular* subject type this
     implementation will use. Method signatures use `S` and `O` so observers
     receive the *refined* subject type, not the generic `Subject` API only.

     Implementations set concrete aliases, e.g. `type S = Sensor`, `type O = Display`.
     */
    type S <: Subject
    type O <: Observer

    /*
     Nested traits `Subject` and `Observer`

     They are declared *inside* `SubjectObserver` so they can refer to `S` and
     `O`.

     `Observer` declares `notify(sub: S)`—the argument is `S`, not `Subject`—so
     each call site passes a subject value typed as the implementation’s own
     subject type (here `Sensor`), which may expose more than the generic trait.

     `Subject` holds `List[O]` and subscribes with `obs: O` for the same reason:
     observers are the implementation’s observer type, not only `Observer`.
     */
    trait Subject:

        /* 
        Self-type Annotations `self: T =>`

         Any concrete class mixing in `Subject` must also be a subtype of `S`.
         Then `this` inside `Subject` is treated as `S`, so `obs.notify(this)` in
         `publish()` type-checks: `notify` expects an `S`, and `this` is an `S`.

         (If `S` were a fixed class, you could sometimes encode this with
         `trait Subject extends S` instead; with abstract `S`, the self-type is the
         usual pattern.)
         */
        self: S =>
            private var observers: List[O] = List()
            def subscribe(obs: O): Unit =
                observers = obs :: observers
            def publish() =
                for obs <- observers do obs.notify(this)

    trait Observer:
        def notify(sub: S): Unit

/*
 Implementing the component: `SensorReader`

 `object SensorReader extends SubjectObserver` fills in the abstract types and
 supplies nested classes. `Sensor` and `Display` are *path-dependent*: the
 classes live on `SensorReader`, so elsewhere you refer to them as
 `SensorReader.Sensor` unless you `import SensorReader.*`.

 `Display()` builds a `Display` with the default constructor—nested classes
 behave like ordinary classes for instantiation once you are in scope.
 */
object SensorReader extends SubjectObserver:
    type S = Sensor
    type O = Display

    class Sensor(val label: String) extends Subject:
        // its own private state
        private var currentValue = 0.0
        def value = currentValue
        // encapsulates modification of the state
        def changeValue(v: Double) = 
            currentValue = v
            publish() // from `Subject`; notifies all subscribed `O`s

    class Display extends Observer:
        /*
         `notify` may use members of `Sensor` (e.g. `label`, `value`) because the
         parameter is conceptually `S`, and here `S = Sensor`.
         */
        def notify(sub: Sensor) =
            println(s"${sub.label} has value ${sub.value}")


def sensorReaderExample() =
    import SensorReader.*

    // Two subjects, two displays; d1 observes both subjects, d2 only s1.
    val s1 = Sensor("sensor1")
    val s2 = Sensor("sensor2")
    val d1 = Display()
    val d2 = Display()
    s1.subscribe(d1)
    s1.subscribe(d2)
    s2.subscribe(d1)

    // Each `changeValue` calls `publish()` → each subscribed display’s `notify`.
    s1.changeValue(2)
    s2.changeValue(3)

    // Prints (order follows `observers` list iteration; both displays see s1=2):
    // sensor1 has value 2.0
    // sensor1 has value 2.0
    // sensor2 has value 3.0
