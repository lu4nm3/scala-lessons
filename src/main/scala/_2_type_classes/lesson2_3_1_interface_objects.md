<h4 align="right">
    <a href="lesson2_2_instances.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_3_2_interface_syntax.md">Next →</a>
</h4>

<h1>Type Class Interface Objects</h1>

<h3>Object methods</h3>

The simplest way of creating an interface to a type class is to create methods in a singleton object to expose specific 
functionality from your type class:

```scala
object Shape {
  def calculateArea[S](shape: S)(implicit calculator: Area[S]): Double = {
    calculator.calculate(shape)
  }
}
```

To use this object, we import any type class instances we care about and call the relevant method:

```scala
@ import ShapeAreas._
import ShapeAreas._

@ Shape.calculateArea(Circle(5))
res0: Double = 78.53981633974483
```

Here the compiler notices that we called the `calculateArea` method without providing an implicit parameter. So it tries 
to fix this by searching for the type class instance of the relevant type and inserting it at the call site:

```scala
Shape.calculateArea(Circle(5))(circleArea)
```

<h3>Summoner method</h3>

If you're creating individual object methods for **ALL** the functionality of a type class, then we can alternatively 
avoid the indirection of having the object methods call the type class methods and just expose the type class
directly.

This commonly done by defining what is known as a "summoner" method in the companion object of the type class itself. 
This summoner is just a 0-argument `apply` method:

```scala
object Area {
  def apply[S](implicit calculator: Area[S]): Area[S] = calculator
}
```

And then we can call this method by simply passing in the type that we're interesting in using. Remember that the type
class instance for that type also has to be in scope:

```scala
@ import ShapeAreas._
import ShapeAreas._

@ Area[Circle].calculate(Circle(5))
res0: Double = 78.53981633974483
```

This is very similar to how the `implicitly` method from the Scala standard library is defined.

```scala
def implicitly[T](implicit e: T): T = e
```

```scala
@ implicitly[Area[Circle]].calculate(Circle(5))
res1: Double = 78.53981633974483
```

<h4 align="right">
    <a href="lesson2_2_instances.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_3_2_interface_syntax.md">Next →</a>
</h4>