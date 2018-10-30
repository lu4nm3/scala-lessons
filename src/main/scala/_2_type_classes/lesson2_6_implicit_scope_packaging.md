<h4 align="right">
    <a href="lesson2_5_context_bounds.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_7_recursive_implicit_resolution.md">Next →</a>
</h4>

<h1>Scope & Packaging</h1>

When looking for candidate instances for implicit parameters, the compiler will search the implicit scope at the call 
site which, in order of priority, roughly consists of:

 - local or inherited definitions
 - imported definitions
 - definitions in the companion object of the type class or the parameter type

<h3>How to package type class instances</h3>

We can package type class instances in roughly 4 ways:

  1. by placing them in an object like `Costs`
  2. by placing them in a trait
  3. by placing them in the companion object of the type class
  4. by placing them in the companion object of the type parameter

To demonstrate the different ways we can package type class instances, let's suppose we want to call the following 
method:

```scala
def foo(truck: Truck)(implicit cost: Cost[Truck]): Double = {
  cost.calculate(truck)
}
```

<h4>Define instances in an object</h4>

With option 1, we bring instances into scope by importing them like we have been doing so far:

```scala
object Costs {
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
}
```

```scala
@ import Costs._
import Costs._

@ foo(Truck())
res0: Double = 180000.0
```

<h4>Inherit instances</h4>

With option 2, we bring instances into scope through inheritance:

```scala
trait TruckCost {
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
}
```

```scala
@ object Service extends TruckCost {
    def getCostOfTruck: Double = foo(Truck())
  }
defined object Service

@ Service.getCostOfTruck
res0: Double = 180000.0
```

<h4>Define instances in companion objects</h4>

With options 3 and 4, instances are always in implicit scope:

```scala
// You can define type class instances in the companion object of the type class itself
object Cost {
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
}

// Or alternatively, you can define type class instances in the companion object of the
// particular type that you want to provide a type class instance for
object Truck {
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
}
```

No explicit imports required:
```scala
@ foo(Truck())
res0: Double = 180000.0
```

<h3>Default instances</h3>

When packaging type class instances, if: 
  
  1. there is only a single instance for a type or 
  2. a single good default for a type
  
then put it in the companion object of the type if possible. This allows users to override the instance by defining one 
in the local scope while still providing sensible default behavior.

```scala
object Truck {
  // default cost calculation for a truck
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
}
```

Otherwise, we can place different type class instances into their own objects and require the users to explicitly import 
them based on what they need.

```scala
object CostToBuyTruck {
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
}

object CostToMaintainTruck {
  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 5000.0
  }
}
```

```scala
@ import CostToBuyTruck._
import CostToBuyTruck._

@ Cost[Truck].calculate(Truck())
res0: Double = 180000.0
```

```scala
@ import CostToMaintainTruck._
import CostToMaintainTruck._

@ Cost[Truck].calculate(Truck())
res1: Double = 5000.0
```

<h4 align="right">
    <a href="lesson2_5_context_bounds.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_7_recursive_implicit_resolution.md">Next →</a>
</h4>