// ****************************************************************
//                  Monad Transformer Library (MTL)
// ****************************************************************
//
// Credit to this lesson goes to John A De Goes for his blog post
// about monad transformers and MTL:
//
// http://degoes.net/articles/effects-without-transformers
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Applicative, FlatMap, Functor, Monad, Monoid, Semigroup}
import scala.language.higherKinds




// Despite the composition benefits that monad transformers offer,
// they impose tremendous performance overhead that rapidly leads
// to CPU-bound, memory-hogging applications.

// Monad transformers rely on vertical composition to layer new
// effects onto other effects. The following is a short snippet of
// OptionT's `pure`, `map`, and `flatMap` operations:

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
  def pure[F[_], A](value: A)(implicit F: Applicative[F]): OptionT[F, A] = {
    OptionT(F.pure(Some(value)))
  }
}

// From the code above, one can see that the transformer imposes
// additional indirection and heap usage for every usage of `pure`,
// `map` and `flatMap`.

// The definition of the class introduces a wrapper `OptionT`.
// Compared to the original effect, F[_], use of this wrapper will
// involve 2 additional allocations when lifting pure values into
// the monad transformer context: 1 for `Option` and 1 for
// `OptionT` which will triple the memory consumption of the
// application. In the following example:

for {
  _ <- OptionT[IO, Int](IO(Some(5)))
  _ <- OptionT.pure[IO, Int](3)
} yield ()

// When we call `pure` above to lift the value 3 into the OptionT
// context, we're tripling the memory usage by having 2 additional
// allocations:

val alloc1 = Some(3)                  // additional allocation
val alloc2 = IO.pure(alloc1)
val alloc3 = OptionT[IO, Int](alloc2) // additional allocation

// Furthermore, in the `map` function of OptionT, there are 3
// additional method calls (`OptionT.apply`, `map`, `map`) and an
// additional 2 allocations (OptionT, _), one for the OptionT
// wrapper and one for the anonymous function. The story for the
// `flatMap` operation is similar.




// `OptionT` was a simple example that demonstrates the issues with
// monad transformers. In practice, however, you often times need
// to combine several behaviors together which means composing 2 or
// more monad transformers.

// As another example, let's say that we want to use the State
// monad to keep track of the state of our program along with the
// Writer monad to keep a log of all the different values the state
// was in. Out of the box, Cats provides a monad transformer called
// `IndexedReaderWriterStateT` which combines the Reader, Writer,
// and State monads together. Below is a simplified version of this
// monad transformer that only provides the Writer and State
// functionality that we need:

final class WriterStateT[F[_], L, SA, SB, A](val runF: F[SA => F[(L, SB, A)]]) {
  def flatMap[SC, B](f: A => WriterStateT[F, L, SB, SC, B])
                    (implicit F: FlatMap[F], L: Semigroup[L]): WriterStateT[F, L, SA, SC, B] = {
    WriterStateT.applyF[F, L, SA, SC, B] {
      F.map(runF) { wsfa =>
        (sa: SA) => {
          F.flatMap(wsfa(sa)) { case (la, sb, a) =>
            F.flatMap(f(a).runF) { wsfb =>
              F.map(wsfb(sb)) { case (lb, sc, b) =>
                (L.combine(la, lb), sc, b)
              }
            }
          }
        }
      }
    }
  }

  /**
    * Modify the result of the computation using `f`.
    */
  def map[B](f: A => B)(implicit F: Functor[F]): WriterStateT[F, L, SA, SB, B] = {
    transform { (l, s, a) => (l, s, f(a)) }
  }

  /**
    * Transform the resulting log, state and value using `f`.
    */
  def transform[LL, SC, B](f: (L, SB, A) => (LL, SC, B))(implicit F: Functor[F]): WriterStateT[F, LL, SA, SC, B] = {
    WriterStateT.applyF[F, LL, SA, SC, B] {
      F.map(runF) { wsfa =>
        (sa: SA) =>
          F.map(wsfa(sa)) { case (l, sb, a) =>
            val (ll, sc, b) = f(l, sb, a)
            (ll, sc, b)
          }
      }
    }
  }

  /**
    * Run the computation using the provided initial state.
    */
  def run(initial: SA)(implicit F: Monad[F]): F[(L, SB, A)] = {
    F.flatMap(runF)(_.apply(initial))
  }
}

object WriterStateT {
  def apply[F[_], L, SA, SB, A](runF: SA => F[(L, SB, A)])
                               (implicit F: Applicative[F]): WriterStateT[F, L, SA, SB, A] = {
    new WriterStateT[F, L, SA, SB, A](F.pure(runF))
  }

  def applyF[F[_], L, SA, SB, A](runF: F[SA => F[(L, SB, A)]]): WriterStateT[F, L, SA, SB, A] = {
    new WriterStateT[F, L, SA, SB, A](runF)
  }

