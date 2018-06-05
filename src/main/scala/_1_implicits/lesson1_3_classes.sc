// ****************************************************************
//                         Implicit Classes
// ****************************************************************




// Implicit classes are a Scala language feature that allows us to
// define extra functionality on existing data types without using
// conventional inheritance. This is known as `type enrichment`.

// You can think of implicit classes as being syntactic sugar for
// the combination of a regular class and an implicit conversion.
// In other words, the previous example:

import scala.language.implicitConversions

class Greet(name: String) {
  def hello: String = s"hello $name!"
}

implicit def stringToGreet(s: String): Greet = new Greet(s)

// can be instead re-written as:

//implicit class Greet(name: String) {
//  def hello: String = s"hello $name!"
//}

// And used just in the same way.
"bob".hello

