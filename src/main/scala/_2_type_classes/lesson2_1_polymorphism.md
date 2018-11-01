<h4 align="right">
    <a href="../_1_implicits/lesson1.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_2_classes.md">Next →</a>
</h4>

<h1>Polymorphism</h1>

The idea behind polymorphism is to increase code reuse by writing it in a more generic fashion. There are 3 main forms
of polymorphism that we can use:

  - subtype polymorphism (inheritance)
  - parametric polymorphism (generics)
  - ad-hoc polymorphism (type classes)

<h2>Subtype Polymorphism</h2>

Subtype polymorphism is the polymorphism that you get from inheritance as you know it from object oriented programming. 

More formally, subtype polymorphism allows a function to take an argument of type `T` but will also work when given an
argument of type `S` that is a subtype of `T`. This is also known as the _Liskov substitution_ principle.

For example: 

```scala
trait Animal {
  def sound: String
}

case class Dog() extends Animal {
  def sound: String = "woof!"
}

case class Sheep() extends Animal {
  def sound: String = "meh!"
}

def speak(animal: Animal): Unit = {
  println(animal.sound)
}
```

With subtype polymorphism, we are able to treat entire groups of types (ie. subtypes) the same since they all implement
the same behaviors, despite having different implementations of these behaviors. That is to say, `Dog`s and `Sheep` can
both make a "`sound`" because all `Animal`s make a sound even if the sound a `Dog` makes is not the same as a `Sheep`'s. 

<h2>Parametric Polymorphism</h2>

Parametric polymorphism allows a function or data type to be written generically so that it can handle values uniformly
without depending on their type.

For example, say we want to implement a custom `List` data type:

```scala
trait List {
  def add(value: ???): List
  def get(index: Int): ???
}
``` 

What type can we use for our values (shown as `???`)? One option could be to use a top-level type like `Any` (which is
similar to Java's `Object` type):

```scala
trait List {
  def add(value: Any): List
  def get(index: Int): Any
}
```

The downside with this approach is that we need to do a lot of runtime type checking and casting to make it work:

```scala
val ints = new List() // assuming List was implemented
ints.add(1)
ints.add(3)

@ ints.get(0)
res0: Any = 1

@ ints.get(0).asInstanceOf[Int]
res1: Int = 1
```

Another option would be to define separate versions of our list for each type that we want to support:

```scala
trait IntList {
  def add(value: Int): List
  def get(index: Int): Int
}

trait StringList {
  def add(value: String): List
  def get(index: Int): String
}
```

This can quickly get out of hand as it involves a lot of boilerplate and duplicate code that isn't really different
across types since we're not implementing any type-specific behavior; all we are doing is adding and removing elements
to a list.

Instead, we can use parametric polymorphism to abstract the type of our `List` and treat it as a template:

```scala
trait List[A] {
  def add(value: A): List[A]
  def get(index: Int): A
}
```

This allows us to reduce boilerplate by writing code that can work uniformly over a range of different types such as
`Int`s:

```scala
val ints = new List[Int]()
ints.add(1)
ints.add(3)

@ ints.get(0)
res0: Int = 1
```

and `Long`s:

```scala
val longs = new List[Long]()
longs.add(1)
longs.add(3)

@ longs.get(0)
res1: Long = 1
```

<h2>Ad-hoc Polymorphism</h2>

What if we needed to implement some behavior across different unrelated types (in terms of subtyping) where the
implementation of the behavior is specific to the type?

For example, let's say that we're designing an application for a bakery to help it figure out the shelf life of its
bread and other pastries. Shelf life for baked goods typically depends on things like the day they were made, the
ingredients that were used, whether or not things were refrigerated, etc. 

In this case we seek to implement a common behavior (calculating the expiration date) across different baked goods so
that the bakery knows when it needs to make new batches. Naturally, from the name of this section, ad-hoc polymorphism
will be the way to go. But first, let's explore some of our other alternatives.

<h3>Subtype Polymorphism</h3>

One approach would be to use subtype polymorphism as we saw earlier by defining a top-level `Perishable` type that the
types representing our different baked goods can inherit from:

```scala
trait Perishable {
  def expired: Boolean
}

case class Baguette() extends Perishable {
  def expired: Boolean = ???
}

case class Croissant() extends Perishable {
  def expired: Boolean = ???
}
```

Then from one of the services in our application, we could use this top-level `Perishable` type to find all of the bread
that has expired so that it can be replaced:

```scala
trait DailyOrderService {
  def needsReplacing(perishables: List[Perishable]): List[Perishable] = {
    perishables.filter(_.expired)
  }
}
```

This approach has a couple of problems:

  - With subtype polymorphism we lose type information that might have otherwise been useful. For example, if we pass in
  a list of `Baguette`s to our `needsReplacing` function, we get back a list of `Perishable`s. In other words we lose
  context about the type of baked goods that expired and we no longer know if we need to get rid of the baguettes or the
  croissants.
  - We close our API to future extension for types that we don't control. For example, imagine that one day the bakery
  enters into a partnership with an ice-cream shop and expands its menu with some of their ice-cream. As part of the
  deal, the ice-cream shop provides the bakery's developers with a custom Scala library (compiled in a jar) for managing
  their product which includes an `IceCream` type. By using subtype polymorphism to model perishable food, we would not
  be able to calculate the expiration for ice-cream since `IceCream` belongs to source code that we don't have access
  to.
  
<h3>Adapter Pattern (ie. Parametric Polymorphism)</h3>

Another option would be to use the adapter pattern. The adapter pattern is used to make existing types work with others
by wrapping them in a new interface. 

```scala
trait PerishableLike[A] {
  def expired: Boolean
}

case class PerishableLikeBaguette(b: Baguette) extends PerishableLike[Baguette] {
  def expired: Boolean = ???
}

case class PerishableLikeCroissant(c: Croissant) extends PerishableLike[Croissant] {
  def expired: Boolean = ???
}

case class PerishableLikeIceCream(i: IceCream) extends PerishableLike[IceCream] {
  def expired: Boolean = ???
}
```

Then, we can parameterize our function on some type `A` and use a list of `PerishableLike[A]` as both its input
parameter type and its return type:

```scala
def needsReplacing[A](perishables: List[PerishableLike[A]]): List[PerishableLike[A]] = {
  perishables.filter(_.expired)
}
```

While this approach gives us full type safety and extensibility (we can even use it with that `IceCream` type we got
from the ice-cream shop), it does come with its downsides:

  - By wrapping each of our domain objects inside a `PerishableLike[A]` type, we incur the performance hit of having to
  allocate extra memory. The larger the list of domain objects that we need to find the expiration for, the more adapter
  instances we have to create.
  - While our `PerishableLike` adapter currently meets our needs for calculating costs, it is possible that in the
  future, we have to implement another set of cross-domain behaviors. For example, we might want to categorize our menu
  by figuring out which items meet certain conditions such as being gluten free. At this point we have 2 options:
    - We can create a new adapter type for each behavior that we need to support and deal with ever increasing memory
    costs.
    - Or we can redefine `PerishableLike` into a more generic `FoodLike` adapter where we define all of the common
    behaviors we need to support. This tight coupling, however, will lead to bloated adapters that make behaviors hard
    to compose, maintain, and test.

Type classes solve these problems.

<h4 align="right">
    <a href="../_1_implicits/lesson1.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_2_classes.md">Next →</a>
</h4>