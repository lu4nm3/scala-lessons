<h4 align="right">
    <a href="lesson0_3_collections.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson0_5_pattern_matching.md">Next →</a>
</h4>

# Classes, Traits, & Objects

## Class

Classes in Scala are similar to those in Java in the sense that they have constructors, fields, methods, you can declare
the visibility level of different things using keywords like `private` and `protected` (everything is public by default)
, etc.

For example, we can take a class in Java:

```java
public class Foo {
    private int value;
    
    public Foo(int value) {
        this.value = value;
    }
    
    public int getValue() { return value; }
}
```

and write an equivalent (and more concise) class in Scala:

```scala
class Foo(val value: Int) {

}
```

### Case Class

Scala has another type of class called a `case class`. They're similar to regular `class`es with a few differences.

```scala
case class Foo(value: Int)
```

#### The `apply` method

When creating a new instance of a case class, you can omit  the `new` keyword if you want:

```scala
@ Foo(3)
res12: Foo = Foo(3)
```

This is because the compiler automatically generates what is known as a companion object for every case class. This
object comes with an `apply` method which is a special method that Scala uses to create new instances of a particular
type (given by the method's return type):

```scala
object Foo {
  def apply(value: Int): Foo = new Foo(3)
}
```

Whenever we have an `apply` method inside an object, we can call it directly using the object name without having to
specify `new` as seen above.

#### The `copy` method

Case classes are immutable by default. In order to update a case class, Scala automatically provides a `copy` method
that you can use to create a new instance of the case class containing the updated value(s) that you specified:

```scala
val foo = Foo(3)

@ foo.copy(value = 5)
res13: Foo = Foo(5)
```

#### Serializable

Case classes also extend `Serializable` and they automatically define `equals` and `hashCode` methods which are used to
compare different instances structurally instead of by reference.

#### Fields

Any fields declared in the case class are automatically turned into `val`s and are given getters/setters (unless
otherwise specified using `var`, `private`, etc). 

#### Pattern Matching

Finally, you are able to pattern match case classes since the compiler automatically generates an extractor method:

```scala
val foo = Foo(3)

@ foo match {
    case Foo(value) if value == 3 => "correct!"
    case _ => "wrong!"
  }
res14: String = "correct!"
```

### `class` vs `case class`

When should you choose to use a regular `class` over a `case class`? 

In object oriented languages like Java, the suggested approach when working with a domain is to use a **rich domain
model** where all business objects express behavior.

For example, an `Account` class might expose methods like `credit` or `debit` which will result in a change of the
underlying total amount of money in the account:

```java
public class Account {
    private String id;
    
    private int total;

    public void credit(int value) { this.value += value; }
    
    public void debit(int value) { this.value -= value; }
}
```

Scala gravitates more towards what is known as an **anemic domain model** where the domain itself (ie. the data objects)
is separated from the behaviors that operate on it. Modeling data is where case classes shine due to all of their unique
properties listed above.

With the example above, in Scala we would use case classes to represent just the account information:

```scala
case class Account(id: String, total: Int)
```

Then we would have a separate "service" containing the behaviors that operate on accounts:

```scala
class AccountService {
  def credit(account: Account, amount: Int): Account
  
  def debit(account: Account, amount: Int): Account
}
```

This gives us the ability to work with our data much more easily (immutability, pattern matching, etc) while allowing us
to potentially compose different behaviors together. This is a common pattern that we will explore in subsequent lessons
in terms of how we can compose different behaviors (ie. effects) together, irrespective of the data types they might be
used on.

## Trait

Traits in Scala are the equivalent of Java interfaces. You're able to define fields and methods in them without having
to fully define them (although you can if you want to):

```scala
trait Foo {
  val bar: String
  
  def toStr(i: Int): String
  
  def toInt(str: String): Int = str.toInt
}
```

In Scala, much like in Java, you can extend another trait/class/object with as many traits as you want but only with a
single class at most. That's why you should prefer to use traits over abstract classes.

### Algebraic data types (ADT)

Traits in Scala are commonly used to represent algebraic data types. Before we explain ADTs let's talk about algebras.
An **algebra** can be thought of as consisting of 2 things:

  - a set of objects and
  - the operations that can be applied to those objects to create new objects

#### Numeric algebra

In numeric algebra, you can think of whole numbers as the set of objects and operations like `+`, `-`, `*`, etc as
operations that can be used on existing numbers to create new numbers:

```
1 + 1 = 2
3 * 3 = 9
```

#### Relational algebra

This is the algebra of relational databases where tables are the set of existing objects and operations like `select`,
`update`, etc are the operations that let you create new objects.

#### Data type algebra

This is where ADTs come in. ADTs fall into 3 main categories:

  - sum type
  - product type
  - hybrid type
  
##### Sum type

Sum types are also referred to as "enumerated types" because you simply enumerate all of the possible instances of the
type. In Scala they are modeled using `sealed trait`s:

```scala
sealed trait Direction
case object North extends Direction
case object South extends Direction
case object East extends Direction
case object West extends Direction
```

The `sealed` keywords prevents a class/trait from being extended outside of the file they are defined in which means
that the types listed are the only possible instances of the base type. In this case you can think of the "operation" of
the algebra as the act of extending your trait with all of the possible types you want to define.

##### Product Type

A product type in Scala is represented as a case class whose constructor is used to create all possible concrete
instances of the type. In this case, the constructor is the "operation" that creates new instances of our type using
other types.

For example:

```scala
case class TwoBooleans(b1: Boolean, b2: Boolean)
```

All of the possible instances that we can create of this type are:

```scala
TwoBooleans(true, true)
TwoBooleans(true, false)
TwoBooleans(false, true)
TwoBooleans(false, false)
```

Another way to look at this is that the total number of instances that we can create for this type is determined by:

`(# posibilities for b1) x (# posibilities for b2)` -> `2 x 2` -> `4`

Hence why they are called "product" types.

##### Hybrid Type

A hybrid type is a type that combines sum type and product types. 

For example, we could model a shape hierarchy using hybrid types:

```scala
sealed trait Shape
case class Square(length: Int) extends Shape
case class Circle(radius: Int) extends Shape
case class Triangle(base: Int, height: Int) extends Shape
```

These types represent a sum type because `Shape` is a `Square` or a `Circle` or a `Triangle`. Furthermore, each type
represented by a case class is a product type because they have their own respective fields which are needed to build
instances of that type. 

## Object

Unlike classes, you can not create instances out of objects. Instead, objects are used to store what you would think of
as static fields and methods in Java:

```scala
object Util {
  def add(a: Int, b: Int): Int = a + b
}
```

You can access these fields and methods directly through the object:

```scala
@ Util.add(2, 3)
res1: Int = 5
```

### Companion objects

Companion objects are objects that are normally defined side-by-side with another class or trait and they share the same
name. They are used to define things like constants and methods that are related to the type in question but that do not
need to belong to an instance of said type.

For example, if we had a `Circle` trait, we could define a companion object with helpful constants for working on circles:

```scala
case class Circle(radius: Int)

object Circle {
  val Pi: Double = 3.14
}
```

<h4 align="right">
    <a href="lesson0_3_collections.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson0_5_pattern_matching.md">Next →</a>
</h4>
