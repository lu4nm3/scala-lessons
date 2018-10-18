<h4 align="right">
    <a href="../_2_type_classes/lesson2_6_recursive_implicit_resolution.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
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

Let's look at a few real-world examples of the Monoid.

<h3>Integer addition</h3>

Let's look at integer addition:

```
// + is the associative combine operation

(1 + 2) + 3

1 + (2 + 3)

// 0 is the identity element

3 + 0 == 3

0 + 3 == 3
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
@ intAdd.combine(intAdd.combine(1, 2), 3)
res0: Int = 6

@ intAdd.combine(1, intAdd.combine(2, 3))
res1: Int = 6

@ intAdd.combine(3, intAdd.empty)
res2: Int = 3

@ intAdd.combine(intAdd.empty, 3)
res3: Int = 3
```

<h3>Integer multiplication</h3>

```
// * is the associative combine operation

(2 * 3) * 4

2 * (3 * 4)

// 1 is the identity element

3 * 1 == 3

1 * 3 == 3
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
@ intMult.combine(intMult.combine(2, 3), 4)
res4: Int = 24

@ intMult.combine(2, intMult.combine(3, 4))
res5: Int = 24

@ intMult.combine(3, intMult.empty)
res6: Int = 3

@ intMult.combine(intMult.empty, 3)
res7: Int = 3
```

<h3>String concatenation</h3>

```
// ++ is the associative combine operation

("one" + "two") + "three"

"one" + ("two" + "three")

// "" (ie. empty string) is the identity element

"" + "three" == "three"

"three" + "" == "three"
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
@ strConcat.combine(strConcat.combine("one", "two"), "three")
res8: String = onetwothree

@ strConcat.combine("one", strConcat.combine("two", "three"))
res9: String = onetwothree

@ strConcat.combine("three", strConcat.empty)
res10: String = three

@ strConcat.combine(strConcat.empty, "three")
res11: String = three
```

<h4 align="right">
    <a href="../_2_type_classes/lesson2_6_recursive_implicit_resolution.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson3_2_semigroups.md">Next →</a>
</h4>