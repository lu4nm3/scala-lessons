<h4 align="right">
    <a href="../_4_functional_programming_monads/lesson4.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson5_2_validated.md">Next →</a>
</h4>

<h1>Semigroupal</h1>

In the past few lessons we have seen how monads allow us to sequence operations using `map` and `flatMap`. While monads 
are useful abstractions, there are cetain types of program flows that they can't represent.

One example is validation. When we validate a form, we want to return all of the errors to the user, not stop on the
first error we encounter. If we model this using `Either`, we fail fast and lose errors. In the example below, the code
fails on the first call to `parseInt` and doesn't go any further:

```scala
import scala.util.{Failure, Success, Try}

def parseInt(s: String): Either[String, Int] = {
  Try(s.toInt) match {
    case Success(i) => Right(i)
    case Failure(_) => Left(s"Error parsing $s")
  }
}

for {
  a <- parseInt("a") // fails here!
  b <- parseInt("b")
  c <- parseInt("c")
} yield a + b + c
```

We need a weaker construct that doesn't guarantee sequencing to achieve the result we want.

`Semigroupal` is a type class that allows us to combine contexts. If we have 2 objects of type `F[A]` and `F[B]`, a
`Semigroupal[F]` allows us to combine them to form an `F[(A, B)]`.

```scala
import scala.language.higherKinds

trait Semigroupal[F[_]] {
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}
```

Here, `fa` and `fb` are independent of one another. They can be computed in either order before being passed to 
`product`. Just like `Semigroup` allows us to join values, `Semigroupal` allows us to join contexts.

Here are some examples where we use an instance of `Semigroupal[Option]` to combine `Option`s together:

```scala
import cats.Semigroupal
import cats.implicits._

@ Semigroupal[Option].product(Some(123), Some("abc"))
res0: Option[(Int, String)] = Some((123,abc))

@ Semigroupal[Option].product(None, Some("abc"))
res1: Option[(Nothing, String)] = None

@ Semigroupal[Option].product(Some(123), None)
res2: Option[(Int, Nothing)] = None
```

The `product` method of `Semigroupal` only supports combining 2 contexts together. If we want to combine more than 2
contexts, we can use the `tupleX` method:

```scala
@ Semigroupal.tuple3(Option(1), Option(2), Option(3))
res3: Option[(Int, Int, Int)] = Some((1,2,3))
```

We can also specify the result of combining contexts together using one of several `mapX` methods that are available to
us:

```scala
@ Semigroupal.map3(Option(1), Option(2), Option(3))((n1, n2, n3) => n1 + n2 + n3)
res4: Option[Int] = Some(6)
```

You can also use the `tupled` and `mapN` syntax methods that are implicitly available to tuples:

```scala
import cats.implicits._

@ (Option(1), Option(2), Option(3)).tupled
res5: Option[(Int, Int, Int)] = Some((1,2,3))

@ (Option(1), Option(2), Option(3)).mapN((n1, n2, n3) => n1 + n2 + n3)
res6: Option[Int] = Some(6)
```

<h4 align="right">
    <a href="../_4_functional_programming_monads/lesson4.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson5_2_validated.md">Next →</a>
</h4>