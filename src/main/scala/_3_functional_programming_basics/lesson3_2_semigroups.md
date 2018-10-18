<h4 align="right">
    <a href="lesson3_1_monoids.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson3_3_functors.md">Next →</a>
</h4>

<h1>Semigroups</h1>

A semigroup is just the `combine` part of a monoid. In the Scala Cats library, `Semigroup` is defined as a type class
that looks like the following:

```scala
trait Semigroup[A] {
  def combine(x: A, y: A): A
}
```

In this sense, perhaps a more accurate definition of Cats' `Monoid` type class looks like:

```scala
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}
```

While many semigroups are also monoids, there are some data types for which we cannot define an `empty` element. For
example, Cats has a `NonEmptyList` data type which is a list that by definition can not be empty. It has an 
implementation of `Semigroup` but no implementation of `Monoid`.

<h4 align="right">
    <a href="lesson3_1_monoids.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson3_3_functors.md">Next →</a>
</h4>