<h4 align="right">
    <a href="lesson5_3_apply_applicative.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson5_5_traverse.md">Next →</a>
</h4>

<h1>Foldable</h1>

The `Foldable` type class captures the `foldLeft` and `foldRight` methods we're used to in `Seq`s, `List`s, `Vector`s,
and `Stream`s. With `Foldable` we can write generic folds that work with a variety of sequence types.

`foldLeft` works recursively down the list starting on the left side. Our binary function is called for each item and
the result becomes the accumulator value for the next call:

```scala
@ List(1, 2, 3, 4, 5).foldLeft(0)((accum, num) => accum + (num + 1))
res0: Int = 20
```

<h3><code>foldLeft</code> vs. <code>foldRight</code></h3>

`foldLeft` traverses from left to right while `foldRight` traverses from right to left They are both equivalent if our
binary operation is commutative:

```scala
@ List(1, 2, 3).foldLeft(0)(_ + _)
res1: Int = 6
```

| accum | value | new accum |
| ----- | ----- | --------- |
| 0     | 1     | 0 + 1     |
| 1     | 2     | 1 + 2     |
| 3     | 3     | 3 + 3     |

```scala
@ List(1, 2, 3).foldRight(0)(_ + _)
res2: Int = 6
```

| value | accum | new accum |
| ----- | ----- | --------- |
| 3     | 0     | 3 + 0     |
| 2     | 3     | 2 + 3     |
| 1     | 5     | 1 + 5     |

If we provided a non-commutative operation, then the order of evaluation makes a difference:

```scala
@ List(1, 2, 3).foldLeft(0)(_ - _)
res3: Int = -6
```

| accum | value | new accum |
| ----- | ----- | --------- |
| 0     | 1     | 0 - 1     |
| -1    | 2     | -1 - 2    |
| -3    | 3     | -3 - 3    |

```scala
@ List(1, 2, 3).foldRight(0)(_ - _)
res4: Int = 2
```

| value | accum | new accum |
| ----- | ----- | --------- |
| 3     | 0     | 3 - 0     |
| 2     | 3     | 2 - 3     |
| 1     | -1    | 1 - (-1)  |

<h3>Foldable</h3>

Cats' `Foldable` abstracts `foldLeft` and `foldRight` into a type class. It provides out-of-the-box instances for a
handful of Scala data types (`List`, `Vector`, `Stream`, `Option`):

```scala
import cats.{Eval, Foldable}
import cats.implicits._

@ Foldable[List].foldLeft(List(1, 2, 3), 0)(_ - _)
res5: Int = -6

@ Foldable[List].foldRight(List(1, 2, 3), Eval.now(0))((num, eval) => eval.map(num - _)).value
res6: Int = 2
```

`Foldable` also provides us with a lot of the same methods that are defined in the collections from the standard library
such as: `find`, `exists`, `forall`, `toList`, `isEmpty`, `nonEmpty`, etc.

```scala
@ Foldable[List].find(List(1, 2, 3))(_ % 2 == 0)
res7: Option[Int] = Some(2)
```

In addition, `Foldable` provides 2 methods that make use of `Monoid`s:

  - `combineAll` combines all elements in the sequence using their `Monoid`:

    ```scala
    import cats.implicits._

    @ Foldable[List].combineAll(List(1, 2, 3))
    res3: Int = 6
    ```

  -  `foldMap` maps a function over the sequence and then combines the results using a `Monoid`:
    
    ```scala
    import cats.implicits._

    @ Foldable[List].foldMap(List(1, 2, 3))(_ + 1)
    res4: Int = 9
    ```

You can also compose Foldables to support deep traversal of nested sequences:

```scala
val ints = List(Vector(1, 2, 3), Vector(4, 5, 6))

@ (Foldable[List] compose Foldable[Vector]).combineAll(ints)
res5: Int = 21
```

<h4 align="right">
    <a href="lesson5_3_apply_applicative.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson5_5_traverse.md">Next →</a>
</h4>