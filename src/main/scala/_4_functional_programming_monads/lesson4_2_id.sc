// ****************************************************************
//                              Id
// ****************************************************************




// The identity monad is a type alias that turns an atomic type
// into a single parameter type constructor:

type Id[A] = A




// `Id` can be thought of as the ambient monad that encodes the
// effect of having no effect. It is ambient in the sense that
// plain pure values are values of `Id`.

val s: Id[String] = "Dan"

val i: Id[Int] = 3

val l: Id[List[Int]] = List(1, 2, 3)




// Cats provides instances of various type classes for `Id`
// including `Functor` and `Monad`.

import cats.Monad

val m1 = Monad[Id].pure(3) // Id[Int]

val m2 = Monad[Id].pure(2) // Id[Int]

import cats.syntax.flatMap._
import cats.syntax.functor._

for {
  a <- m1
  b <- m2
} yield a * b




// The ability to abstract over monadic and non-monadic code is
// extremely powerful. For example, we can run code asynchronously
// in production using `Task` and synchronously for testing using
// `Id`:

import monix.eval.Task
import scala.language.higherKinds

trait Service[F[_]] {
  def findUser: F[String]
}

// for prod
val asyncService = new Service[Task] {
  def findUser: Task[String] = Task("...")
}

// for testing
val syncService = new Service[Id] {
  def findUser: Id[String] = "..."
}
