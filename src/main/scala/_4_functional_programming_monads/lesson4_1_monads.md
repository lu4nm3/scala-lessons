<h4 align="right">
    <a href="../_3_functional_programming_basics/lesson3_4_higher_kinded_types.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson4_2_id.md">Next →</a>
</h4>

<h1>Monads</h1>

Informally, a monad is anything with a constructor and a `flatMap` operation. We can say that a monad is a mechanism for
sequencing computations.

In a previous lesson, we said something similar about the `map` operation of `Functor`. The difference in this case, is
that `map` is limited when dealing with higher order contexts. That is to say, `map` can only deal with contexts once at 
the beginning of a computation sequence.

The `flatMap` operation of a Monad, on the other hand, is able to take care of this by allowing us to map from a value 
to a context before flattening the contexts together (hence the name "flatMap").

<h2><code>map</code> & <code>flatten</code></h2>

As an example, let's take a look at a couple methods, where each can fail by returning `None`:

```scala
import scala.util.Try

def parseInt(str: String): Option[Int] = {
  Try(str.toInt).toOption
}

def divide(a: Int, b: Int): Option[Int] = {
  if (b == 0) None
  else Some(a / b)
}
```

Say we wanted to use these methods to parse 2 integers and then divide them by each other.

```scala
@ val num1 = parseInt("6")
num0: Option[Int] = Some(6)

@ val num2 = parseInt("2")
num1: Option[Int] = Some(2)

@ val div = divide(num1, num2)
<console>:21: error: type mismatch;
 found   : Option[Int]
 required: Int
       val div = divide(num1, num2)
                        ^
<console>:21: error: type mismatch;
 found   : Option[Int]
 required: Int
       val div = divide(num1, num2)
                              ^
```

We run into an issue here because our `divide` function takes `Int`, not `Option[Int]`. We need to find some way to work
with these integers within their `Option` context. 

Earlier we learned about `Functor`s and how they let you `map` values that exist inside a context. Let's see if we can 
use `map` to solve our issue:

```scala
@ parseInt("6").map { num1 =>      
     |   parseInt("2").map { num2 =>    
     |     divide(num1, num2)           
     |   }
     | }
res2: Option[Option[Option[Int]]] = Some(Some(Some(3)))
```

Well, that sort of worked since we were able to work within the `Option` context to call our `divide` function. The only 
problem now is that the result of our operation is nested deep within a bunch of `Option`s:

```scala
parseInt("6").map { num1 =>       // Option[Option[Option[Int]]]
  parseInt("2").map { num2 =>     // Option[Option[Int]]
    divide(num1, num2)            // Option[Int]
  }
}
```

Instead of `Option[Option[Option[Int]]]` we would like to get an `Option[Int]`. We need some way to flatten things. 
Here, we can use the `flatten` method from `Option` after every call to `map` in order to flatten our contexts:

```scala
@ parseInt("6").map { num1 =>
     |   parseInt("2").map { num2 =>
     |     divide(num1, num2)
     |   }.flatten
     | }.flatten
res3: Option[Int] = Some(3)
```

Now if we examine our types at each step of our nested `map`s, we can see that the context never grows beyond a single 
level:

```scala
parseInt("6").map { num1 =>       // 4. Simliarly, the second call to map produces an Option[Option[Int]].
  parseInt("2").map { num2 =>     // 2. The first call to map produces a nested value: Option[Option[Int]].
    divide(num1, num2)            // 1. Our divide method produces an Option[Int].
  }.flatten                       // 3. Before finishing, we flatten our Option[Option[Int]] to get an Option[Int].
}.flatten                         // 5. And just like before, we will flatten Option[Option[Int]] into Option[Int].
```

<h2><code>flatMap</code></h2>

The use of `map` and `flatten` one after the other can be replaced with the `flatMap` operation:

```scala
@ parseInt("6").flatMap { num1 =>
     |   parseInt("2").flatMap { num2 =>
     |     divide(num1, num2)
     |   }
     | }
res23: Option[Int] = Some(3)
```

We can use for-comprehensions to clean up the syntax a bit and make things easier to read. For-comprehensions are just a 
form of syntactic sugar for a series of calls to `map` and `flatMap` like in our example above:

