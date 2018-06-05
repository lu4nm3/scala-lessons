// ****************************************************************
//                    Type Class Interface Syntax
// ****************************************************************




trait Area[S] {
  def calculate(shape: S): Double
}

sealed trait Shape
case class Circle(radius: Int) extends Shape
case class Triangle(base: Int, height: Int) extends Shape

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




// Alternatively we can use extension methods to extend existing
// types with interface methods. We do this by using implicit
// classes like we saw earlier:

object ShapeSyntax {

  implicit class AreaOps[S](shape: S) {
    def area(implicit calculator: Area[S]): Double = {
      calculator.calculate(shape)
    }
  }

}

// We then use the interface syntax by importing it alongside the
// type class instances for the types we need:

import ShapeAreas._
import ShapeSyntax._

Circle(5).area

// Again, the compiler searches for candidates for the implicit
// parameters and fills them in for us:

Circle(5).area(circleArea)