<h4 align="right">
    <a href="lesson4_6_reader.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson4_8_state.md">Next →</a>
</h4>

<h1>Kleisli</h1>

The Scala standard library already allows us to compose functions of type `A => B` together using the `compose` and
`andThen` operators ***ONLY IF*** the output type of one function matches the input type of the next function:

```scala
val foo: String => Int = s => s.toInt
val bar: Int => Int = i => i * 2
val baz: Int => Boolean = i => i % 2 == 0

@ baz compose bar compose foo    // baz(bar(foo(...)))
res0: String => Boolean

@ foo andThen bar andThen baz    // foo(...) -> bar(foo(...)) -> baz(bar(foo(...)))
res1: String => Boolean
```

Often times, however, we have to deal with functions that produce effects (ie. Option, Future, Task, etc) especially 
when dealing with things like uncertainty or asynchronous calls.

For example, if we modify our functions by wrapping the return type with Monix `Task`:

```scala
import monix.eval.Task

val foo: String => Task[Int] = s => Task.now(s.toInt)
val bar: Int => Task[Int] = i => Task.now(i * 2)
val baz: Int => Task[Boolean] = i => Task.now(i % 2 == 0)
```

We will not be able to compose them together like before since the output type of one function doesn't match the input 
type of the next:

```scala
@ foo andThen bar andThen baz
<console>:16: error: type mismatch;
 found   : Int => monix.eval.Task[Int]
 required: monix.eval.Task[Int] => ?
       foo andThen bar andThen baz
                   ^
```

There are a few things we can do here. First, we can try using the `flatMap` operation from `Task` to compose the 
functions:

```scala
@ (s: String) => foo(s) flatMap { v1 =>
    bar(v1) flatMap { v2 =>
      baz(v2)
    }
  }
res9: String => monix.eval.Task[Boolean] = $$Lambda$3816/2142304919@19b0d133
```

We can also use a for-comprehension to re-write the above:

```scala
(s: String) => {
  for {
    v1 <- foo(s)
    v2 <- bar(v1)
    result <- baz(v2)
  } yield result
}
```

<h3>Kleisli</h3>

The Kleisli monad is another way to compose these functions together in a much cleaner and succinct manner. 
`Kleisli[F[_], A, B]` can be thought of as a wrapper  for functions of type `A => F[B]` where `F[_]` is a type 
constructor.

Using our previous IO example, we can rewrite it using `Kleisli` as follows:

```scala
import cats.data.Kleisli

//val foo: String => Task[Int] = s => Task.now(s.toInt)
val foo: Kleisli[Task, String, Int] = Kleisli(s => Task.now(s.toInt))

//val bar: Int => Task[Int] = i => Task.now(i * 2)
val bar: Kleisli[Task, Int, Int] =  Kleisli(i => Task.now(i * 2))

// val baz: Int => Task[Boolean] = i => Task.now(i % 2 == 0)
val baz: Kleisli[Task, Int, Boolean] = Kleisli(i => Task.now(i % 2 == 0))
```

Now, thanks to `Kleisli`, we are able to combine them together just as if though they were regular functions using the
`andThen` keyword (which in this case is provided by `Kleisli`):

```scala
@ val combo = foo andThen bar andThen baz
combo: cats.data.Kleisli[monix.eval.Task,String,Boolean] = Kleisli(cats.data.Kleisli$$$Lambda$3729/1961003967@23d1dcb8)
```

We can call `run` to extract the function being wrapped by `Kleisli`:

```scala
@ combo.run
res11: String => monix.eval.Task[Boolean] = cats.data.Kleisli$$$Lambda$3729/1961003967@23d1dcb8
```

For even cleaner syntax, you can inline the lifting of the first function in the chain and then simply compose the rest 
of the `A => F[B]` functions directly without lifting them to a Kleisli by using Kleisli's `andThen` operator:

```scala
val foo: String => Task[Int] = s => Task.now(s.toInt)
val bar: Int => Task[Int] = i => Task.now(i * 2)
val baz: Int => Task[Boolean] = i => Task.now(i % 2 == 0)

@ Kleisli(foo) andThen bar andThen baz
res12: cats.data.Kleisli[monix.eval.Task,String,Boolean] = Kleisli(cats.data.Kleisli$$$Lambda$3729/1961003967@544d96de)
```

<h3>A Kleisli in disguise</h3>

One important thing to note is that a `Reader` is simply a more specialized form of `Kleisli` that abstracts the type 
constructor of the result type. In fact, `Reader` is actually defined as a type alias of `Kleisli` that uses `Id` for 
the effect type:

```scala
type Reader[I, O] = Kleisli[Id, I, O]
```

This means that we can use Kleisli to sequence operations that rely on some common input the same way we did with 
`Reader`:

```scala
case class Dog(name: String, favoriteFood: String)

val greetDog: Dog => Task[String] = dog => Task.now(s"Hello ${dog.name}")
val feedDog: Dog => Task[String] = dog => Task.now(s"Have a nice bowl of ${dog.favoriteFood}")

val greetAndFeed = for {
  greet <- Kleisli(greetDog)
  feed <- Kleisli(feedDog)
} yield s"$greet. $feed."
```

Just like before, we can use the `run` method to get back the function wrapped by `Kleisli`:

```scala
@ greetAndFeed.run
res13: Dog => monix.eval.Task[String] = cats.data.Kleisli$$$Lambda$3729/1961003967@6ab4b791
```

And to execute, we simply pass an input to the resulting function:

```scala
@ greetAndFeed.run(Dog("Comet", "chicken"))
res14: monix.eval.Task[String] = Task.FlatMap$1931862900
```

<h4 align="right">
    <a href="lesson4_6_reader.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson4_8_state.md">Next →</a>
</h4>
