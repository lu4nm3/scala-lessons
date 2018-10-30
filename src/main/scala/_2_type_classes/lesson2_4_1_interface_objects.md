<h4 align="right">
    <a href="lesson2_3_instances.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_4_2_interface_syntax.md">Next →</a>
</h4>

<h1>Interface Objects</h1>

<h3>Object methods</h3>

The simplest way of creating an interface to a type class is to create methods in a singleton object to expose specific 
functionality from your type class:

```scala
object CostCalculator {
  def calculateCost[T](thing: T)(implicit cost: Cost[S]): Double = {
    cost.calculate(thing)
  }
}
```

To use this object, we import any type class instances we care about and call the relevant method:

```scala
@ import Costs._
import Costs._

@ CostCalculator.calculateCost(Truck())
res0: Double = 180000.0
```

Here the compiler notices that we called the `calculateCost` method without providing an implicit parameter. So it tries 
to fix this by searching for the type class instance of the relevant type and inserting it at the call site:

```scala
CostCalculator.calculateCost(Truck())(truckCost)
```

<h3>Summoner method</h3>

If you're creating individual object methods for **ALL** the functionality of a type class, then we can alternatively 
avoid the indirection of having the object methods call the type class methods and just expose the type class
directly.

This commonly done by defining what is known as a "summoner" method in the companion object of the type class itself. 
This summoner is just a 0-argument `apply` method:

```scala
object Cost {
  def apply[T](implicit calculator: Cost[T]): Cost[T] = calculator
}
```

And then we can call this method by simply passing in the type that we're interesting in using. Remember that the type
class instance for that type also has to be in scope:

```scala
@ import Costs._
import Costs._

@ Cost[Truck].calculate(Truck())
res0: Double = 180000.0
```

This is very similar to how the `implicitly` method from the Scala standard library is defined.

```scala
def implicitly[T](implicit e: T): T = e
```

```scala
@ implicitly[Cost[Truck]].calculate(Truck())
res1: Double = 180000.0
```

<h4 align="right">
    <a href="lesson2_3_instances.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_4_2_interface_syntax.md">Next →</a>
</h4>