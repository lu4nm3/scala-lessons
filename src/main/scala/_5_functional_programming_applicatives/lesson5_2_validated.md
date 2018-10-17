<h4 align="right">
    <a href="lesson5_1_semigroupal.md">← Previous</a> |
    <a href="lesson5.md">Menu</a> |
    <a href="lesson5_3_apply_applicative.md">Next →</a>
</h4>

<h1>Validated</h1>

Cats provides a data type called `Validated` that has an instance of `Semigroupal` which allows it to accumulate errors.

`Validated` has 2 subtypes, `Valid` and `Invalid` which are equivalent to `Either`'s `Right` and `Left`:

```scala
import cats.data.Validated
import cats.implicits._

@ Validated.valid[List[String], Int](123)
res0: cats.data.Validated[List[String],Int] = Valid(123)

@ Validated.invalid[List[String], Int](List("error"))
res1: cats.data.Validated[List[String],Int] = Invalid(List(error))
```

Since `Validated` has an instance of `Semigroupal` you can use methods like `tupled` to combine them:

```scala
@ (
     |   Vector("error 1").invalid[Int],
     |   Vector("error 2").invalid[Int]
     | ).tupled
res2: cats.data.Validated[scala.collection.immutable.Vector[String],(Int, Int)] = Invalid(Vector(error 1, error 2))
```

You can also use syntax methods like `.valid` and `.invalid` to create instances of `Validated` directly from other
types. In addition, you can use methods like `map` and `leftMap` to modify the the value or the log of `Validated`
depending on whether it is valid or invalid:

```scala
@ 123.valid[List[String]]
res3: cats.data.Validated[List[String],Int] = Valid(123)

@ 123.valid[List[String]].map(_ * 10)
res4: cats.data.Validated[List[String],Int] = Valid(1230)
```

```scala
@ "error!".invalid[Int]
res5: cats.data.Validated[String,Int] = Invalid(error!)

@ "error!".invalid[Int].leftMap(_.toUpperCase)
res5: cats.data.Validated[String,Int] = Invalid(ERROR!)
```

You can also convert back and forth between `Validated` and `Either`:

```scala
@ "error!".invalid[Int].toEither
res7: Either[String,Int] = Left(error!)

@ "error!".invalid[Int].toEither.toValidated
res8: cats.data.Validated[String,Int] = Invalid(error!)
```

<h4 align="right">
    <a href="lesson5_1_semigroupal.md">← Previous</a> |
    <a href="lesson5.md">Menu</a> |
    <a href="lesson5_3_apply_applicative.md">Next →</a>
</h4>