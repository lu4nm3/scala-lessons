<h4 align="right">
    <a href="lesson1_2_conversions.md">← Previous</a> |
    <a href="lesson1.md">Menu</a> |
    <a href="lesson1_4_nested_conversions.md">Next →</a>
</h4>

<h1>Implicit Classes</h1>

Implicit classes are a Scala language feature that allows us to define extra functionality on existing data types 
without using conventional inheritance. This is known as `type enrichment`.

You can think of implicit classes as being syntactic sugar for the combination of a regular class and an implicit 
conversion. In other words, the previous example:

```scala
import scala.language.implicitConversions

class Greet(name: String) {
  def hello: String = s"hello $name!"
}

implicit def stringToGreet(s: String): Greet = new Greet(s)
```

Can be instead re-written as:

```scala
implicit class Greet(name: String) {
  def hello: String = s"hello $name!"
}
```

And used the same way.

```scala
scala> "bob".hello
res0: String = hello bob!
```

<h4 align="right">
    <a href="lesson1_2_conversions.md">← Previous</a> |
    <a href="lesson1.md">Menu</a> |
    <a href="lesson1_4_nested_conversions.md">Next →</a>
</h4>