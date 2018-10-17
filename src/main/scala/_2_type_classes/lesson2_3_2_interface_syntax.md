<h4 align="right">
    <a href="lesson2_3_1_interface_objects.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_4_context_bounds.md">Next →</a>
</h4>

<h1>Type Class Interface Syntax</h1>

Alternatively we can use extension methods to extend existing types with functionality from our type class. We do this 
by using implicit classes like we saw in an earlier lesson:

```scala
object ShapeSyntax {

  implicit class AreaOps[S](shape: S) {
    def area(implicit calculator: Area[S]): Double = {
      calculator.calculate(shape)
    }
  }

}
```

This implicit class will expose the `area` method to any type `S` only if there is an implicit instance `Area[S]` for 
that type in scope.

We then use the interface syntax by importing the implicit class along with the type class instances for the types we 
need:

```scala
@ import ShapeAreas._
import ShapeAreas._

@ import ShapeSyntax._
import ShapeSyntax._

@ Circle(5).area
res0: Double = 78.53981633974483
```

Again, the compiler searches for candidates for the implicit parameters and fills them in for us:

```scala
Circle(5).area(circleArea)
```

<h4 align="right">
    <a href="lesson2_3_1_interface_objects.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_4_context_bounds.md">Next →</a>
</h4>