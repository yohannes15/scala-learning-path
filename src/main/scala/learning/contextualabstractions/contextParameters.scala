package learning.contextualabstractions

/*******************************************************************
Scala offers two important features for contextual abstraction:
    - Context Parameters:
    - Given Instances (Scala 3) / Implicit Definitions (Scala 2):
        Terms that can be used by the Scala compiler to fill in the
        missing arguments

Context Parameters
------------------------------------------------
Allow you to specify parameters that, at the call site, can 
be omitted by the programmer and should be automatically 
inferred/provided by the context/compiler

When designing a system, often context info like `configuration` or
`settings` need to be provided to the different components of your
system. One common way to achieve this by passing the config as an
additional arg/s to your methods. This is tedious

Let us assume that the configuration does not change throughout most
of our code base. Passing config to each and every method call 
(like renderWidget) becomes very tedious and makes our program more 
difficult to read, since we need to ignore the config argument.
*/

case class Config(port: Int, baseUrl: String)

def tediousRenderWebsite(path: String, config: Config): String =
    "<html>" + tediousRenderWidget(List("cart"), config) + "</html>"

def tediousRenderWidget(items: List[String], config: Config): String = "tediousRenderWidget"

def tediousParametersExample() = 
    val tediousConfig = Config(8080, "docs.scala-lang.org")
    val res = tediousRenderWebsite("/home", tediousConfig)
    println(res)

/* 
Marking parameters as contextual
------------------------------------------
By starting a parameter section with the keyword `using` in Scala 3 
or implicit in Scala 2, we tell the compiler that at the call-site it 
should automatically find an argument with the correct type. 
The Scala compiler thus performs term inference.

In our call to renderWidget(List("cart")) the Scala compiler will see
that there is a term of type Config in scope (the config) and automatically
provide it to renderWidget. So the program is equivalent to the one above.

In fact, since we do not need to refer to `config` in our implementation 
of renderWebsite anymore, we can even omit its name in the signature in Scala 3.
In Scala 2, the name of implicit parameters is still mandatory.

We have seen how to abstract over contextual parameters and that the Scala
compiler can provide arguments automatically for us. But how can we specify
which configuration to use for our call to renderWebsite? We can explicity provide
contextual arguments -> renderWebsite("/home")(using config)

Explicitly providing contextual parameters can be useful if we have multiple
different values in scope that would make sense, and we want to make sure 
that the correct one is passed to the function.

For all other cases, as we will see in the next section, there is another way
to bring contextual values into scope.
*/

// no need to do using config: Config
def renderWebsite(path: String)(using Config): String =
    "<html>" + renderWidget(List("cart")) + "</html>"

def renderWidget(items: List[String])(using Config): String = "renderWidget"

def contextualParametersExample() = 
    val config = Config(8080, "docs.scala-lang.org")
    // explicitly provide contextual arguments with using
    val res = renderWebsite("/home")(using config)
    println(res)


/* 
Given Instances (Implict Definitions in Scala 2)
----------------------------------------------------------

We have seen that we can explicitly pass args as contextual parameters. However,
if there is a single canonical value for a partical type, there is another preferred
way to make it available to the scala compiler, by marking it as `given` in Scala 3
or `implicit` in Scala 2.

In the below example we specify that whenever a contextual parameter of type Config 
is omitted in the current scope, the compiler should infer config as an argument.
*/

// scala 2
// implicit val config: Config = Config(8081, "docs.scala2lang.org")

// scala 3
val config = Config(8080, "docs.scala-lang.org")

// this is the type that we want to provide the canonical value for
// this is the value the Scala compiler will infer
// as argument to contextual parameters of type Config
given Config = config            

def givenInstancesExample() = 
    val res = renderWebsite("/home") // no need to provide explicity now its "given"
    println(res)



