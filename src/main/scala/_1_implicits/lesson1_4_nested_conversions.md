<h4 align="right">
    <a href="lesson1_3_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_2_type_classes/lesson2.md">Next →</a>
</h4>

<h1>Nested Implicit Conversions</h1>

We have seen implicit conversions from one type to another:

```scala
import scala.language.implicitConversions

case class Foo() {
  def foo: String = "foo"
}

case class Bar() {
  def bar: String = "bar"
}

case class Baz() {
  def baz: String = "baz"
}

implicit def fooToBar(foo: Foo): Bar = Bar()
implicit def barToBaz(bar: Bar): Baz = Baz()
```

```scala
@ Foo().bar
res0: String = bar

@ Bar().baz
res1: String = baz
```

However, just because we have one conversion from `Foo` to `Bar` and another from `Bar` to `Baz` does not mean that we 
can convert an instance of `Foo` directly to an instance of `Baz`:

```scala
@ Foo().baz
<console>:17: error: value baz is not a member of Foo
       Foo().baz
             ^
```

We can get around this by supplying an implicit parameter to the conversion methods:

```scala
implicit def fooToBar[F](foo: F)(implicit f: F => Foo): Bar = Bar()
implicit def barToBaz[B](bar: B)(implicit f: B => Bar): Baz = Baz()
```

```scala
@ Foo().baz
res3: String = baz
```

Here the compiler will first search the implicit scope to see if there is an implicit conversion from `Foo` to another 
type that has the method `baz` (ie. in this case, that's the type `Baz`). Such a conversion doesn't exist. HOWEVER,
there is an implicit conversion between the generic type `B` and type `Baz` so the compiler will explore if it can use
it somehow: 

```scala
barToBaz(Foo())(...)
```

Next, the compiler will search the implicit scope for the implicit function `B => Bar`. Here we said that `B` is our 
type `Foo` and so it finds that the function `fooToBar` meets these requirements:

```scala
barToBaz(Foo())(fooToBar(_)(...))
```

You might be wondering what implicit function `F => Foo` is used for `fooToBar`. Since `F` is our type `Foo`, it means
that the implicit function that we need will look like `Foo => Foo`. This is also known as the `identity` function which
is automatically provided by the Scala standard library so the compiler uses that:

```scala
barToBaz(Foo())(fooToBar(_)(identity))
```

This approach of doing nested implicit conversions also works for implicit classes:

```scala
case class Foo() {
  def foo: String = "foo"
}

implicit class Bar[F](foo: F)(implicit f: F => Foo) {
  def bar: String = "bar"
}

implicit class Baz[F, B](bar: B)(implicit f: B => Bar[F]) {
  def baz: String = "baz"
}
```

```scala
@ Foo().baz
res0: String = baz
```

<h4 align="right">
    <a href="lesson1_3_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_2_type_classes/lesson2.md">Next →</a>
</h4>