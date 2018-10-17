<h4 align="right">
    <a href="lesson2_4_context_bounds.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_6_recursive_implicit_resolution.md">Next →</a>
</h4>

<h1>Implicit Scope & Packaging</h1>

When looking for candidate instances for implicit parameters, the compiler will search the implicit scope at the call 
site which, in order of priority, roughly consists of:

 - local or inherited definitions
 - imported definitions
 - definitions in the companion object of the type class or the parameter type

<h3>How to package type class instances</h3>

We can package type class instances in roughly 4 ways:

  1. by placing them in an object like `ShapeAreas`
  2. by placing them in a trait
  3. by placing them in the companion object of the type class
  4. by placing them in the companion object of the type parameter

To demonstrate the different ways we can package type class instances, let's suppose we want to call the following 
method:

```scala
def foo(circle: Circle)(implicit area: Area[Circle]): Double = {
  area.calculate(circle)
}
```

<h4>Define instances in an object</h4>

With option 1, we bring instances into scope by importing them like we have been doing so far:

```scala
object ShapeAreas {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }
}
```

```scala
@ import ShapeAreas._
import ShapeAreas._

@ foo(Circle(5))
res0: Double = 78.53981633974483
```

<h4>Inherit instances</h4>

With option 2, we bring instances into scope through inheritance:

```scala
trait CircleArea {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }
}
```

```scala
@ object Service extends CircleArea {
     |   def bar: Double = {
     |     foo(Circle(5))
     |   }
     | }
defined object Service

@ Service.bar
res0: Double = 78.53981633974483
```

<h4>Define instances in companion objects</h4>

With options 3 and 4, instances are always in implicit scope:

```scala
// You can define type class instances in the companion object of the type class itself
object Area {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }
}

// Or alternatively, you can define type class instances in the companion object of the
// particular type that you want to provide a type class instance for
object Circle {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }
}
```

No explicit imports required:
```scala
@ foo(Circle(5))
res0: Double = 78.53981633974483
```

<h3>Default instances</h3>

When packaging type class instances, if: 
  
  1. there is only a single instance for a type or 
  2. a single good default for a type
  
then put it in the companion object of the type if possible. This allows users to override the instance by defining one 
in the local scope while still providing sensible default behavior.

```scala
object Circle {
  // default area calculation for a circle
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius
    }
  }
}
```

Otherwise, we can place different type class instances into their own objects and require the users to explicitly import 
them based on what they need.

```scala
object DoubleCircleArea {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius * 2
    }
  }
}

object HalfCircleArea {
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def calculate(circle: Circle): Double = {
      Math.PI * circle.radius * circle.radius / 2
    }
  }
}
```

```scala
@ import DoubleCircleArea._
import DoubleCircleArea._

@ Area[Circle].calculate(Circle(5))
res0: Double = 157.07963267948966
```

```scala
@ import HalfCircleArea._
import HalfCircleArea._

@ Area[Circle].calculate(Circle(5))
res1: Double = 39.269908169872416
```

<h4 align="right">
    <a href="lesson2_4_context_bounds.md">← Previous</a> |
    <a href="lesson2.md">Menu</a> |
    <a href="lesson2_6_recursive_implicit_resolution.md">Next →</a>
</h4>