  def pure[F[_], L, S, A](a: A)(implicit F: Applicative[F], L: Monoid[L]): WriterStateT[F, L, S, S, A] = {
    WriterStateT(s => F.pure((L.empty, s, a)))
  }

  /**
    * Return the input state without modifying it.
    */
  def get[F[_], L, S](implicit F: Applicative[F], L: Monoid[L]): WriterStateT[F, L, S, S, S] = {
    WriterStateT(s => F.pure((L.empty, s, s)))
  }

  /**
    * Set the state to `s`.
    */
  def set[F[_], L, S](s: S)(implicit F: Applicative[F], L: Monoid[L]): WriterStateT[F, L, S, S, Unit] = {
    WriterStateT(_ => F.pure((L.empty, s, ())))
  }

  /**
    * Add a value to the log, without modifying the input state.
    */
  def tell[F[_], L, S](l: L)(implicit F: Applicative[F]): WriterStateT[F, L, S, S, Unit] = {
    WriterStateT(s => F.pure((l, s, ())))
  }
}

// Immediately, you will be able to see the amount of complexity in
// the `map` and `flatMap` methods of the monad transformer both in
// terms of the number of nested function calls and new object
// allocations. The reason for this complexity is that the monad
// transformer has to combine the behaviors of both the State and
// the Writer monads together in order to ensure that their
// behavior remains the same even in combination. For the State
// monad this means be able to "thread" a starting state value `S`
// through what you can think of as multiple functions of type
// `S => (S, A)` as it gets repeatedly updated throughout the
// program's execution. Similarly for the Writer monad, this means
// being able to append to the current log `L` while threading the
// log through the program's execution without losing any values
// that were previously there. Coming up with this combined logic
// can be very complex and is hard to reason about as seen above in
// the `map` and `flatMap` methods.

// This complexity doesn't even begin to account for the amount of
// heap memory an application will require when using this monad
// transformer not to mention the amount of churn that the constant
// wrapping and un-wrapping will create. Consequently this can
// create more frequent, and potentially longer-lasting, garbage
// collection pauses in the JVM which is something to keep in mind
// if your application requires a high level of performance.

// Using this monad transformer, we can write a "simple" program
// that makes use of it. Here, we will use our own case class
// `AppState` to represent our state and we're going to
// parameterize our custom `WriterStateT` monad transformer on
// `Vector[AppState]` for our log, which will keep track of the
// different values that our state was in:

case class AppState(value: Int) // The "state" of our application

def program[F[_]](implicit M: Monad[F]): WriterStateT[F, Vector[AppState], AppState, AppState, AppState] = {
  for {
    state <- WriterStateT.get[F, Vector[AppState], AppState]

    _ <- WriterStateT.tell[F, Vector[AppState], AppState](Vector(state))

    x <- WriterStateT.pure[F, Vector[AppState], AppState, AppState](AppState(state.value * 3))  // some "computation" that uses the program's state
    _ <- WriterStateT.set[F, Vector[AppState], AppState](x)                                     // update the state with a new value
    _ <- WriterStateT.tell[F, Vector[AppState], AppState](Vector(x))                            // log the value of the new state

    state2 <- WriterStateT.get[F, Vector[AppState], AppState]

    y <- WriterStateT.pure[F, Vector[AppState], AppState, AppState](AppState(state2.value + 2))
    _ <- WriterStateT.set[F, Vector[AppState], AppState](y)
    _ <- WriterStateT.tell[F, Vector[AppState], AppState](Vector(y))

    finalState <- WriterStateT.get[F, Vector[AppState], AppState]
  } yield finalState
}

// Then we execute the program using the `run` method and supply it
// with an initial state:

program[IO].run(AppState(3)).unsafeRunSync()
// res0: (Vector[AppState], AppState) = (Vector(AppState(3), AppState(9), AppState(11)), AppState(11))




// Monad transformers aren't all bad, however. In Haskell, the MTL
// library introduced type classes for abstracting over data types
// that support the same effects.

// What is known as MTL-style does not refer to the use of monad
// transformers, per se, but to the use of the type classes that
// allow abstracting over the effects modeled by these monad
// transformers.

// For example, the type class `MonadState` could abstract over all
// data types that are capable of supporting getting and setting
// state including the `State` monad and even the `StateT` monad
// transformer.

// Rather than using `StateT[F, S, A]`, which is a monad
// transformer that adds state management to some base monad F[_],
// we will instead use our own `MonadState` type class to represent
// the state management functionality we need:

trait MonadState[F[_], S] {
  def monad: Monad[F]

  def get: F[S]

  def set(s: S): F[Unit]
}

// Just like before, we also want some way to keep track of the
// history of the different values that our state was in. Instead
// of using the `Writer` monad, we're going to create another type
// class called `MonadWriter` that provides the Writer-like
// functionality needed to log all the previous values the state
// was in:

trait MonadWriter[F[_], L] {
  def monad: Monad[F]

