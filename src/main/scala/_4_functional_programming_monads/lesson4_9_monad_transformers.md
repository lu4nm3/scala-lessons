<h4 align="right">
    <a href="lesson4_8_state.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
</h4>

<h1>Monad Transformers</h1>

Often times you will run into situations where you need to work with multiple monads in combination. Monad transformers
are one approach to tackling this problem.

<h2>Use case</h2>

For example, say we're working with a database to retrieve user information. In this case since we don't want to block 
our program while interacting with the database, we use `IO` to do things asynchronously. In addition, the `User` 
value that we're looking for may or may not exist so we'll use `Option` to represent this:

```scala
import cats.effect.IO

case class User(id: String, age: Int)

def getUser(id: String): IO[Option[User]] = ??? // gets a User from db

def lookUpUserAge(id: String): IO[Option[Int]] = {
  getUser(id).map { optUser =>
    optUser.map(user => user.age)
  }
}
```

You will notice that in order to retrieve the age of a user, we have to nest calls to `map`. This is because first we 
need to access the optional `User` value from within the `IO` before we can get to the `age` field of the `User` itself.
The more complex your data structures, the more tedious this gets.

<h2>Monad Transformers</h2>

A monad transformer is a data type that wraps stacks of monads in order to produce new monads. The Cats library defines 
transformers for a variety of monads, each named with a `T` suffix (eg. `OptionT`, `EitherT`, etc). These transformers 
provide `map` and `flatMap` operations that allow us to use nested monads without having to recursively pack and unpack 
them at every step of a computation.

<h3>Naming convention</h3>

All of the monad transformers follow the same convention. The transformer itself (ie. its name) represents the inner 
monad of the stack, while the first type parameter specifies the outer monad. The remaining type parameters will vary 
based on the transformer since they are the types needed by the corresponding inner monad.

For example: 

```scala
OptionT[F[_], A] 
```

is a monad transformer that wraps a monad stack that looks like `F[Option[A]]`, where `F[_]` is the outer monad, 
`Option` is the inner monad (as specified by the transformer's name **`OptionT`**), and `A` is the type of the value 
inside `Option[_]`.

<h3><code>map</code> & <code>flatMap</code></h3>

The Cats library exposes the `pure`, `map`, and `flatMap` methods on its monad transformers just like with any other 
monad which you can use to build and compose transformer instances together:

```scala
import cats.effect.IO
import cats.data.OptionT
import cats.implicits._

@ val aa = 3.pure[OptionT[IO, ?]]
aa: cats.data.OptionT[cats.effect.IO,Int] = OptionT(IO(Some(3)))

@ val bb = 2.pure[OptionT[IO, ?]]
bb: cats.data.OptionT[cats.effect.IO,Int] = OptionT(IO(Some(2)))

@ val result = for {
     |   a <- aa
     |   b <- bb
     | } yield a * b
result: cats.data.OptionT[cats.effect.IO,Int] = OptionT(IO$815725629)

@ result.value
res0: cats.effect.IO[Option[Int]] = IO$815725629

@ result.value.unsafeRunSync
res0: Option[Int] = Some(6)
```

We can also rewrite our original database example in terms of `OptionT` in order to get rid of the nested calls to 
`map`:

```scala
def lookUpUserAge(id: String): IO[Option[Int]] = {
  OptionT(getUser(id))        // OptionT[IO, User]
    .map(user => user.age)    // OptionT[IO, Int]
    .value                    // IO[Option[Int]]
}
```

<h3>What about big monad stacks?</h3>

Things start to become even more confusing when we stack 3 or more monads together. For example, say we wanted to create
an `IO` of an `Either` of `Option`:

```scala
@ 3.pure[OptionT[EitherT[IO, String, ?], ?]]
res18: cats.data.OptionT[[γ$1$]cats.data.EitherT[cats.effect.IO,String,γ$1$],Int] = OptionT(EitherT(<function1>))
```

We can rewrite this to show the breakdown of the monad transformer composition:

```scala
type IOEither[A] = EitherT[IO, String, A]       // equivalent to `IO[Either[String, A]]`

type IOEitherOption[A] = OptionT[IOEither, A]   // equivalent to `IO[Either[String, Option[A]]]`

@ 3.pure[IOEitherOption]
res19: IOEitherOption[Int] = OptionT(EitherT(<function1>))
```

Using monad transformers can be difficult because they combine monads together in predefined ways. It can also be 
difficult to reason about how the monads they represent are composed. We can approach this in multiple ways:

<h4>Use a "super stack"</h4>

One approach involves creating a single "super stack" and using it throughout a code base. This works if the code is 
simple and largely uniform in nature. For example:

```scala
sealed trait HttpError
case class NotFound(s: String) extends HttpError
case class BadRequest(s: String) extends HttpError

type HttpIOEither[A] = EitherT[IO, HttpError, A]    // equivalent to IO[Either[HttpError, A]]
```

The "super stack" approach starts to fail in larger, more heterogeneous code bases where different stacks make sense in
different contexts.

<h4>Monad transformers as "glue code"</h4>

A different design pattern that makes more sense in these situations uses monad transformers as local "glue code". We
expose untransformed stacks at module boundaries, transform them to operate on them locally, and unwrap them before 
passing them on:

```scala
def getUser(id: String): IO[Option[User]] = ???

def avgAge(userId1: String, userId2: String, userId3: String): IO[Option[Int]] = {
  val result = for {
    user1 <- OptionT(getUser(userId1))  // use monad transformers locally to simplify composition
    user2 <- OptionT(getUser(userId2))
    user3 <- OptionT(getUser(userId3))
  } yield (user1.age + user2.age + user3.age) / 3

  result.value // return untransformed stack
}
```

<h4 align="right">
    <a href="lesson4_8_state.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
</h4>