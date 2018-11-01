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

  1. by placing them in an object like `Perishables`
  2. by placing them in a trait
  3. by placing them in the companion object of the type class
  4. by placing them in the companion object of the type parameter

To demonstrate the different ways we can package type class instances, let's suppose we want to call the following 
method:

```scala
def expired(baguette: Baguette)(implicit p: Perishable[Baguette]): Boolean = {
  p.expired(baguette)
}
```

<h4>Define instances in an object</h4>

With option 1, we bring instances into scope by importing them like we have been doing so far:

```scala
object Perishables {
  implicit val baguettePerishable: Perishable[Baguette] = new Perishable[Baguette] {
    def expired(b: Baguette): Boolean = false // hard coded, in reality would use `b` to figure out if it expired
  }
}
```

```scala
@ import Perishables._
import Perishables._

@ expired(Baguette())
res0: Boolean = false
```

<h4>Inherit instances</h4>

With option 2, we bring instances into scope through inheritance:

```scala
trait BaguetteExpiration {
  implicit val baguettePerishable: Perishable[Baguette] = new Perishable[Baguette] {
    def expired(b: Baguette): Boolean = false
  }
}
```

```scala
@ object Service extends BaguetteExpiration {
    private def expired(baguette: Baguette)(implicit p: Perishable[Baguette]): Boolean = {
      p.expired(baguette)
    }

    def getBaguetteExpiration(baguette: Baguette): Boolean = expired(baguette)
  }
defined object Service

@ Service.getBaguetteExpiration(Baguette())
res0: Boolean = false
```

<h4>Define instances in companion objects</h4>

With options 3 and 4, instances are always in implicit scope:

```scala
// You can define type class instances in the companion object of the type class itself
object Perishable {
  implicit val baguettePerishable: Perishable[Baguette] = new Perishable[Baguette] {
    def expired(b: Baguette): Boolean = false
  }
}

// Or alternatively, you can define type class instances in the companion object of the
// particular type that you want to provide a type class instance for
object Baguette {
  implicit val baguettePerishable: Perishable[Baguette] = new Perishable[Baguette] {
    def expired(b: Baguette): Boolean = false
  }
}
```

No explicit imports required:

```scala
@ expired(Baguette())
res0: Boolean = false
```

<h3>Default instances</h3>

When packaging type class instances, if: 
  
  1. there is only a single instance for a type or 
  2. a single good default for a type
  
then put it in the companion object of the type if possible. This allows users to override the instance by defining one 
in the local scope while still providing sensible default behavior.

```scala
object IceCream {
  implicit val iceCreamPerishable: Perishable[IceCream] = new Perishable[IceCream] {
    def expired(i: IceCream): Boolean = true
  }
}
```

Otherwise, we can place different type class instances into their own objects and require the users to explicitly import 
them based on what they need. For example, we might want to differentiate between water-based ice cream like sorbets and
ice cream made with milk as they will have different shelf lives. To do this, we can create 2 separate objects to
contain different expiration behaviors:

```scala
object WaterBasedIceCream {
  implicit val iceCreamPerishable: Perishable[IceCream] = new Perishable[IceCream] {
    def expired(i: IceCream): Boolean = false
  }
}

object MilkBasedIceCream {
  implicit val iceCreamPerishable: Perishable[IceCream] = new Perishable[IceCream] {
    def expired(i: IceCream): Boolean = true
  }
}
```

And then simply import the specific implementation that we're interested in when we need it:

```scala
@ import WaterBasedIceCream._
import WaterBasedIceCream._

@ Perishable[IceCream].expired(IceCream())
res0: Boolean = false
```

```scala
@ import MilkBasedIceCream._
import MilkBasedIceCream._

@ Perishable[IceCream].expired(IceCream())
res1: Boolean = true
```

<h4 align="right">
    <a href="lesson2_5_context_bounds.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_7_recursive_implicit_resolution.md">Next →</a>
</h4>