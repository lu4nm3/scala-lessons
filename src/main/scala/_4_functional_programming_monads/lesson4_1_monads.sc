// ****************************************************************
//                            Monads
// ****************************************************************
import cats.Monad
import cats.instances.option._
import scala.language.higherKinds
import scala.util.Try




// Informally, a monad is anything with a constructor and a
// `flatMap` operation. We can say that a monad is a mechanism for
// sequencing computations.
//
// In the previous lesson, we said something similar about the
// `map` operation of functors. The difference in this case, is
// that `map` is limited when dealing with higher order contexts.
// That is to say, `map` can only deal with contexts once at the
// beginning of a computation sequence.
//
// The `flatMap` operation of a monad, on the other hand, is able
// to take care of this context allowing us to `flatMap` over and
// over again.




def parseInt(str: String): Option[Int] = {
  Try(str.toInt).toOption
}

def divide(a: Int, b: Int): Option[Int] = {
  if (b == 0) None
  else Some(a / b)
}

// Each of these methods may fail by returning `None`. The
// `flatMap` method allows us to ignore this when we sequence
// operations:

parseInt("6").flatMap { num1 =>
  parseInt("2").flatMap { num2 =>
    divide(num1, num2)
  }
}

// At each step, `flatMap` chooses whether to call our function,
// and if so, our function generates the next computation in the
// sequence. The result of of a computation is an `Option`,
// allowing us to call `flatMap` again and so the sequence
// continues. This results in fail-fast error handling if one of
// the computations fail.

// Since every monad is also a functor, we have both `flatMap` and
// `map` this means we can use for comprehensions to clarify
// sequencing behavior:

for {
  num1 <- parseInt("6")
  num2 <- parseInt("2")
  result <- divide(num1, num2)
} yield result




// Monadic behavior is formally captured in 2 operations:
//
// `pure` of type A => F[A]
// `flatMap` of type (F[A], A => F[A]) => F[B]
//
// `pure` provides a way of creating a new monadic context from a
// plain value. `flatMap` provides the sequencing step extracting
// the value from a context and generating the next context in the
// sequence.

trait Monad[F[_]] {
  def pure[A](value: A): F[A]

  def flatMap[A, B](value: F[A])(f: A => F[B]): F[B]
}

// There are 3 laws that the `pure` and `flatMap` operations of a
// monad must obey:

// Left identity:
//
// Calling `pure` and transforming the result with `f` is the same
// as calling `f`.
//
// pure(a).flatMap(f) == f(a)

val f: Int => Option[Int] = x => Some(x * 2)
Monad[Option].pure(3).flatMap(f) == f(3)

// Right identity:
//
// Passing `pure` to `flatMap` is the same as doing nothing
//
// m.flatMap(pure) == m

val m = Some(3)
m.flatMap(Monad[Option].pure) == m

// Associativity:
//
// `flatMap`ping over 2 functions `f` and `g` is the same as
// `flatMap`ping over `f` and then `flatMap`ping over `g`
//
// m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))

val g: Int => Option[Int] = x => Some(x + 1)
m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))




// We mentioned earlier that every monad is also a functor. This is
// because we can define `map` in terms of the `flatMap` and `pure`
// operations:

trait M0nad[F[_]] {
  def pure[A](value: A): F[A]

  def flatMap[A, B](value: F[A])(f: A => F[B]): F[B]

  def map[A, B](value: F[A])(f: A => B): F[B] = {
    flatMap(value)(a => pure(f(a)))
  }
}






