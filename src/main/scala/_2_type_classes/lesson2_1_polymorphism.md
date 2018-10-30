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

For example, let's say that we're designing an accounting application for a chain of supermarkets and we needed to track
costs. Now you can imagine that the costs for a supermarket come from many different places such as the cost of the food
it sells, the shipping costs to deliver merchandise, and the costs from operating brick and mortar stores, to name a
few.

In this case, we need to implement a common behavior (calculating costs) across multiple domains (merchandise, shipping,
physical locations). Naturally, from the name of this section, ad-hoc polymorphism will be the way to go. But first,
let's explore some of our other alternatives if ad-hoc polymorphism was not an option.

<h3>Subtype Polymorphism</h3>

One approach would be to use subtype polymorphism as we saw earlier by defining a top-level `Cost` type that the types
of our different sub-domains can inherit from:

```scala
trait Cost {
  def calculateCosts: Double
}

// merchandise
case class Apple() extends Cost {
  def calculateCosts: Double = ???
}

// shipping
case class Truck() extends Cost {
  def calculateCosts: Double = ???
}

// physical location
case class Store() extends Cost {
  def calculateCosts: Double = ???
}
```

Then in one of the services in our application, we could use this top-level type to calculate all of the costs across
the company:

```scala
trait AccountingService {
  def totalCosts(costs: List[Cost]): Double = ???
}
```

This approach presents a few different problems:

  - Having a common super type for `Cost` that all of our sub-domain types have to inherit from is a little awkward and
  does not make a lot of sense from a domain modeling perspective. Normally we use inheritance to denote an **is a**
  relationship. In this case, however, we are saying that an apple **is a** cost when it's probably more accurate to say
  that an apple **has a** cost.
  - With subtype polymorphism we lose type information that might have otherwise been useful. In our example, we need to
  calculate the costs for the entire company so we parameterize our `totalCosts` method on a type that contains cost
  information. The only common type across our company's domain model that we can use for this is our top-level `Cost`
  type. By doing so, however, we lose context about what it is exactly that we are calculating the costs for.
  - We are closing our API against future extension to types that we don't control. For instance, imagine we decided to
  use some 3rd party library for managing employees that provided us with an `Employee` class. In this case, we would
  not be able to calculate costs related to payroll by extending the class with `Cost` since `Employee` belongs to
  source code we don't have access to.
  
<h3>Adapter Pattern</h3>

Another option would be to use the adapter pattern. The adapter pattern is used to make existing types work with others
by wrapping them in a new interface. 

```scala
trait CostLike[A] {
  def calculateCosts: Double
}

case class CostLikeApple(a: Apple) extends CostLike[Apple] {
  def calculateCosts: Double = ???
}

case class CostLikeTruck(t: Truck) extends CostLike[Truck] {
  def calculateCosts: Double = ???
}

case class CostLikeStore(s: Store) extends CostLike[Store] {
  def calculateCosts: Double = ???
}

def totalCosts[A](costs: List[CostLikeApple[A]]): Double = costs.map(_.calculateCosts).sum
```

While this approach gives us full type safety and extensibility, it does come with its downsides:

  - By wrapping each of our domain objects inside a `CostLike[A]` type, we incur the performance hit of having to
  allocate extra memory. The larger the list of domain objects that we need to calculate costs for, the more adapter
  instances we have to create.
  - While our `CostLike` adapter currently meets our needs for calculating costs, it is possible that in the future, we
  have to implement another set of cross-domain behaviors. For example, we might want to calculate the total amount of
  taxes paid across the company. At this point we have 2 options:
    - We can create a new adapter type for each cross-domain behavior we need to support and somehow deal with ever
    increasing memory penalties.
    - Or we can redefine `CostLike` into a more generic `AccountingLike` adapter where we define all of the common
    behaviors we need to support. This tight coupling, however, will lead to bloated adapters that make behaviors hard
    to compose, maintain, and test.

Type classes solve these problems.

<h4 align="right">
    <a href="../_1_implicits/lesson1.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_2_classes.md">Next →</a>
</h4>