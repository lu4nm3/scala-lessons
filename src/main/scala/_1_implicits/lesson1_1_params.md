<h4 align="right">
    <a href="../_0_basics/lesson0.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson1_2_conversions.md">Next →</a>
</h4>

<h1>Implicit Values & Parameters</h1>

You can use the `implicit` keyword inside a parameter list to make specifying the parameters optional:

```scala
case class Context()

def foo(implicit ctx: Context): Unit = ()
```

We can specify the context as a normal argument

```scala
foo(Context())
```

Or we can omit the implicit argument to have the compiler search or implicit values of the correct type that it can use 
to fill the missing arg:

```scala
implicit val ctx: Context = Context()

foo // compiler looks for an implicit Context
```

Note that the `implicit` keyword applies to the whole parameter list, not just an individual parameter:

```scala
case class Config()

def bar(implicit ctx: Context, conf: Config): Unit = ()
```

Any implicit value must always be declared inside an object, class, or trait.

We can create an implicit value by tagging any `val`, `var`, `object`, or 0-argument `def` with the `implicit` keyword 
which makes it a potential candidate as an implicit parameter.

```scala
implicit val x1: String = ""
implicit var x2: String = ""
implicit object x3 {}
implicit def x4: String = ""
```

<h3>What are implicits good for?</h3>

As an example, here is some code where it's tedious and verbose to manually pass in the `Config` argument:


```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def initDb1(conf: Config): Future[Unit] = ???
def initDb2(conf: Config): Future[Unit] = ???
def initDb3(conf: Config): Future[Unit] = ???

def init(conf: Config): Future[Unit] = {
  for {
    _ <- initDb1(conf) // manually pass in Config
    _ <- initDb2(conf)
    _ <- initDb3(conf)
  } yield ()
}
```

We can instead pass `conf` implicitly to every place that needs it:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def initDb1(implicit conf: Config): Future[Unit] = ???
def initDb2(implicit conf: Config): Future[Unit] = ???
def initDb3(implicit conf: Config): Future[Unit] = ???

implicit val conf: Config = Config()

def init(implicit conf: Config): Future[Unit] = {
  for {
    _ <- initDb1
    _ <- initDb2
    _ <- initDb3
  } yield ()
}
```

What happens when multiple implicit values of the same type are in scope?

```scala
implicit val x: Int = 1
implicit val y: Int = 2
implicit val z: Int = 3

def useInt(implicit i: Int): Int = i
```

If we call `useInt` we get a compilation error:

```scala
@ useInt
<console>:16: error: ambiguous implicit values:
 both value x of type => Int
 and value y of type => Int
 match expected type Int
       useInt
       ^
```

<h4 align="right">
    <a href="../_0_basics/lesson0.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson1_2_conversions.md">Next →</a>
</h4>
