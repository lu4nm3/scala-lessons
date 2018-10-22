# Functions

Scala has first-class support for functions.

### Methods

Methods are like functions except that they operate on an `object`, `class`, etc:

```scala
class Foo {
  def bar(i: Int): String = i.toString
}
```

### Functions

Functions, on the other hand, aren't necessarily bound to anything and can be declared in variables just like any
regular value:

```scala
val bar = (i: Int) => i.toString
```

Functions themseleves have types. If you type the function above into the REPL, you can see its type value:

```scala
@ val bar = (i: Int) => i.toString
bar: Int => String
```

### Higher-Order Functions

Higher order functions are functions that either take another function as an input:

```scala
def foo(f: String => Int): Int
```

Or that produce a function as a result:

```scala
def bar(i: Int): String => Int
```

### Pure Functions

Pure functions are functions that have no side-effects. This means that in an expression, you should be able to
substitute a function call for the actual result of the function and the expression's evaluation will remain unchanged.
A function with no side-effects is also said to be referentially transparent.

For example, here is a function with no side-effects:

```scala
def add(a: Int, b: Int): Int = {
  a + b
}
```

This means that if we use it in an expression, we could swap it out for the result of the function and evaluating the
expression would remain the same:

```scala
@ 2 * add(1, 3)
res1: Int = 8

@ 2 * 4
res2: Int = 8
```

Substituting a function with side-effects on the other hand:

```scala
def add(a: Int, b: Int): Int = {
  println("a: " + a)
  a + b
}
```

would not result in the same expression evaluation:

```scala
@ 2 * add(1, 3)
a: 1
res4: Int = 8

@ 2 * 4
res5: Int = 8
```

Function purity is important because it guarantees deterministic execution of your logic. Without this guarantee,
reasoning about your code becomes hard, as does testing, refactoring, etc...