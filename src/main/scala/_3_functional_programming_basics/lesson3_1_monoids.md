<h4 align="right">
    <a href="lesson3.md">Menu</a> |
    <a href="lesson3_2_semigroups.md">Next →</a>
</h4>

<h1>Monoids</h1>

Formally, a monoid for type `A` is:

  - an operation `combine` of type `(A, A) => A`
  - an element `empty` of type `A`

In the Scala Cats library, `Monoid` is represented as a type class that looks like:

```scala
trait Monoid[A] {
  def combine(x: A, y: A): A
  def empty: A
}
```

In addition, a monoid's `combine` must be associative and `empty` must be an identity element (ie. when a value is 
combined with `empty` the result must be the original value).

Let's look at a few real world examples of the Monoid.

<h3>Integer addition</h3>

Let's look at integer addition:

```
// `+` is the associative combine operation

(1 + 2) + 3

1 + (2 + 3)

// `0` is the identity element

2 + 0 == 2

0 + 2 == 2
```

We can create a type class instance of `Monoid` for integer addition:

```scala
import cats.Monoid

implicit val intAdd: Monoid[Int] = new Monoid[Int] {
  def combine(x: Int, y: Int): Int = x + y

  def empty: Int = 0
}
```

And we can test out these properties:

```scala
scala> intAdd.combine(1, 2)
res0: Int = 3

scala> intAdd.combine(2, 1)
res1: Int = 3

scala> intAdd.combine(2, intAdd.empty)
res2: Int = 2

scala> intAdd.combine(intAdd.empty, 2)
res3: Int = 2
```

<h3>Integer multiplication</h3>

```
// `*` is the associative combine operation

(1 * 2) * 3

1 * (2 * 3)

// `1` is the identity element

2 * 1 == 2

1 * 2 == 2
```

We can also create a type class instance of `Monoid` for integer multiplication:

```scala
implicit val intMult: Monoid[Int] = new Monoid[Int] {
  def combine(x: Int, y: Int): Int = x * y
  
  def empty: Int = 1
}
```

And we will see that the associative and identity properties for integer multiplication hold:

```
scala> intMult.combine(1, 2)
res4: Int = 2

scala> intMult.combine(2, 1)
res5: Int = 2

scala> intMult.combine(2, intAdd.empty)
res6: Int = 0

scala> intMult.combine(intAdd.empty, 2)
res7: Int = 0
```

<h3>String concatenation</h3>

```
// `++` is the associative combine operation

"one" ++ "two"

// `""` (ie. empty string) is the identity element

"" ++ "one" == "one"

"one" ++ "" == "one"
```

Just like we've done for integers, we can create a `Monoid` for String concatenation:

```scala
implicit val strConcat: Monoid[String] = new Monoid[String] {
  def combine(x: String, y: String): String = x + y
  
  def empty: String = ""
}
```

And we can observe that once again, the properties for a Monoid's combine and identity operations hold when doing string
concatenation:

```scala
scala> strConcat.combine("one", "two")
res8: String = onetwo

scala> strConcat.combine("one", strConcat.empty)
res9: String = one

scala> strConcat.combine(strConcat.empty, "one")
res10: String = one
```

<h4 align="right">
    <a href="lesson3.md">Menu</a> |
    <a href="lesson3_2_semigroups.md">Next →</a>
</h4>