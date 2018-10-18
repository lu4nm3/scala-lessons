<h4 align="right">
    <a href="lesson4_1_monads.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson4_3_monad_error.md">Next →</a>
</h4>

<h1>Id</h1>

The identity monad is a type alias that turns a regular type into a single parameter type constructor:

```scala
type Id[A] = A
```

`Id` can be thought of as the monad that encodes the effect of having no effect. In that sense, plain old values are 
also values of `Id`:

```scala
val s: Id[String] = "Dan"

val i: Id[Int] = 3

val l: Id[List[Int]] = List(1, 2, 3)
```

Cats provides instances of various type classes for `Id` including the `Monad` type class.

```scala
import cats.{Id, Monad}

val m1 = Monad[Id].pure(3) // Id[Int]

val m2 = Monad[Id].pure(2) // Id[Int]

import cats.implicits._

for {
  a <- m1
  b <- m2
} yield a * b
//res0: cats.Id[Int] = 6
```

<h3>Abstracting over F[_]</h3>

The ability to abstract over monadic and non-monadic code is extremely powerful. For example, say we had a service class 
that abstracted over some `F[_]`:

```scala
import scala.language.higherKinds

trait Service[F[_]] {
  def findUser: F[String]
}
```

We can then have separate implementations where things run asynchronously in production using Monix `Task`:

```scala
import monix.eval.Task

val asyncService = new Service[Task] {
  def findUser: Task[String] = Task("...")
}
```

And synchronously for unit tests using `Id`:

```scala
import cats.Id

val syncService = new Service[Id] {
  def findUser: Id[String] = "..."
}
```

<h4 align="right">
    <a href="lesson4_1_monads.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson4_3_monad_error.md">Next →</a>
</h4>