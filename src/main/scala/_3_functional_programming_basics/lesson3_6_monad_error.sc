// ****************************************************************
//                          Monad Error
// ****************************************************************
import cats.{Monad, MonadError}
import monix.eval.Task

import scala.language.higherKinds




// Cats provides an additional monad-like type class called
// `MonadError` that abstracts over data types that are used for
// error handling. It provides extra operations for raising and
// handling errors:

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




val monadError = MonadError[Task, Throwable]

val success = monadError.pure(3)
// success: Task[Int] = Task(3)

val failure = monadError.raiseError(new NullPointerException())
// failure: Task[Nothing] = Task(NullPointerException)

monadError.handleError(failure) {
  case _: NullPointerException => monadError.pure(0)
  case _ => monadError.raiseError(new InterruptedException())
}
// res2: Task[Int] = Task(0)

monadError.ensure(success)(new IllegalArgumentException("Number is too low!"))(_ > 5)
// res3: Task[Int] = Task(new IllegalArgumentException("Number is too low!"))





