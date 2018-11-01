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
object BakeryService {
  def expired[T](thing: T)(implicit perishable: Perishable[T]): Boolean = {
    perishable.expired(perishable)
  }
}
```

To use this object, we import any type class instances we care about and call the relevant method:

```scala
@ import Perishables._
import Perishables._

@ BakeryService.expired(Baguette())
res0: Boolean = false
```

Here the compiler notices that we called the `expired` method without providing an implicit parameter. So it searches
the implicit scope for a type class instance for the `Baguette` type and it inserts it at the call site:

```scala
BakeryService.expired(Baguette())(baguettePerishable)
```

<h3>Summoner method</h3>

If you're creating individual object methods for **ALL** the functionality of a type class, then you can alternatively 
avoid the indirection of having the object methods call the type class methods and just expose the type class directly.

This commonly done by defining what is known as a "summoner" method in the companion object of the type class itself. 
This summoner is just a 0-argument `apply` method:

```scala
object Perishable {
  def apply[T](implicit perishable: Perishable[T]): Perishable[T] = perishable
}
```

And then we can call this method by simply passing in the type that we're interesting in using it with. Remember that
the type class instance for that type also has to be in scope:

```scala
@ import Perishables._
import Perishables._

@ Perishable[Baguette].expired(Baguette())
res0: Boolean = false
```

This is very similar to how the `implicitly` method from the Scala standard library is defined.

```scala
def implicitly[T](implicit e: T): T = e
```

```scala
@ implicitly[Perishable[Baguette]].expired(Baguette())
res1: Boolean = false
```

<h4 align="right">
    <a href="lesson2_3_instances.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_4_2_interface_syntax.md">Next →</a>
</h4>