<h4 align="right">
    <a href="../_5_functional_programming_applicatives/lesson5_5_traverse.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a>
</h4>

<h1>MTL</h1>

<h2>Monad Transformers</h2>

In an earlier lesson, we looked at monad transformers and how you could use them to compose stacks of monads together.

Going back to an earlier example, we saw how we could take a stack of monads (`IO[Option[A]]` in this case) where you
had to nest multiple calls to `map`:

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

And simplify it with the `OptionT` monad transformer:

```scala
import cats.data.OptionT
import cats.implicits._

def lookUpUserAge(id: String): IO[Option[Int]] = {
  OptionT(getUser(id))        // OptionT[IO, User]
    .map(user => user.age)    // OptionT[IO, Int]
    .value                    // IO[Option[Int]]
}
```

Despite the composition benefits that monad transformers offer, they impose a big performance overhead that rapidly leads
to CPU-bound, memory-hogging applications.

<h3>Performance implications</h3>

Monad transformers rely on vertical composition to layer new effects onto other effects. The following is a short
snippet of `OptionT`'s implementation of its `pure`, `map`, and `flatMap` operations:

```scala
import cats.{Applicative, FlatMap, Functor, Monad, Monoid, Semigroup}
import scala.language.higherKinds

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
```

From the code above, one can see that the transformer imposes additional indirection and heap usage for every usage of
`pure`, `map` and `flatMap`.

The definition of the class introduces a wrapper `OptionT`. Compared to the original effect, `F[_]`, use of this wrapper
will involve 2 additional allocations when lifting **pure** values into the monad transformer context: 1 for `Option` 
and 1 for `OptionT` which will triple the memory consumption of the application. For example:

```scala
import cats.data.OptionT
import cats.effect.IO

def lookUpUserAge(id: String): IO[Option[Int]] = ...

for {
  age <- OptionT(lookUpUserAge("erlich"))   // 33
  multiplier <- OptionT.pure[IO, Int](2)    // 2
} yield age * multiplier
```

When we call `pure` above to lift the value 3 into the `OptionT` context, we're tripling the memory usage by having 2
additional allocations:

```scala
val alloc1 = Some(3)                  // additional allocation for Option
val alloc2 = IO.pure(alloc1)
val alloc3 = OptionT[IO, Int](alloc2) // additional allocation for OptionT
```

Furthermore, in the `map` function of OptionT, there are 3 additional method calls (`OptionT.apply`, `map`, `map`) and
an additional 2 allocations (OptionT, _), one for the OptionT wrapper and one for the anonymous function. The story for
the `flatMap` operation is similar.

<h3>Complexity</h3>

In our previous on monad transformers, we also saw how things started to get more complex as we started to stack more
monads together:

```scala
type IOEither[A] = EitherT[IO, String, A]       // equivalent to `IO[Either[String, A]]`

type IOEitherOption[A] = OptionT[IOEither, A]   // equivalent to `IO[Either[String, Option[A]]]`

@ 3.pure[IOEitherOption]
res19: IOEitherOption[Int] = OptionT(EitherT(<function1>))
```

In practice, you often need to combine several behaviors together which means composing 2 or more monad transformers.

As another example, let's say that we want to use the `State` monad to keep track of the state of our program along with
the `Writer` monad to keep a log of all the different values the state was in. Out of the box, Cats provides a monad
transformer called `IndexedReaderWriterStateT` which combines the `Reader`, `Writer`, and `State` monads together. Below
is a "simplified" version of this monad transformer that only provides the `Writer` and `State` functionality that we're
looking for:

```scala
import cats.{Applicative, FlatMap, Functor, Monad, Monoid, Semigroup}
import cats.implicits._

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
```

Immediately, you will be able to see the amount of complexity in the `map` and `flatMap` methods of the monad
transformer both in terms of the number of nested function calls and new object allocations. The reason for this
complexity is that the monad transformer has to combine the behaviors of both the `State` and the `Writer` monads 
together in order to ensure that their behavior remains the same even in combination. For the `State` monad this means
being able to "thread" a starting state `S` through what we can think of as multiple functions of type `S => (S, A)` as
it gets repeatedly updated throughout the program's execution. Similarly for the `Writer` monad, this means being able
to append to the current log `L` while threading the log through the program's execution without losing any values that
were previously there. Coming up with this combined logic can be very complex and is hard to reason about as seen above
in the `map` and `flatMap` methods.

