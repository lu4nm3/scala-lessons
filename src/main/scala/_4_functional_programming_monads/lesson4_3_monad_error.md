<h1>MonadError</h1>

Cats provides a monad-like type class called `MonadError` that abstracts over data types that are used for error 
handling. It provides extra operations for raising and handling errors:

```scala
trait MonadError[F[_], E] extends Monad[F] {
  // Lifts an error `E` into the `F` context. This is like the
  // `pure` operation in `Monad` except that it creates an instance
  // representing a failure.
  def raiseError[A](e: E): F[A]

  // Handles an error `E`, potentially recovering from it. It
  // allows us to consume an error and possibly turn it into a
  // success.
  def handleError[A](fa: F[A])(f: E => A): F[A]

  // Implements filter-like behavior that tests the value of a
  // successful monad `fa` with a predicate `f` and specifies an
  // error to raise if the predicate returns false.
  def ensure[A](fa: F[A])(e: E)(f: A => Boolean): F[A]
}
```

Let's look at an example of a `MonadError` for Cats Effect's `IO` where our error `E` is of type `Throwable`. 

We can lift values that we consider to be "successful" up to a `MonadError` using `pure` just like with `Monad`:

```scala
import cats.MonadError
import cats.effect.IO

scala> MonadError[IO, Throwable].pure(3)
res0: cats.effect.IO[Int] = IO(3)
```

We can also lift "errors" up to a `MonadError` using `raiseError`:

```scala
scala> val error = MonadError[IO, Throwable].raiseError[Int](new NullPointerException())
error: cats.effect.IO[Int] = IO(throw java.lang.NullPointerException)
```

And in the case of errors, we can use `handleError` to recover from them them:

```scala
val recover = MonadError[IO, Throwable].handleError(error) {
  case _: NullPointerException => 0
  case _ => 1
}

scala> recover.unsafeRunSync()
res1: Int = 0
```

In addition, we can use `ensure` to check whether the contents of a `Monad` meet certain criteria and raise an error if 
they don't: 

```scala
monadError.ensure(success)(new IllegalArgumentException("Number is too low!"))(_ > 5)
// res3: Task[Int] = Task(new IllegalArgumentException("Number is too low!"))
```