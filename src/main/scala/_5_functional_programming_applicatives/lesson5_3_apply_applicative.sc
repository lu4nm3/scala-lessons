// ****************************************************************
//                        Apply & Applicative
// ****************************************************************
import cats.{Functor, Monad, Semigroupal}
import scala.language.higherKinds





// Cats models applicatives using 2 type classes. The first,
// `Apply` extends `Semigroupal` and `Functor` and adds an ap
// method that applies a parameter to a function within a context:

trait Apply[F[_]] extends Semigroupal[F] with Functor[F] {

  // applies parameter fa to function ff within context F[_]
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

  // defined in terms of `ap` and `map`
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] = {
    ap[B, (A, B)](map(fa)(a => (b: B) => (a, b)))(fb)
  }
}




// The second, `Applicative`, extends Apply and adds the `pure`
// method introduced in lesson 4. In this sense, `Applicative` is
// to `Apply` as `Monoid` is to `Semigroup`:

trait Applicative[F[_]] extends Apply[F] {
  def pure[A](a: A): F[A]
}




// The main point here is that there is a tight relationship
// between `product`, `ap`, and `map` that allows any one of them
// to be defined in term of the other 2. This extends further to
// the hierarchy of applicatives and monads:

//   -------------           ---------
//  | Semigroupal |         | Functor |
//  | ----------- |         | ------- |
//  |   product   |         |   map   |
//   -------------           ---------
//                \         /
//                  -------
//                 | Apply |
//                 | ----- |
//                 |   ap  |
//                  -------
//                /          \
//   -------------             ---------
//  | Applicative |           | FlatMap |
//  | ----------- |           | ------- |
//  |     pure    |           | flatMap |
//   -------------             ---------
//                 \         /
//                   -------
//                  | Monad |
//                   -------
//
// - every Monad is an Applicative
// - every Applicative is a Semigroupal
// - etc




// Let's say we have 2 hypothetical data types:

// Implements `pure` and `flatMap` and inherits standard
// definitions of `product`, `map`, and `ap`
trait Foo[F[_]] extends Monad[F]

// Implements `pure` and `ap` and inherits standard definitions of
// `product` and `map`
trait Bar[F[_]] extends Applicative[F]

// What can we say about these 2 data types without knowing more
// about their implementation?

// We know strictly more about `Foo` than `Bar`. Since `Monad` is a
// subtype of `Applicative`, we can guarantee properties  of `Foo`
// (namely `flatMap`) that we can't guarantee with `Bar`.

// We also know that `Bar` has fewer laws to obey so it can
// implement behaviors that `Foo` can't.

// The more constraints we place on a data type, the more
// guarantees we have about its behavior, but the fewer behaviors
// we can model. While monads impose a strict sequencing on the
// computations they model, applicatives and semigroupals have no
// such restriction and we can use them to represent classes of
// parallel or independent computations that monads cannot


