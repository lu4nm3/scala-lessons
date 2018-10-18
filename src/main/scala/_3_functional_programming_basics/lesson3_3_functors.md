<h4 align="right">
    <a href="lesson3_2_semigroups.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson3_4_higher_kinded_types.md">Next →</a>
</h4>

<h1>Functors</h1>

Functors are an abstraction that allow us to represent sequences of operations within a context. Informally, a functor 
is anything with a `map` operation. 

For example, both `List` and `Option` could be considered to be Functors:

```scala
List(1, 2, 3).map(n => n * 2) // List(2, 4, 6)
```

```scala
Some(2).map(n => n + 1) // Some(3)
```

Since `map` leaves the structure of the context unchanged, we can call it repeatedly to sequence multiple computations 
on the contents of a data structure:

```scala
List(2, 5, 8).map(n => n + 1) // List(3, 6, 9)
             .map(n => n * 2) // List(6, 12, 18)
             .map(n => n / 3) // List(2, 4, 6)
```

Formally, a functor is a some `F[A]` with an operation `map` that is of type `(A => B) => F[B]`. The Cats library 
implements Functor as a type class so the `map` operation looks a bit different:

```scala
import scala.language.higherKinds

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
```

As an example, let's define an instance of Functor `Option`

```scala
implicit val optionFunctor: Functor[Option] = new Functor[Option] {
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = {
    fa match {
      case Some(a) => Some(f(a))
      case None => None
    }
  }
}
```

Then we can create a summoner method for our `Functor` type class:

```scala
object Functor {
  def apply[F[_]](implicit functor: Functor[F]): Functor[F] = functor
}
```

And finally, we can use it to map the value of an `Option`:

```scala
@ Functor[Option].map(Some(2))(_ * 2)
res0: Option[Int] = Some(4)

@ Functor[Option].map(None)(_ * 2)
res1: Option[Int] = None
```

<h4 align="right">
    <a href="lesson3_2_semigroups.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson3_4_higher_kinded_types.md">Next →</a>
</h4>