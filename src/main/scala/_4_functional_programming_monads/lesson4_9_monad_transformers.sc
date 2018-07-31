// ****************************************************************
//                        Monad Transformers
// ****************************************************************
import cats.data.{EitherT, OptionT}
import monix.eval.Task
import scala.language.higherKinds




// Often times you will run into situations where you need to work
// with multiple monads in combination. For example, say we're
// working with a database to retrieve user information. In this
// case since we don't want to block our program while interacting
// with the database, we use `Task` to do things asynchronously. In
// addition, the `User` value that we're looking for may or may not
// exist so we'll use `Option` to represent this:

case class User(id: String, age: Int)

def getUser(id: String): Task[Option[User]] = ???

def lookUpUserAge(id: String): Task[Option[Int]] = {
  for {
    optUser <- getUser(id)
  } yield {
    for {
      user <- optUser
    } yield user.age
  }
}

// You will notice that in order to retrieve the age of a user, we
// have to nest calls to `flatMap` (ie. for-comprehensions). This
// is because first we need to access the optional `User` value
// from within the `Task` before we can get to the `age` field of
// the `User` itself. The more complex your data structures, the
// more tedious this gets.




// A monad transformer is a data type that wraps stacks of monads
// in order to produce new monads. Cats defines transformers for a
// variety of monads, each named with a `T` suffix (eg. `OptionT`,
// `EitherT`, etc). These transformers provide `map` and `flatMap`
// operations that allow us to use nested monads without having to
// recursively pack and unpack them at every step of a computation.

// All of the monad transformers follow the same convention. The
// transformer itself represents the inner monad in a stack, while
// the first type parameter specifies the outer monad. The
// remaining type parameters will vary based on the transformer
// since they are the types needed by the corresponding inner
// monad.

// We build monad stacks from the inside out starting with the
// inner monad (which is also given by the name of the monad
// transformer itself):

type TaskOption[A] = OptionT[Task, A]

// Here, `TaskOption[A]` is equivalent to `Task[Option[A]]`.

// We can use `pure`, `map`, and `flatMap` as usual to create and
// compose instances:
import cats.syntax.applicative._

val s = 3.pure[TaskOption]
// s: TaskOption[Int] = OptionT(Task.Now(Some(3)))

val t = 2.pure[TaskOption]
// t: TaskOption[Int] = OptionT(Task.Now(Some(2)))

for {
  ss <- s
  tt <- t
} yield ss * tt
// res0: OptionT[Task,Int] = OptionT(Task.FlatMap$950467863)




// We can also rewrite our original database example in terms of
// `OptionT` in order to get rid of the nested calls to `flatMap`:

def lookUpUserAge2(id: String): Task[Option[Int]] = {
  val result: OptionT[Task, Int] = for {
    optUser <- OptionT(getUser(id))
  } yield optUser.age

  result.value // use `.value` to retrieved the wrapped monad
}




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

def findInDb(user: String): Task[Option[User]] = ???

def processUser(user1: String, user2: String, user3: String): Task[Option[Int]] = {
  val result = for {
    usr1 <- OptionT(findInDb(user1))  // use monad transformers locally to simplify composition
    usr2 <- OptionT(findInDb(user2))
    usr3 <- OptionT(findInDb(user3))
  } yield (usr1.age + usr2.age + usr3.age) / 3

  result.value // return untransformed stack
}