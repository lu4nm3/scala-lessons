// ****************************************************************
//                             Basics
// ****************************************************************




// Type classes are a programming pattern that originated in
// Haskell. Allow us to extend existing code with new functionality
// without using inheritance and without altering the original
// source code.
//
// There are 3 main components to they type class pattern: the type
// class itself, instances for particular types, and the interface
// methods that we expose to users.



// A type class is an interface that represents some functionality
// we want to implement.

// In this example, `Area` is our type class, with Shape and its
// subtypes providing supporting code:

trait Area[S] {
  def calculate(shape: S): Double
}

sealed trait Shape
case class Circle(radius: Int) extends Shape
case class Triangle(base: Int, height: Int) extends Shape