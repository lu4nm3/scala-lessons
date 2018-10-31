<h4 align="right">
    <a href="lesson1_3_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_2_type_classes/lesson2.md">Next →</a>
</h4>

<h1>Nested Implicit Conversions</h1>

We have seen implicit conversions from one type to another:

```scala
import scala.language.implicitConversions

case class Dog() {
  def bark: String = "woof!"
}

case class Cat() {
  def meow: String = "meow"
}

case class Sheep() {
  def baa: String = "baa!"
}

implicit def dogToCat(dog: Dog): Cat = Cat()
implicit def catToSheep(cat: Cat): Sheep = Sheep()
```

```scala
@ Dog().meow
res0: String = "meow"

@ Cat().baa
res1: String = "baa!"
```

However, just because we have one conversion from `Dog` to `Cat` and another from `Cat` to `Sheep` does not mean that we 
can convert an instance of `Dog` directly to an instance of `Sheep` (in other words, dogs won't be able to baa):

```scala
@ Dog().baa
<console>:17: error: value baa is not a member of Dog
       Dog().baa
             ^
```

We can get around this by supplying an implicit parameter to the conversion methods:

```scala
implicit def dogToCat[D](dog: D)(implicit f: D => Dog): Cat = Cat()
implicit def catToSheep[C](cat: C)(implicit f: C => Cat): Sheep = Sheep()
```

```scala
@ Dog().baa
res3: String = "baa!"
```

Here the compiler will first search the implicit scope to see if there is an implicit conversion from `Dog` to another 
type that has the method `baa` (ie. in this case, that type would be `Sheep`). Such a conversion doesn't exist. HOWEVER,
there is an implicit conversion between the generic type `C` and type `Cat` so the compiler will explore if it can use
it somehow: 

```scala
catToSheep(Dog())(...)
```

Next, the compiler will search the implicit scope for the implicit function `C => Cat`. Here we said that `C` is our 
type `Dog` and so it finds that the function `dogToCat` meets these requirements:

```scala
catToSheep(Dog())(dogToCat(_)(...))
```

You might be wondering what implicit function `D => Dog` is used for `dogToCat`. Since `D` is our type `Dog`, it means
that the implicit function that we need will look like `Dog => Dog`. This is also known as the `identity` function which
is automatically provided by the Scala standard library so the compiler uses that:

```scala
catToSheep(Dog())(dogToCat(_)(identity))
```

This approach of doing nested implicit conversions also works for implicit classes:

```scala
case class Dog() {
  def dog: String = "bark!"
}

implicit class Cat[D](dog: D)(implicit f: D => Dog) {
  def meow: String = "meow"
}

implicit class Sheep[D, C](cat: C)(implicit f: C => Cat[D]) {
  def baa: String = "baa!"
}
```

```scala
@ Dog().baa
res0: String = "baa!"
```

<h4 align="right">
    <a href="lesson1_3_classes.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_2_type_classes/lesson2.md">Next →</a>
</h4>
