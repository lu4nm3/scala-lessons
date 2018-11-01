<h4 align="right">
    <a href="lesson2_6_implicit_scope_packaging.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_3_functional_programming_basics/lesson3.md">Next →</a>
</h4>

<h1>Recursive Implicit Resolution</h1>

Part of the power of type classes and implicits lies in the compiler's ability to combine implicit definitions when 
searching for candidate instances.

So far, we have been defining implicit instances as `implicit val`s.

```scala
implicit val croissantPerishable: Perishable[Croissant] = new Perishable[Croissant] {
  def expired(c: Croissant): Boolean = false
}
```

But we can also define them as implicit methods that can be used to construct instances from other type class instances.

As an example, consider defining an instance of `Perishable` for a `List` of baked goods that determines whether or not
all of the items in the list have expired. For this, we would need a `Perishable[List[T]]` for every item `T` that the
bakery sells.  

```scala
implicit val listBaguettePerishable: Perishable[List[Baguette]] = new Perishable[List[Baguette]] {
  def expired(b: List[Baguette]): Boolean = false // hard coded, in reality would use `b` to figure out if it expired
}

implicit val listCroissantPerishable: Perishable[List[Croissant]] = new Perishable[List[Croissant]] {
  def expired(c: List[Croissant]): Boolean = false
}
  
implicit val listIceCreamPerishable: Perishable[List[IceCream]] = new Perishable[List[IceCream]] {
  def expired(i: List[IceCream]): Boolean = true
}
```

However, this approach doesn't scale as now we end up requiring 2 `val`s for every type `T` (one val for the type itself 
and one `val` for `List[T]`). We would also be duplicating a lot of logic.

What we can do instead is to abstract the code for handling `List[T]` into a common method based on an instance for `T`. 
This method constructs an `Perishable` instance for `List[T]` by relying on an implicit parameter to fill in the
T-specific functionality.

```scala
implicit def listPerishable[T](implicit p: Perishable[T]): Perishable[List[T]] = new Perishable[List[T]] {
  def expired(items: List[T]): Boolean = {
    if (items.isEmpty) false
    else items.map(item => p.expired(item)).forall(item => item)
  }
}
```

Given our previous interface object:

```scala
object Perishable {
  def apply[T](implicit perishable: Perishable[T]): Perishable[T] = perishable
}
```

When the compiler sees an expression like:

```scala
Perishable[List[Baguette]].expired(List(Baguette(), Baguette()))
```

it searches for an implicit `Perishable[List[Baguette]]` and finds our implicit method `listPerishable` (which we
defined for items of type `Perishable[List[T]]`):

```scala
Perishable[List[Baguette]](listPerishable[Baguette]).expired(List(Baguette(), Baguette()))
```

and recursively searches for an instance of `Perishable[Baguette]` from our imported instances to use as the parameter
for `listPerishable[Baguette]`:

```scala
import Perishables._

Perishable[List[Baguette]](listPerishable[Baguette](baguettePerishable)).expired(List(Baguette(), Baguette()))
```

<h4 align="right">
    <a href="lesson2_6_implicit_scope_packaging.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_3_functional_programming_basics/lesson3.md">Next →</a>
</h4>