This complexity doesn't even begin to account for the amount of heap memory an application will require when using this
monad transformer not to mention the amount of churn that the constant wrapping and un-wrapping will create.
Consequently this can create more frequent, and potentially longer-lasting, garbage collection pauses in the JVM which
is something to keep in mind if your application requires a high level of performance.

Using this monad transformer, we can write a "simple" program that makes use of it. Here, we will use our own case class
`AppState` to represent our state and we're going to parameterize our custom `WriterStateT` monad transformer on
`Vector[AppState]` for our log, which will keep track of the different values that our state was in:

```scala
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
```

Then we execute the program using the `run` method and supply it with an initial state:

```scala
import cats.effect.IO

@ program[IO].run(AppState(3)).unsafeRunSync()
res6: (Vector[AppState], AppState, AppState) = (Vector(AppState(3), AppState(9), AppState(11)), AppState(11), AppState(11))
```

The return value of our program includes a log of all of our state values along with the latest result value contained
in the `State` along with the latest value contained within the `Writer`.

<h2>MTL</h2>

In Haskell the Monad Transformer Library (MTL) package, as its name suggests, used to provide monad transformer types.
For some time now, however, these monad transformers have resided in the `transformers` package. And so it is partly by
historical accident and partly by an interesting choice of vocabulary that the name "MTL" is now used to refer to a
library of type classes. These type classes abstract over many design patterns that involve different types of 
"effects".

MTL was inspired by the 1995 paper "Functional Programming with Overloading and Higher-Order Polymorphism" by Mark P. 
Jones. In it, he explores the practical applications of the Hindley/Milner type system as well as a range of extensions
that provide the system with more flexibility while still retaining the characteristics that made it so popular. In
particular, Jones examines the use of higher-order polymorphism along with type and constructor class overloading in
order to promote modularity and reusability.

In Scala, these MTL type classes are encoded using what is known as a final tagless style.

<h3>Revisiting monad composition</h3>

Let's see how we can take our `WriterStateT` monad transformer from earlier and rewrite it using this MTL approach. The
`WriterStateT` transformer models 2 main effects namely, **state** for keeping track of a current value and **writer**
for logging the history of changes to the state.

For state management, rather than using the `StateT[F, S, A]` monad transformer which adds state management to some base
monad `F[_]`, we will instead create our own `MonadState` type class to capture the state management functionality we
need:

```scala
import scala.language.higherKinds
import cats.Monad

trait MonadState[F[_], S] {
  val monad: Monad[F]

  def get: F[S]

  def set(s: S): F[Unit]
}
```

Similarly for logging history, instead of using the `WriterT[F[_], L, V]` monad transformer, we will create another type
class, `MonadWriter`, to capture this functionality:

```scala
import scala.language.higherKinds
import cats.Monad

trait MonadWriter[F[_], L] {
  val monad: Monad[F]

  def tell(l: L): F[Unit]

  def logs: F[L]
}
```

With the 2 effect type classes that we defined above, we can create a program that uses them. Our program will be
parameterized on `F[_]` which represents some effect "context" that it will run inside of. Furthermore, the program will
rely on instances of `MonadState` and `MonadWriter`. These instances will be parameterized on `AppState` and
`Vector[AppState]` just like before to represent our state and to keep track of the changes to the state respectively.
The program will then return both the log of values the state was in as well as the final value of the state:

```scala
import scala.language.higherKinds
import cats.Monad

def program[F[_]](S: MonadState[F, AppState],
                  W: MonadWriter[F, Vector[AppState]]): F[(Vector[AppState], AppState)] = {
  implicit val monad: Monad[F] = S.monad

  for {
    state <- S.get

    _ <- W.tell(Vector(state))

    x <- monad.pure(AppState(state.value * 3)) // some "computation" that uses the program's state
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
```

