// ****************************************************************
//                   Implicit Scope & Packaging
// ****************************************************************




trait Area[S] {
  def calculate(shape: S): Double
}

sealed trait Shape
case class Circle(radius: Int) extends Shape




// When looking for candidate instances for implicit parameters,
// the compiler will search the implicit scope at the call site
// which, in order of priority, roughly consists of:
//
// - local or inherited definitions
// - imported definitions
// - definitions in the companion object of the type class or the
//   parameter type

// We can package type class instances in roughly 4 ways:
//
// 1. by placing them in an object like `ShapeAreas`
// 2. by placing them in a trait
// 3. by placing them in the companion object of the type class
// 4. by placing them in the companion object of the type parameter

def foo(circle: Circle)(implicit area: Area[Circle]): Double = {
  area.calculate(circle)
}

// With option 1 we bring instances into scope by importing them.

//object ShapeAreas {
//  implicit val circleArea: Area[Circle] = new Area[Circle] {
//    override def calculate(circle: Circle): Double = {
//      Math.PI * circle.radius * circle.radius
//    }
//  }
//}
//
//import ShapeAreas._
//foo(Circle(5))

// With option 2 we bring instances into scope with inheritance.

//trait CircleArea {
//  implicit val circleArea: Area[Circle] = new Area[Circle] {
//    override def calculate(circle: Circle): Double = {
//      Math.PI * circle.radius * circle.radius
//    }
//  }
//}
//
//object Service extends CircleArea {
//  def bar: Double = {
//    foo(Circle(5))
//  }
//}

// With options 3 and 4, instances are always in implicit scope.

//object Area {
//  implicit val circleArea: Area[Circle] = new Area[Circle] {
//    override def calculate(circle: Circle): Double = {
//      Math.PI * circle.radius * circle.radius
//    }
//  }
//}

//object Circle {
//  implicit val circleArea: Area[Circle] = new Area[Circle] {
//    override def calculate(circle: Circle): Double = {
//      Math.PI * circle.radius * circle.radius
//    }
//  }
//}

//foo(Circle(5))




// When packaging type class instances, if there is a single
// instance for a type or a single good default for a type, then
// put it in the companion object of the type if possible. This
// allows users to override the instance by defining one in the
// local scope whilst still providing sensible default behavior.

//object Circle {
//  implicit val circleArea: Area[Circle] = new Area[Circle] {
//    override def calculate(circle: Circle): Double = {
//      Math.PI * circle.radius * circle.radius
//    }
//  }
//}

// Otherwise, one way to package implicits is to place each one in
// an object and require the user to explicitly import them based
// on what they need.

object DoubleCircleArea {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    override def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius * 2
    }
  }
}

object HalfCircleArea {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    override def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius / 2
    }
  }
}