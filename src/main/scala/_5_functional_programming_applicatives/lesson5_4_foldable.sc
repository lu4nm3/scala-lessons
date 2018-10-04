// ****************************************************************
//                             Foldable
// ****************************************************************
import cats.{Eval, Foldable}
import cats.implicits._
import cats.kernel.Monoid




// The `Foldable` type class captures the `foldLeft` and
// `foldRight` methods we're used to in `Seq`s, `List`s, `Vector`s,
// and `Stream`s. With `Foldable` we can write generic folds that
// work with a variety of sequence types.




// `foldLeft` works recursively down the list starting on the left
// side. Our binary function is called for each item and the result
// becomes the accumulator value for the next call:

List(1, 2, 3, 4, 5).foldLeft(0)((accum, num) => accum + (num + 1))
//res0: Int = 20




// `foldLeft` traverses from left to right
// `foldRight` traverses from right to left

// They are both equivalent if our binary operation is commutative:

List(1, 2, 3).foldLeft(0)(_ + _)
// 0 + 1 = 1
// 1 + 2 = 3
// 3 + 3 = 6
//res1: Int = 6

List(1, 2, 3).foldRight(0)(_ + _)
// 3 + 0 = 3
// 2 + 3 = 5
// 1 + 5 = 6
//res2: Int = 6




// If we provided a non-commutative operation, then the order of
// evaluation makes a difference:

List(1, 2, 3).foldLeft(0)(_ - _)
//  0 - 1 = -1
// -1 - 2 = -3
// -3 - 3 = -6
//res1: Int = -6

List(1, 2, 3).foldRight(0)(_ - _)
// 3 - 0 = 3
// 2 - 3 = -1
// 1 - (-1) = 2
//res2: Int = 2




// Cats' `Foldable` abstracts `foldLeft` and `foldRight` into a
// type class. It provides out-of-the-box instances for a handful
// of Scala data types (`List`, `Vector`, `Stream`, `Option`):

Foldable[List].foldLeft(List(1, 2, 3), 0)(_ - _)
//res3: Int = -6

Foldable[List].foldRight(List(1, 2, 3), Eval.now(0))((num, eval) => eval.map(num - _)).value
//res4: Int = 2




// `Foldable` also provides us with a lot of the same methods that
// are defined in the collections from the standard library such
// as: `find`, `exists`, `forall`, `toList`, `isEmpty`, `nonEmpty`,
// etc.

Foldable[List].find(List(1, 2, 3))(_ % 2 == 0)
//res5: Option[Int] = Some(2)




// In addition, `Foldable` provides 2 methods that make use of
// `Monoid`s:

// `combineAll` combines all elements in the sequence using their
// `Monoid`:

Foldable[List].combineAll(List(1, 2, 3))
//res6: Int = 6

// `foldMap` maps a function over the sequence and then combines
// the results using a `Monoid`:

Foldable[List].foldMap(List(1, 2, 3))(_ + 1)
//res7: Int = 9




// You can also compose Foldables to support deep traversal of
// nested sequences:

val ints = List(Vector(1, 2, 3), Vector(4, 5, 6))

(Foldable[List] compose Foldable[Vector]).combineAll(ints)
//res8: Int = 21






