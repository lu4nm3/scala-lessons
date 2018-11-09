<h4 align="right">
    <a href="../_2_type_classes/lesson2.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson3_2_monoids.md">Next →</a>
</h4>

<h1>Higher-Kinded Types</h1>

Before jumping straight to higher-kinded types, let's give a bit of background first.

<h3>Values</h3>

As we know by now, values represent raw data and they are at the lowest level of abstraction:

```scala
@ "Daniel"
res0: String = Daniel

@ 1
res1: Int = 1

@ (1, 2, 3)
res2: (Int, Int, Int) = (1,2,3)

@ List(1, 2, 3)
res3: List[Int] = List(1, 2, 3)
```

Nothing new here.

<h3>Proper Types (aka. 0-order types)</h3>

Proper types are types that you can make a value out of (eg. `String`, `Int`, `(Int, Int, Int)`, `List[Int]`). They are 
at a higher level of abstraction than values.

```scala
val name: String = "Daniel"

val one: Int = 1

val tuple: (Int, Int, Int) = (1, 2, 3)

val list: List[Int] = List(1, 2, 3)
```

<h3>1st-order Types</h3>

First-order types are types that have "holes" we can fill with other types to produce new types. They are also known as 
**type constructors** or **higher-kinded types** and they sit at an abstraction level higher than proper types.

Just like value constructors (ie. functions) produce values:

```scala
val f = (i: Int) => i * 2 // value constructor, takes one value parameter
f(1)                      // produces value
```

Type constructors produce proper types:

```scala
List[_]   // type constructor, takes one type parameter
List[Int] // produces proper type
```

<h3>2nd-order Types</h3>

Second-order types are types that abstract over first-order types. They represent types that take type constructors as 
parameters.

```scala
List("hello")       // value

List[String]        // proper type (used to create values)

List[_]             // 1st-order type (takes a proper type to produce another proper type)

trait Foo[F[_]] {}  // 2nd-order type (takes a 1st-order type to produce a proper type)
```

<h3>The <code>*</code> notation</h3>

You will sometimes see the `*` notation to describe the order of types.

```scala
String              // is of kind * and is Order-0

List[_]             // is of kind * -> * (takes 1 Order-0 type and produces 
                    // an Order-0 type) and is Order-1

Map[_, _]           // is of kind * -> * -> * (takes 2 Order-0 types and
                    // produces an Order-0 type) and is Order-1

trait Foo[F[_]] {}  // is of kind (* -> *) -> * (takes 1 Order-1 type
                    // and produces an Order-0 type) and is Order-2
```

<h4 align="right">
    <a href="../_2_type_classes/lesson2.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson3_2_monoids.md">Next →</a>
</h4>