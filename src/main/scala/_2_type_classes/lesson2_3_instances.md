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
case class Baguette()
case class Croissant()
case class IceCream()
```

We can define instances for our `Perishable` type class:

```scala
trait Perishable[T] {
  def expired(item: T): Boolean
}

object Perishables {
  implicit val baguettePerishable: Perishable[Baguette] = new Perishable[Baguette] {
    def expired(b: Baguette): Boolean = false // hard coded, in reality would use `b` to figure out if it expired
  }

  implicit val croissantPerishable: Perishable[Croissant] = new Perishable[Croissant] {
    def expired(c: Croissant): Boolean = false
  }
  
  implicit val iceCreamPerishable: Perishable[IceCream] = new Perishable[IceCream] {
    def expired(i: IceCream): Boolean = true
  }
}
```

<h4 align="right">
    <a href="lesson2_2_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_4_1_interface_objects.md">Next →</a>
</h4>