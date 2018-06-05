// ****************************************************************
//                      Implicit Context Bounds
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




// Context bounds allow us to write single implicit parameters more
// compactly by using a shorthand notation.

object Area {

  def apply[S](implicit calculator: Area[S]): Area[S] = {
    calculator
  }

//  def apply[S: Area]: Area[S] = {
//    // Since we no longer have an explicit name for the implicit
//    // parameter we can't use it in our methods. To get around
//    // this, we make use of the `implicitly` summoner in order to
//    // obtain the instance of the type class.
//    implicitly[Area[S]]
//  }

}

import ShapeAreas._

Area[Circle].calculate(Circle(5))




// Similarly, you can use context bounds to define implicit
// parameters in classes.

object ShapeSyntax {

  implicit class AreaOps[S](shape: S) {
    def area(implicit calculator: Area[S]): Double = {
      calculator.calculate(shape)
    }
  }

//  implicit class AreaOps[S: Area](shape: S) {
//    def area: Double = {
//      implicitly[Area[S]].calculate(shape)
//    }
//  }

//  implicit class AreaOps[S](shape: S)(implicit calculator: Area[S]) {
//    def area: Double = {
//      calculator.calculate(shape)
//    }
//  }

}

import ShapeAreas._
import ShapeSyntax._

Circle(5).area




// You can also specify multiple context bounds.

trait Foo[A] {
  def foo: Int
}
trait Bar[A] {
  def bar: Int
}

def fooBar[A: Foo : Bar]: Int = {
  implicitly[Foo[A]].foo + implicitly[Bar[A]].bar
}

def fooBar2[A](implicit F: Foo[A], B: Bar[A]): Int = {
  F.foo + B.bar
}