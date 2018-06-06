// ****************************************************************
//                             Writer
// ****************************************************************
import cats.data.Writer

import scala.language.higherKinds




// Writer is a monad that lets us carry a "log" along with a
// computation. We can use it to record messages, errors, or
// additional data about a computation, ane extract the log
// alongside the final result.

// A `Writer[W, A]` carries 2 values: a log of type `W` and a
// result of type `A`.

Writer(Vector("step 1", "step 2"), 42)




// Typically, the log type `W` should have a `Monoid[W]` in scope
// in order to use some of the methods for Writer.




// If we only have a result, we can use the `pure` operation:

import cats.instances.vector._    // for Monoid[Vector]
import cats.syntax.applicative._  // for pure

type Logged[A] = Writer[Vector[String], A]

42.pure[Logged]




// If we have a log and no result, we can create use the `tell`
// operation:

import cats.syntax.writer._

Vector("step 1", "step 2").tell




// If we have both a result and a log, we can either use
// `Writer.apply` like in the example above, or we can use the
// following syntax:

val w = 42.writer(Vector("step 1", "step 2"))




// We can extract the result and log from a Writer using the
// `value` and `written` operations:

w.value
// res0: Int = 42

w.written
// res1: Vector[String] = Vector("step 1", "step 2")

w.run
// res2: (Vector[String], Int) = (42, Vector("step 1", "step 2"))




// ****************************************************************






