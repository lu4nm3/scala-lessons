// ****************************************************************
//                        Monad Transformers
// ****************************************************************
import cats.data.EitherT
import cats.{Applicative, Functor, Monad}
import monix.eval.Task
import scala.language.higherKinds




// A monad transformer is a data type that allows us to wrap stacks
// of monads to produce new monads. Cats defines transformers for a
// variety of monads, each providing extra functionality that we
// need to compose that monad with others.

// Cats provides transformers for many monads, each named with a T
// suffix (eg. OptionT, EitherT, etc). These transformers provide
// `map` and `flatMap` operations that allow us to use both
// component monads without having to recursively unpack and repack
// values at each stage in the computation.

// All of the monad transformers follow the same convention. The
// transformer itself represents the inner monad in a stack, while
// the first type parameter specifies the outer monad. The
// remaining type parameters are the types we use to form the
// corresponding monads.

// We build monad stacks from the inside out starting with the
// inner monad (which is also given by the name of the monad
// transformer itself):

type ErrorOr[A] = Either[String, A]
type ErrorOrOption[A] = OptionT[ErrorOr, A]

// We can use `pure`, `map`, and `flatMap` as usual to create and
// compose instances:

import cats.instances.either._
import cats.syntax.applicative._

val a = 3.pure[ErrorOrOption]
// a: ErrorOrOption[Int] = OptionT(Right(Some(3)))

val b = 2.pure[ErrorOrOption]
// b: ErrorOrOption[Int] = OptionT(Right(Some(2)))

for {
  aa <- a
  bb <- b
} yield aa * bb
// res0: cats.data.OptionT[ErrorOr,Int] = OptionT(Right(Some(6)))




// Things start to become even more confusing when we stack 3 or
// more monads. As an example, let's say we wanted to create a
// Task of an Either of Option:

type TaskEither[A] = EitherT[Task, String, A]

type TaskEitherOption[A] = OptionT[TaskEither, A]

for {
  a <- 3.pure[TaskEitherOption]
  b <- 2.pure[TaskEitherOption]
} yield a * b
// res1: cats.data.OptionT[TaskEither,Int] = OptionT(EitherT(Task.FlatMap$1596291944))




// Using monad transformers can be difficult because they combine
// monads together in predefined ways. We can approach this in
// multiple ways:

// One approach involves creating a single "super stack" and using
// it throughout a code base. This works if the code is simple and
// largely uniform in nature. For example:

sealed trait HttpError
case class NotFound(s: String) extends HttpError
case class BadRequest(s: String) extends HttpError

type HttpTaskEither[A] = EitherT[Task, HttpError, A]

// The "super stack" approach starts to fail in larger, more
// heterogeneous code bases where different stacks make sense in
// different contexts.

// A different design pattern that makes more sense in these
// situations uses monad transformers as local "glue code". We
// expose untransformed stacks at module boundaries, transform them
// to operate on them locally, and untransform them before passing
// them on:

case class User(age: Int)

def findInDb(user: String): Task[Option[User]] = ???

def processUser(user1: String, user2: String, user3: String): Task[Option[Int]] = {
  val result = for {
    usr1 <- OptionT(findInDb(user1))  // use monad transformers locally to simplify composition
    usr2 <- OptionT(findInDb(user2))
    usr3 <- OptionT(findInDb(user3))
  } yield (usr1.age + usr2.age + usr3.age) / 3

  result.value // return untransformed stack
}




// Credit to this next part and the next lesson goes to John A De
// Goes for his blog post about monad transformers and MTL:
//
// http://degoes.net/articles/effects-without-transformers

// Despite the composition benefits that monad transformers offer,
// they impose tremendous performance overhead that rapidly leads
// to CPU-bound, memory-hogging applications.

// Monad transformers rely on vertical composition to layer new
// effects onto other effects. The following is a short snippet of
// the OptionT's `pure`, `map`, and `flatMap` operations:

final case class OptionT[F[_], A](value: F[Option[A]]) {
  def map[B](f: A => B)(implicit F: Functor[F]): OptionT[F, B] = {
    OptionT(F.map(value)(_.map(f)))
  }

  def flatMap[B](f: A => OptionT[F, B])(implicit F: Monad[F]): OptionT[F, B] = {
    flatMapF(a => f(a).value)
  }

  def flatMapF[B](f: A => F[Option[B]])(implicit F: Monad[F]): OptionT[F, B] = {
    OptionT(F.flatMap(value)(_.fold(F.pure[Option[B]](None))(f)))
  }
}

object OptionT {
  def pure[F[_], A](value: A)(implicit F: Applicative[F]): OptionT[F, A] =
    OptionT(F.pure(Some(value)))
}

// From the code above, one can see that the transformer imposes
// additional indirection and heap usage for every usage of `pure`,
// `map` and `flatMap`.

// The definition of the class introduces a wrapper OptionT.
// Compared to the original effect F[_], use of this wrapper will
// involve 2 additional allocations: 1 for Option and 1 for OptionT
// which will triple the memory consumption of the application:

val option = Some(3)        // 1st allocation
val task = Task.now(option) // 2nd allocation
val optionT = OptionT(task) // 3rd allocation

// In the `map` function of OptionT, there are 3 additional method
// calls (`OptionT.apply`, `map`, `map`) and an additional 2
// allocations (OptionT, _), one for the OptionT wrapper and one
// for the anonymous function. The story for the `flatMap`
// operation is similar.


// ****************************************************************