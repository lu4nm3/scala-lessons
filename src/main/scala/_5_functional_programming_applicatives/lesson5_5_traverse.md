<h4 align="right">
    <a href="lesson5_4_foldable.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_6_functional_programming_patterns/lesson6.md">Next →</a>
</h4>

<h1>Traverse</h1>

`foldLeft` and `foldRight` are flexible iteration methods but they require us to do a lot of work to define accumulator
and combinator functions. The `Traverse` type class is a higher level tool that leverages `Applicative`s to provide a
more convenient pattern for iteration.

Let's look at an example where we need to asynchronously get the server uptimes for a list of hosts. First let's define
a helper function that we will use to obtain the uptime for a single host:

```scala
import monix.eval.Task

def getUptime(hostname: String): Task[Int] = {
  Task.now(hostname.length * 60)
}
```

Next, let's define a function that takes a list of hosts and asynchronously returns a list of their uptimes:

```scala
def getAllUptimes(hostnames: List[String]): Task[List[Int]] = {
  hostnames.foldLeft(Task.now(List.empty[Int])) { (taskAccum: Task[List[Int]], host: String) =>
    for {
      accum <- taskAccum          // first unwrap the accumulator from the IO it is in
      uptime <- getUptime(host)   // then unwrap the IO containing the next uptime
    } yield accum :+ uptime       // finally add the uptime to the accumulator
  }                               // this will yield a new IO[List[Int]] containing the added value
}
```

Finally in order to run it, let's define a list of hosts and call our function:

```scala
val hostnames = List("one.example.com", "two.example.com", "three.example.com")

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global

@ getAllUptimes(hostnames).runSyncUnsafe(1.second)
res0: List[Int] = List(900, 900, 1020)
```

We can simplify our code by using `IO.traverse`:

```scala
def getAllUptimes(hostnames: List[String]): Task[List[Int]] = {
  Task.traverse(hostnames)(getUptime)
}
```

Which gives us the same result as before but with less boilerplate:

```scala
@ getAllUptimes(hostnames).runSyncUnsafe(1.second)
res20: List[Int] = List(900, 900, 1020)
```

The `traverse` method gives us a clean, high-level interface to do what we want, namely:
  
  1. start with a `List[A]`
  2. provide a function `A => Future[B]`
  3. end up with a `Future[List[B]]`

The Scala standard library also provides another method called `sequence`. Going off the example above, `sequence`
assumes we're starting with a `List[Task[B]]`:

```scala
def sequence[B](tasks: List[Task[B]]): Task[List[B]]
```

In this case we:

  1. start with a `List[Future[A]]`
  2. end up with a `Future[List[A]]`

<h3>Traverse</h3>

The Cats `Traverse` type class generalizes these patterns to work with any type of `Applicative`.

We can rewrite our `getAllUptimes` function from earlier using `Traverse`

```scala
import cats.Traverse
import cats.implicits._

def getAllUptimes(hostnames: List[String]): Task[List[Int]] = {
  Traverse[List].traverse(hostnames)(getUptime)
}
```

If we had a `List` of `Task`s, we could alternatively use `sequence`:

```scala

def getAllUptimes(hostnames: List[String]): Task[List[Int]] = {
  val uptimes: List[Task[Int]] = hostname.map(getUptime)
  Traverse[List].sequence(uptimes)
}
```

`Traverse` abstracts and generalizes the `traverse` and `sequence` methods we know from `Task`. Using these methods we
can turn any `F[G[A]]` into a `G[F[A]]` for any `F` with an instance of `Traverse` and any `G` with an instance of
`Applicative`.

<h4 align="right">
    <a href="lesson5_4_foldable.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_6_functional_programming_patterns/lesson6.md">Next →</a>
</h4>