<h1>Higher-Kinded Types</h1>

Before jumping straight to higher-kinded types, let's give a bit of background first.

<h3>Values</h3>

As we know by now, values represent raw data and they are at the lowest level of abstraction:

```scala
scala> "Daniel"
res0: String = Daniel

scala> 1
res1: Int = 1

scala> (1, 2, 3)
res2: (Int, Int, Int) = (1,2,3)

scala> List(1, 2, 3)
res3: List[Int] = List(1, 2, 3)
```

Nothing new here.

<h3>Proper Types</h3>

Proper types are types that you can make a value out of (eg. `String`, `Int`, `(Int, Int, Int)`, `List[Int]`). They are 
at a higher level of abstraction than values.

```scala
val name: String = "Daniel"

val one: Int = 1

val tuple: (Int, Int, Int) = (1, 2, 3)

val list: List[Int] = List(1, 2, 3)
```

<h3>First-Order Types</h3>

First-order types are types that have "holes" we can fill with other types to produce new types. They are also known as 
**type constructors** or **higher-kinded types** and they sit at an abstraction level higher than proper types.

Just like value constructors (ie. functions) produce values:

```scala
val f = (i: Int) => i * 2 // value constructor, takes one parameter
f(1)                      // value, produced using a value parameter
```

Type constructors produce proper types:

```scala
List[_]   // type constructor, takes one parameter
List[Int] // proper type, produced using type parameter
```

<h3>Second-Order Types</h3>

Second-order types are types that abstract over first-order types. They represent types that take type constructors as 
parameters.

```scala
List("hello")         // value
```

```scala
List[String]          // proper type (used to instatiate values)
```

```scala
List[_]               // first-order type (takes one type to produce a proper type)
```

```scala
// second-order type (takes one first order type to produce a proper type)
trait WithList[F[_]] {

}
```

```scala
List("hello")       // value

List[String]        // proper type (used to create values)

List[_]             // 1st-order type (takes a  proper type to produce another proper type)

trait Foo[F[_]] {   // 2nd-order type (takes a 1st-order type to produce a proper type)

} 
```



// Higher Kinded Types

import scala.language.higherKinds


// As mentioned previously, a type with a type constructor (ie. a
// type with [_]) is called a higher kinded type.

// A type constructor is just like a function that works at the
// type level instead of the value level. Given a proper type, it
// will return another proper type:

// T => F[T]

// We can also have a function that, given a type level function
// (ie. a type constructor), returns another type level function:

// F[_] => G[F[_]]

// These types of function are known as higher order functions.




// The * notation

// We can use * as a notation to describe what order things are.

String    // is of kind * and is Order 0

List[_]   // is of kind * -> * (takes 1 Order-0 type and produces a
          // proper type) and is Order-1

Map[_, _] // is of kind * -> * -> * (takes 2 Order-0 types and
          // produces a proper type) and is Order-1

trait HasList[F[_]] // is of kind (* -> *) -> * (takes 1 Order-1
                    // type and produces a proper type) and is
                    // Order-2
