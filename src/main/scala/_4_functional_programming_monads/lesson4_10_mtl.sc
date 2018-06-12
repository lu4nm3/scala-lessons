// ****************************************************************
//                  Monad Transformer Library (MTL)
// ****************************************************************
import cats.Monad
import monix.eval.Task

import scala.language.higherKinds




// Monad transformers aren't all bad, however. In Haskell, the MTL
// library introduced type classes for abstracting over data types
// that support the same effects.

// What is known to as MTL-style does not refer to the use of monad
// transformers, per se, but to the use of the type classes that
// allow abstracting over the effects modeled by data structure.

// For example, the type class `MonadState` could abstract over all
// data types that are capable of supporting getting and setting
// state including `State` and even the monad transformer `StateT`.

// As an example, let's say that our application is using a
// `Map[String, String]` to store some state. Rather than us
// `StateT[F, S, A]`, which is a monad transformer that adds state
// management to some base monad F[_], we will instead use the
// `MonadState` type class:


trait MonadState[F[_], S] extends Monad[F] {
  self =>
  def get: F[S]

  def put(s: S): F[Unit]
}

case class AppState(values: List[Int])

import cats.implicits._

def proram[F[_]](implicit S: MonadState[F, AppState]): F[Unit] = {
  for {
    state <- S.get
    newValues = state.values :+ 3
    _ <- S.put(state.copy(newValues))
  } yield ()
}

implicit val mapTaskMonadState: MonadState[Task, AppState] = new MonadState[Task, AppState] {
  var state = AppState(List.empty[Int])

  def get: Task[AppState] = Task.now(state)

  def put(s: AppState): Task[Unit] = Task.now { state = s }

  def flatMap[A, B](fa: Task[A])(f: A => Task[B]) = fa.flatMap(f)

  def tailRecM[A, B](a: A)(f: A => Task[Either[A, B]]) = Task.tailRecM(a)(f)

  def pure[A](x: A) = Task.pure(x)
}
// ****************************************************************