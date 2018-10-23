<h4 align="right">
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson0_2_functions.md">Next →</a>
</h4>

# Super Basics

### Automatic type inference

Scala automatically infers types (ie. you don't have to explicitly declare them):

```scala
@ val str = "abc"
str: String = "abc"

@ val i = 3
i: Int = 3
```

### Statements vs. Expressions

Normally in other languages like Java, there is a difference between expressions:

```java
// expression

b + 1

// statements

a = b + 1

System.out.println("Hello world!")
```

In Scala, on the other hand, pretty much everything is an expression:

```scala
// println is an expression that returns Unit
@ val p: Unit = println("Hello world!")
Hello world!


@ val a = 3
a: Int = 3

@ val b = 4
b: Int = 4

// if-statements are expressions that return values based on their evaluation
@ val i: Int = if (a > b) a else b
i: Int = 4


// using match also results in an expression that returns a value
@ val m = i match {
    case 1 => "one"
    case 2 => "two"
    case _ => "other"
  }
m: String = "other"


// even try-catch blocks are expressions
@ val result = try {
      "1".toInt
  } catch {
      case _ => 0
  }
result: Int = 1
```

A block can contain a sequence of expressions where the result is also an expression. The value of the block is the
value of the last expression:

```scala
@ val foo = {
    val a = 1
    val b = 2
    val c = 3
    a + b + c
  }
foo: Int = 6
```

### `val` vs. `var`

Variables declared with `val`s are immutable (which means you can't change their value after you declare them):

```scala
@ val x = 3
x: Int = 3

@ x = 2
cmd8.sc:1: reassignment to val
val res8 = x = 2
             ^
Compilation Failed
```

Which is like using `final` in Java:

```scala
public final int x = 3;
```

And those declared with `var`s are mutable:

```scala
@ var x = 3
x: Int = 3

@ x = 2

@ x
res10: Int = 2
```

In general, you should opt for immutability.

### Operators are methods

```scala
a + b
```

is shorthand for:

```scala
a.+(b)
```

<h4 align="right">
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson0_2_functions.md">Next →</a>
</h4>