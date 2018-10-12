<h1>Eval</h1>

Eval is a monad that allows us to abstract over different models of evaluation (ie. eager, lazy) as well as whether or 
not a result is memoized. First let's look at how Scala natively supports these models.

<h2>Scala</h2>

<h3><code>val</code></h3>

`val`s are both eager and momoized. 

Notice how the following print statement is only executed once regardless of how many times `x` is accessed:

```scala
scala> val x = {
     |   println("computing x...")
     |   3
     | }
computing x...
x: Int = 3

scala> x
res0: Int = 3

scala> x
res1: Int = 3
```

<h3><code>def</code></h3>

`def`s are lazy and not momoized. 

Notice how the print statement is re-run on every access of 
`y`:

```scala
scala> def y = {
     |   println("computing y...")
     |   3
     | }
y: Int

scala> y
computing y...
res2: Int = 3

scala> y
computing y...
res3: Int = 3
```

<h3><code>lazy val</code></h3>

`lazy val`s are lazy and memoized.
 
Notice that nothing gets printed when `z` is defined and that once it is accessed, only the first access will execute 
the print and memoize the resulting value:

```scala
scala> lazy val z = {
     |   println("computing z...")
     |   3
     | }
z: Int = <lazy>

scala> z
computing z...
res4: Int = 3

scala> z
res5: Int = 3
```

<h2>Cats</h2>

Cats' `Eval` data type has 3 subtypes: `Now`, `Later`, and `Always`.

| Scala      | Cats     | Properties         |
| ---------- | -------- | ------------------ |
| `val`      | `Now`    | eager, memoized    |
| `lazy val` | `Later`  | lazy, memoized     |
| `def`      | `Always` | lazy, not memoized |

<h3><code>Now</code></h3>

`Now` captures a value "right now", similar to a `val`:

```scala
scala> import cats.Eval
import cats.Eval

scala> val x = Eval.now {
     |   println("computing x...")
     |   3
     | }
computing x...
x: cats.Eval[Int] = Now(3)

scala> x.value
res0: Int = 3

scala> x.value
res1: Int = 3
```

<h3><code>Always</code></h3>

`Always` captures a lazy computation similar to a `def`:

```scala
scala> val y = Eval.always {
     |   println("computing y...")
     |   3
     | }
y: cats.Eval[Int] = cats.Always@59eae87c

scala> y.value
computing y...
res2: Int = 3

scala> y.value
computing y...
res3: Int = 3
```

<h3><code>Later</code></h3>

`Later` captures a lazy, memoized computation similar to a `lazy val`:

```scala
scala> val z = Eval.later {
     |   println("computing y...")
     |   3
     | }
z: cats.Eval[cats.Eval[Int]] = cats.Later@292f4ca9

scala> z.value
computing y...
res4: cats.Eval[Int] = Now(3)

scala> z.value
res5: cats.Eval[Int] = Now(3)
```

<h3><code>map</code> & <code>flatMap</code></h3>

Like any monad, Eval's `map` and `flatMap` operations add computations to a chain while still keeping the semantics of 
the different Eval instances. Mapping functions (ie. the `yield` block), however, are **always** called lazily on 
demand:

```scala
scala> val result = for {
     |   a <- Eval.now { println("calculating a..."); 3 }
     |   b <- Eval.always { println("calculating b..."); 2 }
     | } yield {
     |   println("multiplying a and b...")
     |   a * b
     | }
calculating a...
result: cats.Eval[Int] = cats.Eval$$anon$9@6413cd8d

scala> result.value
calculating b...
multiplying a and b...
res0: Int = 6

scala> result.value
calculating b...
multiplying a and b...
res1: Int = 6
```

<h3>Trampolining</h3>

One useful property of Eval is that its `map` and `flatMap` operations are trampolined. This means we can nest calls to
`map` and `flatMap` arbitrarily without consuming stack space. This is also known as "stack safety".

Eval has a `defer` operation which takes an existing instance of Eval and defers its evaluation. `defer` is trampolined 
just like `map` and `flatMap` so we can use it as a quick way to make an existing operation stack safe:

Say we had the following method which recursively computes the factorial of a number:

```scala
def factorial(n: BigInt): BigInt = {
  if (n == 1) n
  else n * factorial(n - 1)
}
```

Due to its recursive nature, executing the method with a large value will result in a `StackOverflowException`:

```scala
scala> factorial(50000)
java.lang.StackOverflowError
  at scala.math.BigInt$.apply(BigInt.scala:49)
  at scala.math.BigInt$.long2bigInt(BigInt.scala:101)
  ...
```

Let's refactor our method to use `Eval` and `defer`: 

```scala
import cats.Eval

def factorial(n: BigInt): Eval[BigInt] = {
  if (n == 1) Eval.now(n)
  else Eval.defer(factorial(n - 1).map(_ * n))
}
```

Now if we try executing it with the same large value, we will see that it no longer results in a 
`StackOverflowException`:

```scala
scala> factorial(50000).value
res0: BigInt = 334732050959714483691547609407148647791277322381045480773010032199016802214436564169738123107191693087984
804381902082998936163847430666937426305728453637840383257562821233599872682440782359723560408538544413733837535685655363
711683274051660761551659214061560754612942017905674796654986292422200225415535107181598016154764518106166749702179965374
749725411393381916388235006303076442568748572713946510819098749096434862685892298078700310310089628611545539799116129406
523273969714972110312611428607337935096878373558118306095517289066038335925328516359617308852798119573994952994503063544
424784926410289900695596348835299005576765509291754759207880448076225624151651304590463180685174067663600123295564540657
242251754734281831210291957155937874236411171945138...
```

<h4>Note</h4>

Keep in mind that trampolining is not free. It uses the heap to create a chain of function objects to avoid consuming 
stack space. In this case, computations are bounded by the size of the heap rather than the stack.






