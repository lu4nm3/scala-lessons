<h4 align="right">
    <a href="lesson0_4_classes_traits_objects.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_1_implicits/lesson1.md">Next →</a>
</h4>

# Pattern Matching

You can think of Scala's pattern matching as Java's switch case statement on steroids.

In Java, switch statements work only with primitive data types, enums, and wrapper classes (eg. `Integer`, `Boolean`,
etc).

```java
String result = "";
int num = 3;

switch(num) {
    case 1:
        result = "odd";
        break;
    case 2: 
        result = "even";
        break;
    case 3: 
        result = "odd";
        break;
    case 4:
        result = "even";
        break;
    default:
        result = "unknown";
        break;
}
```

In Scala, pattern matching doesn't have the "fall through" problem from Java where each case needs to end with `break`.
In addition, match statements return a value which means you can assign their result to a variable:

```scala
@ val num = 3
num: Int = 3

@ val result = num match {
    case 1 => "odd"
    case 2 => "even"
    case 3 => "odd"
    case 4 => "even"
    case _ => "unknown"   // _ is a "catch all" which is the equivalent of the default case in Java
  }
result: String = "odd"
```

If an expression does not match any of the cases, a `MatchError` gets thrown. Because of this, it's important to define
a catch call `_` (unless you're dealing with ADTs, see section below).

### `|` operator

In pattern matching, you can use `|` to separate multiple alternatives:

```scala
num match {
  case 1 | 3 => "odd" 
  case 2 | 4 => "even"
  case _ => "unknown"
}
```

### Guards

Pattern matching supports guards which allow you to define additional conditions that a case must meet:

```scala
val flag = true

num match {
  case 1 | 3 if flag => "odd"
  case 2 | 4 => "even"
  case _ => "unknown"
}
```

### Variables

If the `case` is followed by a variable name, then the match expression is assigned to that variable:

```scala
num match {
  case 1 | 3 => "odd"
  case num if num <= 4 => "even"
  case unknown => s"unknown number: $unknown"
}
```

### Types

You can match on the type of an expression (in Scala this is preferred over using the `isInstanceOf` method):

```scala
obj match {
  case i: Int => "int"
  case l: Long => "long"
  case s: String => "string"
  case _ => "unknown"
}
```

If the variable name isn't used, you can omit it using `_` instead:

```scala
obj match {
  case i: Int => s"$i is a int"
  case _: Long => "long"
  case _: String => "string"
  case _ => "unknown"
}
```

### Case classes

We can pattern match case classes in order to get to their contents:

```scala
case class Square(length: Int)

val s = Square(3)

s match {
  case Square(len) => s"the length of the square is $len"
}
```

Alternatively, if we wanted to use the case class itself and don't care about its contents, we can substitute each of
its parameters with `_` and use a variable to refer to the instance:

```scala
s match {
  case s: Square(_) => ...
}
```

Or we can use both:

```scala
s match {
  case s: Square(len) => ...
}
```

#### Algebraic data types (ADTs)

Earlier we saw how we can create algebraic data types by defining all possible instances of a type using the `sealed`
keyword:

```scala
sealed trait Shape
case class Square(length: Int) extends Shape
case class Circle(radius: Int) extends Shape
case class Triangle(base: Int, height: Int) extends Shape
```

We can use the fact that a type is sealed to pattern match against all of its subtypes:

```scala
val shape: Shape = Square(3)

def area(shape: Shape): Double = {
  shape match {
    case Square(l) => l * l
    case Circle(r) => Math.PI * r * r
    case Triangle(b, h) => 0.5 * b * h
  }
}

@ area(Square(3))
res61: Double = 9.0
```

You'll notice that we did not include a "catch all" `_` case. Since we marked `Shape` to be `sealed` it means that we
defined the enumeration of all possible subtypes. Because of this, the compiler can do an exhaustive search in order to
make sure that all possible types are accounted for and warn us at compile time if we missed something.

<h4 align="right">
    <a href="lesson0_4_classes_traits_objects.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="../_1_implicits/lesson1.md">Next →</a>
</h4>