  def tell(l: L): F[Unit]

  def logs: F[L]
}

// With the 2 effect type classes that we defined above, we can
// create a program that makes use of them. Our program will be
// parameterized on `F[_]` which represents some effect "context"
// that it will run inside of. Furthermore, the program will rely
// on instances of `MonadState` and `MonadWriter`. These instances
// will be parameterized on our own class `AppState` to represent
// the state of the program and on `Vector[AppState]` to keep track
// of the history of our state. The program will then return both
// the log of values the state was in as well as the final value of
// the state:

def program[F[_]](S: MonadState[F, AppState],
                  W: MonadWriter[F, Vector[AppState]]): F[(Vector[AppState], AppState)] = {
  implicit val monad: Monad[F] = S.monad

  for {
    state <- S.get

    _ <- W.tell(Vector(state))

    x <- monad.pure(AppState(state.value * 3)) // some "computation" in the program
    _ <- S.set(x)
    _ <- W.tell(Vector(x))

    state2 <- S.get

    y <- monad.pure(AppState(state2.value + 2))
    _ <- S.set(y)
    _ <- W.tell(Vector(y))

    logs <- W.logs
    finalState <- S.get
  } yield (logs, finalState)
}

// In order to create `MonadState` instances, we will define the
// following method that parameterizes `MonadState` on cats-effect
// `IO`. It's possible to use the `StateT` monad transformer
// internally to manage the state of our `MonadState` type class
// but instead, we will opt for using `Ref` (also from cats-effect)
// for simplicity, to keep track of our state:

def createState[S](initial: S): IO[MonadState[IO, S]] = {
  for {
    state <- Ref.of[IO, S](initial)
  } yield new MonadState[IO, S] {
    def monad: Monad[IO] = Monad[IO]

    def get: IO[S] = state.get

    def set(s: S): IO[Unit] = state.set(s)
  }
}

// We will also define a method to create instances of
// `MonadWriter` that is parameterized on `IO` and uses `Ref`.
// This method will need a `Monoid[L]` which will be used to create
// empty instances of our log `L` as well as to combine multiple
// values of `L` together:

def createWriter[L](implicit monoid: Monoid[L]): IO[MonadWriter[IO, L]] = {
  for {
    writer <- Ref.of[IO, L](monoid.empty)
  } yield new MonadWriter[IO, L] {
    def monad: Monad[IO] = Monad[IO]

    def tell(l: L): IO[Unit] = writer.update(_.combine(l))

    def logs: IO[L] = writer.get
  }
}

// Once we have all of these components in place, we can assemble
// them together and execute the program:

def main(): IO[(Vector[AppState], AppState)] = {
  for {
    monadState <- createState(AppState(3))
    monadWriter <- createWriter[Vector[AppState]]
    result <- program(monadState, monadWriter)
  } yield result
}

main().unsafeRunSync()
// res0: (Vector[AppState], AppState) = (Vector(AppState(3), AppState(9), AppState(11)),AppState(11))




// Here we can see some big differences between the MTL-style of
// composing effects and the monad transformer approach.

// The most notable difference is the reduced complexity in the
// MTL-style over monad transformers.
//
// With MTL, we can specify type classes for the
// behaviors that we're interested in separately, and then compose
// them together within our program. This is because with MTL, the context that the different operations
// of our program run within is the `F` monad which already provides the
// `map` and `flatMap` methods needed to compose
// things together.
//
// With monad transformers, on the other hand, we are forced to combine the
// behaviors of different monads together within a new, custom monad transformer "wrapper".
// In this wrapper, we define custom `map` and `flatMap` methods that we use for composing
// multiple instances of our wrapper together. These methods must then ensure that the original behaviors
// of the monads that are being composed remain the same as when they are used separately which is
// no easy task.
//
// Another benefit of the MTL-style is the added re-usability that it provides.
// By defining small and focused type classes for the behaviors we're looking for,
// it becomes easy to re-use them. If some part of an application only
// needs state management, for example, then it doesn't make sense to
// also include writer-related functionality. Unfortunately, with monad transformers,
// you're forced to combine behaviors together which means that you
// have to bring along the entire functionality of the transformer even if you only use a small
// part of it.
//
//
// Furthermore, with MTL-style, we are able to customize the type classes by making them
// as simple or as complex as we need them to be by
// including only the behavior that we're interested. This helps us
// avoid bloating our programs with functionality we dont need.
// In addition, we get the opportunity to re-define behaviors in
// more efficient ways from how they're normally defined.
//
// Using the above example, let's consider
// the state monad `State[S,A]` which is commonly represented as a function
// of type S => (S, A) where `S` is the type of the state and `A` is the type
// of the result such that an input state is transformed to an output state
// along with a result. If we only care about the "state" portion of the `State`
// monad, we can define an MTL
// type class, `MonadState[F[_], S]`, like we did above that only captures the state behavior
// and does away with anything related to the result.







