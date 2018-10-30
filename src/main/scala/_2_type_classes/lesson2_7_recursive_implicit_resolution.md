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
implicit val appleCost: Cost[Apple] = new Cost[Apple] {
  def calculate(apple: Apple): Double = 0.25
}
```

But we can also define them as implicit methods that can be used to construct instances from other type class instances.

As an example, consider defining an instance of `Cost` for a `List` of fruits that adds the costs of all the fruits in 
the list. We would need a `Cost[List[T]]` for every type of fruit `T` that we care about.

```scala
sealed trait Fruit
case class Apple() extends Fruit
case class Orange() extends Fruit

implicit val listAppleCost: Cost[List[Apple]] = new Cost[List[Apple]] {
  def calculate(apples: List[Apple]): Double = ???
}

implicit val listOrangeCost: Cost[List[Orange]] = new Cost[List[Orange]] {
  def calculate(oranges: List[Orange]): Double = ???
}
```

However, this approach doesn't scale as now we end up requiring 2 `val`s for every type `T` (one val for the type itself 
and one `val` for `List[T]`). We would also be duplicating logic.

What we can do instead is to abstract the code for handling `List[T]` into a common method based on an instance for `T`. 
This method constructs an `Cost` instance for `List[T]` by relying on an implicit parameter to fill in the T-specific 
functionality.

```scala
implicit def listCost[T](implicit cost: Cost[T]): Cost[List[T]] = new Cost[List[T]] {
  def calculate(things: List[T]): Double = {
    if (things.isEmpty) 0
    else things.map(cost.calculate).sum
  }
}
```

Given our previous interface object:

```scala
object Cost {
  def apply[T](implicit calculator: Cost[T]): Cost[T] = calculator
}
```

When the compiler sees an expression like:

```scala
Cost[List[Apple]].calculate(List(Apple(), Apple()))
```

it searches for an implicit `Cost[List[Apple]]`. It finds the implicit method for `Cost[List[T]]`:

```scala
Cost[List[Apple]](listCost[Apple]).calculate(List(Apple(), Apple()))
```

and recursively searches for an `Cost[Apple]` to use as the parameter to `listCost[Apple]`:

```scala
import Costs._

Cost[List[Apple]](listCost[Apple](appleCost)).calculate(List(Apple(), Apple()))
```

<h4 align="right">
    <a href="lesson2_6_implicit_scope_packaging.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_3_functional_programming_basics/lesson3.md">Next →</a>
</h4>