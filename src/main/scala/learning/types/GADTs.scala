package learning.types

/*******************************************************************
*******************************************************************
Generalized Algebraic Datatypes (GADTs)
-------------------------------------------------------------------
A regular ADT (like Option[T]) has a type parameter T that is left
open — the caller decides what T is, and all cases share that same T.

A GADT goes further: each case can FIX T to a specific concrete type
via an explicit `extends` clause. The compiler tracks which T belongs
to which case and uses that knowledge inside pattern match branches.

This is called "type refinement": when you match on a GADT case, the
compiler narrows the type parameter to what that case declared.

Plain ADT vs GADT:

    // ADT — type params A and B are chosen by the caller, not pinned per-case
    enum Pair[A, B]:
        case Both(a: A, b: B)   // A and B remain whatever the caller passes in

    // GADT — each case pins T independently
    enum Expr[T]:
        case Num(n: Int)      extends Expr[Int]
        case Bool(b: Boolean) extends Expr[Boolean]
        case Add(l: Expr[Int], r: Expr[Int]) extends Expr[Int]
        case IfThenElse(cond: Expr[Boolean],
                        thenB: Expr[T],
                        elseB: Expr[T]) extends Expr[T]

Plain ADT — T stays the same throughout.
GADT     — each case can narrow T to something more specific.

Because T is pinned per-case, the compiler can type-check functions
over GADTs WITHOUT any runtime casts. Each match arm refines T and
the return type is verified statically.
*******************************************************************
*******************************************************************/

// --- Example 1: Box ---
// A simple GADT where T specifies what is stored inside the box.
// Each case fixes T to a concrete type via the extends clause.
enum Box[T](contents: T):
    case IntBox(n: Int)       extends Box[Int](n)
    case BoolBox(b: Boolean)  extends Box[Boolean](b)
    case StringBox(s: String) extends Box[String](s)

// The GADT refines T in each branch so the compiler verifies the
// return type is correct without any casting.
def extract[T](b: Box[T]): T =
    import Box.*
    b match
        case IntBox(n)    => n + 1                                    // T = Int
        case BoolBox(b)   => !b                                       // T = Boolean
        case StringBox(s) => StringBuilder(s"Happy birthday, $s").toString  // T = String

// --- Example 2: Expr — a typed expression language ---
// Each case pins T to the type it produces, so the compiler rejects
// malformed expressions (e.g. Add(Bool(true), Num(1))) at compile time.
enum Expr[T]:
    case Num(n: Int)                         extends Expr[Int]
    case Bool(b: Boolean)                    extends Expr[Boolean]
    // Add requires both operands to be Expr[Int] and always produces Expr[Int]
    case Add(l: Expr[Int], r: Expr[Int])     extends Expr[Int]
    // IfThenElse: condition must be Boolean; both branches must agree on T
    case IfThenElse(
        cond:  Expr[Boolean],
        thenB: Expr[T],
        elseB: Expr[T]
    ) extends Expr[T]

// evaluate reduces an Expr[T] to a plain Scala value of type T.
// No casting needed: T is refined per arm and verified by the compiler.
def evaluate[T](expr: Expr[T]): T =
    import Expr.*
    expr match
        case Num(n)               => n
        case Bool(b)              => b
        case Add(l, r)            => evaluate(l) + evaluate(r)
        case IfThenElse(c, t, e)  => if evaluate(c) then evaluate(t) else evaluate(e)

def gadtExample() =
    // Box examples
    println(extract(Box.IntBox(42)))             // 43
    println(extract(Box.BoolBox(true)))          // false
    println(extract(Box.StringBox("Mike")))      // Happy birthday, Mike

    import Expr.*

    // 1 + 2 → 3
    val sum = Add(Num(1), Num(2))
    println(evaluate(sum))                       // 3

    // if true then 10 else 20 → 10
    val branch = IfThenElse(Bool(true), Num(10), Num(20))
    println(evaluate(branch))                    // 10

    // condition must be Expr[Boolean] — Expr[Int] would not compile
    val boolBranch = IfThenElse(Bool(false), Bool(true), Bool(false))
    println(evaluate(boolBranch))                // false

    // Add(Bool(true), Num(1))  // ← does not compile: Bool is Expr[Boolean], not Expr[Int]
