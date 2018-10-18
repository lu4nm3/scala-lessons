<h4 align="right">
    <a href="lesson2_1_classes.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson2_3_1_interface_objects.md">Next →</a>
</h4>

<h1>Type Class Instances</h1>

The instances of a type class provide implementations for the types that we care about. These types can include not only 
your own custom types but also types from the Scala standard library as well as types defined in 3rd-party libraries.

In Scala we define instances by creating concrete implementations of the type class and tagging them with the `implicit` 
keyword.

Using our custom `Shape` type and some supporting implementations:
```scala
sealed trait Shape
case class Circle(radius: Int) extends Shape
case class Triangle(base: Int, height: Int) extends Shape
```

We can define instances for our `Area` type class:
```scala
trait Area[S] {
  def calculate(shape: S): Double
}

object ShapeAreas {

  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }

  implicit val triangleArea: Area[Triangle] = new Area[Triangle] {
    def calculate(triangle: Triangle): Double = {
      0.5 * triangle.base * triangle.height
    }
  }
}
```

<h4 align="right">
    <a href="lesson2_1_classes.md">← Previous</a> |
    <a href="../README.md">Menu</a> |
    <a href="lesson2_3_1_interface_objects.md">Next →</a>
</h4>