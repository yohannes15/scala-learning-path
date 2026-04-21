package learning.typeclasses

import cats.Applicative


/** Just a binary tree container with **`traverse`**
 * 
 * Same pattern as [[traverse]] on `List`, but the shape is recursive 
 * (left / right subtrees) instead of a list spine — so each step uses
 * **`map3`** (value, left, right). 
 * 
 * [[LearningTraits.Traverse]] in [[Traverse.scala]] abstracts over both
 * `List` and shapes like this.
 * 
 * *Cases:* `Empty` has no `A` to map — stay `Empty` inside `pure`. `Branch`
 * runs `f` on the value and **recursively** `traverse`s both subtrees, 
 * then rebuilds `Branch`.
*/
object tree {
  sealed abstract class Tree2[A] extends Product with Serializable {

    /** Walk the tree in applicative `F`: apply `f` at every `A`, combining all
      * `F[B]`s into one `F[Tree[B]]` while preserving shape (`Empty` vs `Branch`).
      */
    /** The **implementation** lives here (`map3`, recursion). The [[LearningTraits.Traverse]]
      * instance for `Tree` only forwards to this method so generic code can use one API.
      */
    def traverse[F[_]: Applicative, B](f: A => F[B]): F[Tree2[B]] = this match {
      case Tree2.Empty() =>
        Applicative[F].pure(Tree2.Empty()) // nothing to map; structure stays empty

      case Tree2.Branch(v, l, r) =>
        // Three independent effectful results: value, left subtree, right subtree
        Applicative[F].map3(f(v), l.traverse(f), r.traverse(f))(Tree2.Branch(_, _, _))
    }
  }

  object Tree2 {
    final case class Empty[A]() extends Tree2[A]
    final case class Branch[A](value: A, left: Tree2[A], right: Tree2[A]) extends Tree2[A]
  }

  /** Same shape and `traverse` as [[Tree]], using a Scala 3 `enum` (`Empty` is a singleton case).
    *
    * `+A` because `case Empty` has no type parameter: it is effectively `TreeEnum[Nothing]`.
    * Covariance gives `TreeEnum[Nothing] <: TreeEnum[B]`, so `Applicative[F].pure(Empty)` in
    * `traverse` is a `F[TreeEnum[B]]`. [[Tree]] uses `Empty[A]()` instead, so `A` is fixed at
    * each site and the class stays invariant.
    */
  enum Tree[+A]:
    case Empty
    case Branch(value: A, left: Tree[A], right: Tree[A])

    def traverse[F[_]: Applicative, B](f: A => F[B]): F[Tree[B]] = this match {
      case Empty =>
        Applicative[F].pure(Empty)

      case Branch(v, l, r) =>
        Applicative[F].map3(f(v), l.traverse(f), r.traverse(f))(Branch(_, _, _))
    }
}
