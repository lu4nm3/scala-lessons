// ****************************************************************
//                        Monad Transformers
// ****************************************************************
import cats.data.{EitherT, OptionT}
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

// Here, `ErrorOrOption[A]` is equivalent to `ErrorOr[Option[A]]`
// which is the same as `Either[String, Option[A]]`.

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
// more monads together. As an example, let's say we wanted to
// create a `Task` of an `Either` of `Option`:

type TaskEither[A] = EitherT[Task, String, A] // equivalent to `Task[Either[String, A]]`

type TaskEitherOption[A] = OptionT[TaskEither, A] // equivalent to `Task[Either[String, Option[A]]]`

for {
  a <- 3.pure[TaskEitherOption]
  b <- 2.pure[TaskEitherOption]
} yield a * b
// res1: cats.data.OptionT[TaskEither,Int] = OptionT(EitherT(Task.FlatMap$1596291944))




// Using monad transformers can be difficult because they combine
// monads together in predefined ways. It can also be difficult to
// reason about how the monads they represent are composed. We can
// approach this in multiple ways:

// One approach involves creating a single "super stack" and using
// it throughout a code base. This works if the code is simple and
// largely uniform in nature. For example:

sealed trait HttpError
case class NotFound(s: String) extends HttpError
case class BadRequest(s: String) extends HttpError

type HttpTaskEither[A] = EitherT[Task, HttpError, A] // equivalent to Task[Either[HttpError, A]]

// The "super stack" approach starts to fail in larger, more
// heterogeneous code bases where different stacks make sense in
// different contexts.

// A different design pattern that makes more sense in these
// situations uses monad transformers as local "glue code". We
// expose untransformed stacks at module boundaries, transform them
// to operate on them locally, and unwrap them before passing them
// on:

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




