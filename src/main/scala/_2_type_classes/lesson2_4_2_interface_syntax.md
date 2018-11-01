<h4 align="right">
    <a href="lesson2_4_1_interface_objects.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_5_context_bounds.md">Next →</a>
</h4>

<h1>Interface Syntax</h1>

Alternatively we can use extension methods to extend existing types with functionality from our type class. We do this 
by using implicit classes like we saw in an earlier lesson:

```scala
object PerishableSyntax {

  implicit class PerishableOps[T](item: T) {
    def expired(implicit perishable: Perishable[T]): Boolean = {
      perishable.expired(item)
    }
  }

}
```

This implicit class will expose the `expired` method to any type `T` only if there is an implicit instance
`Perishable[T]` for that type in scope.

We then use the interface syntax by importing the implicit class along with the type class instances for the types we 
need:

```scala
@ import Perishables._
import Perishables._

@ import PerishableSyntax._
import PerishableSyntax._

@ Baguette().expired
res0: Boolean = false
```

Again, the compiler searches for candidates for the implicit parameters and fills them in for us:

```scala
Baguette().expired(baguettePerishable)
```

<h4 align="right">
    <a href="lesson2_4_1_interface_objects.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_5_context_bounds.md">Next →</a>
</h4>