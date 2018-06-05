// ****************************************************************
//                      Type Class Instances
// ****************************************************************




trait Area[S] {
  def calculate(shape: S): Double
}

sealed trait Shape
case class Circle(radius: Int) extends Shape
case class Triangle(base: Int, height: Int) extends Shape




// The instances of a type class provide implementations for the
// types that we care about. These can include not only your own
// custom types but also types from the Scala standard library as
// well as type defined in third-party libraries.

// In Scala we define instance by creating concrete implementations
// of the type class and tagging them with the `implicit` keyword:

object ShapeAreas {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    override def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }

  implicit val triangleArea: Area[Triangle] = new Area[Triangle] {
    override def calculate(triangle: Triangle): Double = {
      0.5 * triangle.base * triangle.height
    }
  }
}