```scala
for {
  num1 <- parseInt("6")
  num2 <- parseInt("2")
  result <- divide(num1, num2)
} yield result
```

<h2>The Monad</h2>

At every step of a monadic computation, `flatMap` will choose whether to call its function parameter (ie. `A => F[B]`) 
or whether to **fail-fast** and terminate the computation in the case of "failure". 

<h3>Success</h3>

If everything is going smoothly and `flatMap` decides to continue the computation, the 
function parameter of `flatMap`, `A => F[B]`, will generate the next computation in the sequence, `F[B]`, which will 
allow us to keep chaining calls to `flatMap` like we saw in the previous section.

<h3>Failure</h3>

In the case of "failure", the monadic computation will short-circuit and not go any further. Now, when we say "failure",
we don't necessarily mean that an exception was thrown or that the program now has to terminate. We simply mean that the
monadic computation did not follow the successful path of operation and must now return a type to represent this 
failure. This type is typically a sub-class from the Monad in question (eg. `None` for `Option`, `Left` for `Either`,
etc).

Using the example from the previous section, let's suppose that parsing our second integer fails:

```scala
@ for {
     |   num1 <- parseInt("6")
     |   num2 <- parseInt("a") // a is not an integer!!!
     |   result <- divide(num1, num2)
     | } yield result
res26: Option[Int] = None
```

Here we see that nothing after the point where the failure occurs gets executed (ie. the call to `divide`) . Instead, 
the computation fails-fast and returns `None`.

<h3>Definition</h3>

Monadic behavior is formally captured in 2 operations:

  - `pure` of type `A => F[A]`
  - `flatMap` of type `(F[A], A => F[A]) => F[B]`
  
`pure` provides a way of creating a new monadic context from a plain value. `flatMap` provides the sequencing step 
extracting the value from a context and generating the next context in the sequence.

In the Cats library, `Monad` is defined as a type class that looks like the following:

```scala
trait Monad[F[_]] {
  def pure[A](value: A): F[A]

  def flatMap[A, B](value: F[A])(f: A => F[B]): F[B]
}
```

As an example, we can implement the `Monad` type class for `Option`:

```scala
implicit val optionMonad = new Monad[Option] {
  def pure[A](value: A): Option[A] = Some(value)
  
  def flatMap[A, B](value: Option[A])(f: A => Option[B]): Option[B] = {
    value match {
      case Some(v) => f(v)
      case None => None
    }
  }
}
``` 

<h3>Every Monad is also a Functor</h3>

Every monad is also a functor. This is because we can define `map` in terms of the `flatMap` 
and `pure` operations:

```scala
trait Monad[F[_]] {
  def pure[A](value: A): F[A]

  def flatMap[A, B](value: F[A])(f: A => F[B]): F[B]

  def map[A, B](value: F[A])(f: A => B): F[B] = {
    flatMap(value)(a => pure(f(a)))
  }
}
```

<h3>Laws</h3>

There are 3 laws that the `pure` and `flatMap` operations of a monad must obey:

<h4>Left identity</h4>

Calling `pure` and transforming the result with `f` is the same as calling `f`.

```scala
pure(a).flatMap(f) == f(a)
```

For example:

```scala
import cats.Monad
import cats.implicits._

val f: Int => Option[Int] = x => Some(x * 2)

Monad[Option].pure(3).flatMap(f) == f(3)
```

<h4>Right identity</h4>

Passing `pure` to `flatMap` is the same as doing nothing

```scala
m.flatMap(pure) == m
```

For example:

```scala
import cats.Monad
import cats.implicits._

val m = Some(3)

m.flatMap(Monad[Option].pure) == m
```

<h4>Associativity</h4>

`flatMap`ping over 2 functions `f` and `g` is the same as `flatMap`ping over `f` and then `flatMap`ping over `g`

```scala
m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
```

For example:

```scala
val f: Int => Option[Int] = x => Some(x * 2)
val g: Int => Option[Int] = x => Some(x + 1)

val m = Some(3)

m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
```

<h4 align="right">
    <a href="../_3_functional_programming_basics/lesson3_4_higher_kinded_types.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson4_2_id.md">Next →</a>
</h4>