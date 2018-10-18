<h4 align="right">
    <a href="lesson2_5_implicit_scope_packaging.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_3_functional_programming_basics/lesson3.md">Next →</a>
</h4>

<h1>Recursive Implicit Resolution</h1>

Part of the power of type classes and implicits lies in the compiler's ability to combine implicit definitions when 
searching for candidate instances.

So far, we have been defining implicit instances as `implicit val`s.

```scala
implicit val circleArea: Area[Circle] = new Area[Circle] {
  def calculate(circle: Circle): Double = {
    Math.PI * circle.radius * circle.radius
  }
}
```

But we can also define them as implicit methods that can be used to construct instances from other type class instances.

As an example, consider defining an instance of `Area` for a `List` of shapes that adds the areas of all the shapes in 
the list. We would need an `Area[List[S]]` for every type of Shape `S` that we care about.

```scala
implicit val listCircleArea: Area[List[Circle]] = new Area[List[Circle]] {
  def calculate(circle: List[Circle]): Double = ???
}

implicit val listTriangleArea: Area[List[Triangle]] = new Area[List[Triangle]] {
  def calculate(triangle: List[Triangle]): Double = ???
}
...
```

However, this approach doesn't scale as now we end up requiring 2 `val`s for every type `S` (one val for the type itself 
and one `val` for `List[S]`). We would also be duplicating logic.

What we can do instead is to abstract the code for handling `List[S]` into a common method based on an instance for `S`. 
This method constructs an `Area` instance for `List[S]` by relying on an implicit parameter to fill in the S-specific 
functionality.

```scala
implicit def listArea[S](implicit area: Area[S]): Area[List[S]] = new Area[List[S]] {
  def calculate(shapes: List[S]): Double = {
    if (shapes.isEmpty) 0
    else shapes.map(area.calculate).sum
  }
}
```

Given our previous interface object:

```scala
object Area {
  def apply[S](implicit calculator: Area[S]): Area[S] = calculator
}
```

When the compiler sees an expression like:

```scala
Area[List[Circle]].calculate(List(Circle(2), Circle(3)))
```

it searches for an implicit `Area[List[Circle]]`. It finds the implicit method for `Area[List[S]]`:

```scala
Area[List[Circle]].calculate(List(Circle(2), Circle(3)))(listArea[Circle])
```

and recursively searches for an `Area[Circle]` to use as the parameter to `listArea[Circle]`:

```scala
import ShapeAreas._

Area[List[Circle]].calculate(List(Circle(2), Circle(3)))(listArea[Circle](circleArea))
```

<h4 align="right">
    <a href="lesson2_5_implicit_scope_packaging.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_3_functional_programming_basics/lesson3.md">Next →</a>
</h4>