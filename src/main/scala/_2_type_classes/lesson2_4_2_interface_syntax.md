<h4 align="right">
    <a href="lesson2_4_1_interface_objects.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_5_context_bounds.md">Next →</a>
</h4>

<h1>Interface Syntax</h1>

Alternatively we can use extension methods to extend existing types with functionality from our type class. We do this 
by using implicit classes like we saw in an earlier lesson:

```scala
object CostSyntax {

  implicit class CostOps[T](thing: T) {
    def cost(implicit calculator: Cost[T]): Double = {
      calculator.calculate(thing)
    }
  }

}
```

This implicit class will expose the `cost` method to any type `T` only if there is an implicit instance `Cost[T]` for 
that type in scope.

We then use the interface syntax by importing the implicit class along with the type class instances for the types we 
need:

```scala
@ import Costs._
import Costs._

@ import CostSyntax._
import CostSyntax._

@ Truck().cost
res0: Double = 180000.0
```

Again, the compiler searches for candidates for the implicit parameters and fills them in for us:

```scala
Truck().cost(truckCost)
```

<h4 align="right">
    <a href="lesson2_4_1_interface_objects.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_5_context_bounds.md">Next →</a>
</h4>