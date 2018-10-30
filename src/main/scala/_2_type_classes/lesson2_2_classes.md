<h4 align="right">
    <a href="lesson2_1_polymorphism.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_3_instances.md">Next →</a>
</h4>

<h1>The Type Class</h1>

Type classes are a programming pattern that originated in Haskell. They allow us to extend existing code with new 
functionality **without** using inheritance and **without** altering the original source code.

There are 3 main components to the type class pattern: 

  1. the type class itself
  2. instances for particular types, and 
  3. the interface that we expose to users.

A type class is an interface that represents some behavior or functionality we want to implement. 

Continuing from our supermarket example, `Cost` will be our type class:

```scala
trait Cost[T] {
  def calculate(thing: T): Double
}
```

<h4 align="right">
    <a href="lesson2_1_polymorphism.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson2_3_instances.md">Next →</a>
</h4>