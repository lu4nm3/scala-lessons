<h4 align="right">
    <a href="lesson2_2_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_4_1_interface_objects.md">Next →</a>
</h4>

<h1>Instances</h1>

The instances of a type class provide implementations for the types that we care about. These types can include not only 
your own custom types but also types from the Scala standard library as well as types defined in 3rd-party libraries.

In Scala we define instances by creating concrete implementations of the type class and tagging them with the `implicit` 
keyword.

Using some of the types from our supermarket domain:

```scala
case class Apple()
case class Truck()
case class Store()
```

We can define instances for our `Cost` type class:

```scala
trait Cost[T] {
  def calculate(thing: T): Double
}

object Costs {
  implicit val appleCost: Cost[Apple] = new Cost[Apple] {
    def calculate(apple: Apple): Double = 0.25
  }

  implicit val truckCost: Cost[Truck] = new Cost[Truck] {
    def calculate(truck: Truck): Double = 180000.0
  }
  
  implicit val storeCost: Cost[Store] = new Cost[Store] {
    def calculate(store: Store): Double = 10000000.0
  }
}
```

<h4 align="right">
    <a href="lesson2_2_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_4_1_interface_objects.md">Next →</a>
</h4>