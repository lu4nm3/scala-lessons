<h4 align="right">
    <a href="lesson2_3_2_interface_syntax.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_5_implicit_scope_packaging.md">Next →</a>
</h4>

<h1>Implicit Context Bounds</h1>

Context bounds allow us to write implicit parameters more compactly by using a shorthand notation.

Instead of:

```scala
object Area {
  def apply[S](implicit calculator: Area[S]): Area[S] = calculator
}
```

We can do:

```scala
object Area {
  def apply[S: Area]: Area[S] = {
    // Since we no longer have an explicit name for the implicit parameter, we can't use 
    // it in our method definition. To get around this, we make use of the `implicitly` 
    // summoner in order to obtain the instance of the type class.
    implicitly[Area[S]]
  }
}
```

And then use it just like before:

```scala
scala> import ShapeAreas._
import ShapeAreas._

scala> Area[Circle].calculate(Circle(5))
res0: Double = 78.53981633974483
```

Similarly, you can use context bounds to define implicit parameters in classes.

Without context bounds:

```scala
implicit class AreaOps[S](shape: S) {
  def area(implicit calculator: Area[S]): Double = {
    calculator.calculate(shape)
  }
}
```

With context bounds:

```scala
implicit class AreaOps[S: Area](shape: S) {
  def area: Double = {
    implicitly[Area[S]].calculate(shape) // we need to use implicitly here too!
  }
}
```
You can also specify multiple context bounds:

```scala
trait Foo[A] {
  def foo: Int
}
trait Bar[A] {
  def bar: Int
}

def fooBar[A: Foo : Bar]: Int = {
  implicitly[Foo[A]].foo + implicitly[Bar[A]].bar
}
```

Which is the same thing as requiring multiple implicit parameters:

```scala
def fooBar[A](implicit F: Foo[A], B: Bar[A]): Int = {
  F.foo + B.bar
}
```

<h4 align="right">
    <a href="lesson2_3_2_interface_syntax.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_5_implicit_scope_packaging.md">Next →</a>
</h4>