In order to create `MonadState` instances, we will define the following method that parameterizes `MonadState` on Cats
Effect's `IO`. It's possible to use the `StateT` monad transformer internally to manage the state of our `MonadState`
type class but instead, we will opt for using `Ref` (also from cats-effect) for simplicity:

```scala
import cats.effect.concurrent.Ref
import cats.effect.IO
import cats.Monad

def createMonadState[S](initial: S): IO[MonadState[IO, S]] = {
  for {
    state <- Ref.of[IO, S](initial)   // IO[Ref[IO, S]]
  } yield new MonadState[IO, S] {
    val monad: Monad[IO] = Monad[IO]

    def get: IO[S] = state.get

    def set(s: S): IO[Unit] = state.set(s)
  }
}
```

We will also define a method to create instances of `MonadWriter` that is parameterized on `IO` and uses `Ref`. This
method will need a `Monoid[L]` which will be used to create empty instances of our log `L` as well as to combine
multiple values of `L` together:

```scala
import cats.effect.concurrent.Ref
import cats.effect.IO
import cats.Monoid
import cats.Monad
import cats.implicits._

def createMonadWriter[L](implicit M: Monoid[L]): IO[MonadWriter[IO, L]] = {
  for {
    writer <- Ref.of[IO, L](M.empty)
  } yield new MonadWriter[IO, L] {
    val monad: Monad[IO] = Monad[IO]

    def tell(l: L): IO[Unit] = writer.update(_.combine(l))

    def logs: IO[L] = writer.get
  }
}
```

Once we have all of these components in place, we can assemble them together and execute the program:

```scala
def main(): IO[(Vector[AppState], AppState)] = {
  for {
    monadState <- createMonadState(AppState(3))           // initial state
    monadWriter <- createMonadWriter[Vector[AppState]]      
    result <- program(monadState, monadWriter)
  } yield result
}

@ main().unsafeRunSync()
res22: (Vector[AppState], AppState) = (Vector(AppState(3), AppState(9), AppState(11)), AppState(11))
```

Here we can see some big differences between the MTL-style of composing effects and the monad transformer approach.
Perhaps the most notable distinction is the reduced complexity in the MTL-style over monad transformers.

With MTL, we can specify type classes for the behaviors that we are interested in separately, and then compose them
together within our program. This is because with MTL, the context that the different operations of our program run
within is the `F` monad which already provides the `map` and `flatMap` methods needed to compose things together.

With monad transformers, on the other hand, we are forced to combine the behaviors of different monads together within a
new, custom monad transformer "wrapper". In this wrapper, we define custom `map` and `flatMap` methods that we use for
composing multiple instances of our wrapper together. These methods must then ensure that the original behaviors of the
monads that are being composed remain the same as when they are used separately which is no easy task.

Another benefit of the MTL-style is the added re-usability that it provides. By defining small and focused type classes
for the behaviors we're looking for, it becomes easy to re-use them. If some part of an application only needs state
management, for example, then it doesn't make sense to also include `Writer` related functionality. Unfortunately, with
monad transformers, you're forced to combine behaviors together in a tightly coupled way which means that you have to
bring along the entire functionality of the transformer even if you only use a small part of it.

Furthermore, with MTL-style, we are able to customize the type classes by making them as simple or as complex as we need
them to be. And we do this by including only the behavior that we're interested. This helps us avoid bloating our
program with functionality we don't need. In addition, we get the opportunity to re-define behaviors in more efficient
ways from how they're normally defined.

Using the above example, let's consider the `State` monad `State[S, A]` which is commonly represented as a function of
type `S => (S, A)`. If we only care about the "state" portion of the `State` monad, we can define an MTL type class, 
`MonadState[F[_], S]`, like we did above that only captures the state behavior and does away with anything related to
the result.

Defining our behaviors this way helps to narrow the scope of the type classes that model them. This opens the door for
better implementations that might not have been otherwise possible if we needed to support the complete functionality of
the monad.

For a more comprehensive comparison of monad transformers and MTL, take a look at:
https://kleisli.io/monad-transformers-vs-mtl/

<h4 align="right">
    <a href="../_5_functional_programming_applicatives/lesson5_5_traverse.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a>
</h4>