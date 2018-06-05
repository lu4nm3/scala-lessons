// ****************************************************************
//                       Implicit Conversions
// ****************************************************************
import scala.language.implicitConversions




class Greet(name: String) {
  def hello: String = s"hello $name!"
}




// You can tag any single-arg method with `implicit` keyword to
// allow compiler to automatically convert the input type to the
// output type:

implicit def stringToGreet(s: String): Greet = new Greet(s)

// Here the `String` type doesn't have a method called `hello`. So
// the compiler will search the implicit scope to see if there are
// any implicit conversions that it can use to convert `String`
// to another type that does have a method `hello`:

"bob".hello




// Be mindful when using implicit conversions as they can cause
// problems if you are not careful. It's easy to define very
// general implicit conversions that can mess with the semantics of
// a program. This is part of the reason why the compiler requires
// you to use the special `scala.language.implicitConversions`
// import when using implicit conversions.

// Even though the following is a contrived example, it
// demonstrates what can happen if you're not careful when using
// implicit conversions:

implicit def intToBoolean(i: Int): Boolean = i == 0

if (1) {
  "yes"
} else {
  "no"
}

if (0) {
  "no"
} else {
  "yes"
}