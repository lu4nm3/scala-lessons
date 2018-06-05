// ****************************************************************
//                   Type Class Interface Objects
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




// The simplest way of creating an interface is to place methods in
// a singleton object:

object Shape {

  def calculateArea[S](shape: S)(implicit calculator: Area[S]): Double = {
    calculator.calculate(shape)
  }

}

// To use this object, we import any type class instances we care
// about and call the relevant method:

import ShapeAreas._

Shape.calculateArea(Circle(5))

// Here the compiler notices that we called the `calculate` method
// without providing an implicit parameter. So it tries to fix this
// by searching for the type class instance of the relevant type
// and inserting it at the call site:

Shape.calculateArea(Circle(5))(circleArea)




// If the interface to a type class is exactly the methods defined
// on the type class, then we can alternatively avoid the
// indirection of having the `calculateArea` method call the 
// calculator's `calculate` method.
//
// This commonly done by defining a "summoner" method in the
// companion object of the type class which is just a 0-argument
// `apply` method:

object Area {

  def apply[S](implicit calculator: Area[S]): Area[S] = {
    calculator
  }

}

Area[Circle].calculate(Circle(5))

// This is very similar to how the `implicitly` method from the
// Scala standard library is defined.

def implicitly[T](implicit e: T): T = e



