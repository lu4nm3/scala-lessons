// ****************************************************************
//                       Higher Kinded Types
// ****************************************************************
import scala.language.higherKinds




// Values

// Values represent raw data and they are at the lowest level of
// abstraction.

val name = "daniel"
val one = 1
val oneAndTwoAsTuple = (1,2)
val oneAndTwoInAList = List(1,2)




// Proper Types

// Proper types are types that you can make a value out of (eg.
// `String`, `Int`, `(Int, Int)`, `List[Int]`). They are at a
// higher level than values.

val name2: String = "daniel"
val one2: Int = 1
val oneAndTwoAsTuple2: (Int, Int) = (1,2)
val oneAndTwoInAList2: List[Int] = List(1,2)




// First-Order Types

// First-order types are types that have "holes" we can fill with
// other types to produce new types. They are also known as type
// constructors or "higher kinded types" and they sit at an
// abstraction level higher than proper types.

// type constructors produce types

List[_]   // type constructor, takes one parameter
List[Int] // proper type, produced using type parameter

// value constructors (ie. functions) produce values

math.abs(_) // function, takes one parameter
math.abs(1) // value, produced using a value parameter




// Second-Order Types

// Second-order types are types that abstract over first-order
// types. They represent types that take type constructors as
// parameters.

List("hello")         // value
List[String]          // proper type (used to instatiate values)
List[_]               // first-order type (takes one type to produce a proper type)
trait WithList[F[_]]  // second-order type (takes one first order type to produce a proper type)




// Higher Kinded Types

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
