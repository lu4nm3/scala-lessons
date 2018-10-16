<h4 align="right">
    <a href="lesson4_4_eval.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
    <a href="lesson4_6_reader.md">Next →</a>
</h4>

<h1>Writer</h1>

Writer is a monad that lets us carry a "log" along with a computation. We can use it to record messages, errors, or
additional data about a computation, and extract the log along with the final result.

A `Writer[W, A]` carries 2 values: 

  - a log of type `W` and
  - a result of type `A`.

For example, here, the log is of type `Vector[String]` and the result type is `Int`:

```scala
import cats.data.Writer

Writer(Vector("step 1", "step 2"), 42)
```

Typically, the log type `W` should also have a `Monoid[W]` in scope since some of the methods in `Writer` need for 
certain functionality.

If we only have a result, we can use the `pure` operation:

```scala
import cats.implicits._ // for Monoid[Vector] and ".pure" syntax and 

type Logged[A] = Writer[Vector[String], A]

scala> 42.pure[Logged]
res0: Logged[Int] = WriterT((Vector(),42))
```

If we have a log and no result, we can create a `Writer` using the `tell` operation:

```scala
scala> Vector("step 1", "step 2").tell
res1: cats.data.Writer[scala.collection.immutable.Vector[String],Unit] = WriterT((Vector(step 1, step 2),()))
```

We can extract the result and log from a Writer using the following operations:

```scala
scala> val w = Writer(Vector("step 1", "step 2"), 42)

scala> w.value
res0: cats.Id[Int] = 42

scala> w.written
res1: cats.Id[scala.collection.immutable.Vector[String]] = Vector(step 1, step 2)

scala> w.run
res2: cats.Id[(scala.collection.immutable.Vector[String], Int)] = (Vector(step 1, step 2),42)
```

<h3><code>map</code> & <code>flatMap</code></h3>

Just like any `Monad`, `Writer` supports both `map` and `flatMap`. One thing to note about `Writer` is that its log is 
preserved even when we chain multiple operations together:

```scala
import cats.data.Writer
import cats.implicits._

type Logged[A] = Writer[Vector[String], A]

val result = for {
  _ <- Vector("computing a...").tell
  a <- 3.pure[Logged]
  _ <- Vector("computed a!").tell

  _ <- Vector("preparing to compute b...").tell
  b <- 2.writer(Vector("computing b..."))
  _ <- Vector("computed b!").tell
} yield a * b
```

And if we run it, you will see that besides the final result, we also get back everything that was written to the log:

```scala
scala> result.run
res0: cats.Id[(scala.collection.immutable.Vector[String], Int)] = (Vector(computing a..., computed a!, preparing to compute b..., computing b..., computed b!),6)
```

<h4 align="right">
    <a href="lesson4_4_eval.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
    <a href="lesson4_6_reader.md">Next →</a>
</h4>