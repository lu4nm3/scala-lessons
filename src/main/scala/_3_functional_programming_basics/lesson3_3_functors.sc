// ****************************************************************
//                            Functors
// ****************************************************************
import scala.language.higherKinds




// Functors are an abstraction that allow us to represent sequences
// of operations within a context. Informally, a functor is
// anything with a `map` operation.

List(1, 2, 3).map(n => n * 2) // List(2, 4, 6)

Some(2).map(n => n + 1) // Some(3)

// Since `map` leaves the structure of the context unchanged, we
// can call is repeatedly to sequence multiple computations on the
// contents of a data structure:

List(1, 2, 3)
  .map(n => n * 2)
  .map(n => n + 1) // List(3, 5, 7)




// Formally, a functor is a type F[A] with an operation `map` with
// type `(A => B) => F[B]`. Cats implements Functor as a type class
// so the `map` operation looks a bit different:

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

// As an example, we can define an instance of Functor `Option`

implicit val optionFunctor: Functor[Option] = new Functor[Option] {
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = {
    fa match {
      case Some(a) => Some(f(a))
      case None => None
    }
  }
}

object Functor {
  def apply[F[_]](implicit f: Functor[F]): Functor[F] = f
}

Functor[Option].map(Some(2))(_ * 2)



