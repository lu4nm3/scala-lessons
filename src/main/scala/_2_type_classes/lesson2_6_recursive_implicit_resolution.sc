// ****************************************************************
//                   Recursive Implicit Resolution
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




// Part of the power of type classes and implicits lies in the
// compiler's ability to combine implicit definitions when
// searching for candidate instances.

// So far, we have been defining implicit instances as implicit
// vals.

implicit val circleArea: Area[Circle] = new Area[Circle] {
  override def calculate(circle: Circle): Double = {
    Math.PI * circle.radius * circle.radius
  }
}

// But we can also define instances as implicit methods that can be
// used to construct instances from other type class instances.

// As an example, consider defining an instance of Area for a List
// of shapes that adds the areas of all the shapes in the list. We
// would need an `Area[List[S]]` for every type of Shape `S` that
// we care about.

implicit val listCircleArea: Area[List[Circle]] = new Area[List[Circle]] {
  override def calculate(circle: List[Circle]): Double = ???
}

implicit val listTriangleArea: Area[List[Triangle]] = new Area[List[Triangle]] {
  override def calculate(triangle: List[Triangle]): Double = ???
}

// However, this approach doesn't scale as now we end up requiring
// 2 vals for every type S (one val for the type itself and one val
// for List[S]). We would also be duplicating the logic for
// calculating the area of different shapes.

// What we can do instead is to abstract the code for handling
// List[S] into a common method based on the instance of `S`. This
// method constructs an `Area` instance for `List[S]` by relying on
// an implicit parameter to fill in the S-specific functionality.

implicit def listArea[S](implicit area: Area[S]): Area[List[S]] = new Area[List[S]] {
  override def calculate(shapes: List[S]): Double = {
    if (shapes.isEmpty) {
      0
    } else {
      shapes.map(area.calculate).sum
    }
  }
}

// Given our previous interface object

object Area {

  def apply[S](implicit calculator: Area[S]): Area[S] = {
    calculator
  }

}

// When the compiler sees an expression like:

Area[List[Circle]].calculate(List(Circle(2), Circle(3)))

// it searches for an implicit `Area[List[Circle]]`. It finds the
// implicit method for `Area[List[S]]`:

Area[List[Circle]].calculate(List(Circle(2), Circle(3)))(listArea[Circle])

// and recursively searches for an `Area[Circle]` to use as the
// parameter to `listArea`:

Area[List[Circle]].calculate(List(Circle(2), Circle(3)))(listArea(ShapeAreas.circleArea))
