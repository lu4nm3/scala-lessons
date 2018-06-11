// ****************************************************************
//                              State
// ****************************************************************
import cats.data.State
import scala.language.higherKinds




// The State monad allows us to pass state around as part of a
// computation. We define state instances representing atomic state
// operations and thread them together using `map` and `flatMap`.
// This allows us to model mutable state in a purely functional way
// without using mutation.




// Instances of `State[S, A]` represent functions of type `S =>
// (S, A)` where `S` is the type of the state and `A` is the type
// of the result.

val s = State[Int, String] { state => (state, s"The state is $state") }

// State can be thought of a function that transforms an input
// state to an output state and computes a result. We can run our
// state monad by providing an initial state value:

val (state, result) = s.run(3).value
// state: Int = 3
// result: String = The state is 3

val onlyState = s.runS(3).value
// onlyState: Int = 3

val onlyResult = s.runA(3).value
// onlyResult: String = The state is 3




// And as with any other monad, we can use `map` and `flatMap` to
// thread state from one instance to another where each state
// represents an atomic state transformation and their combination
// represents a complete sequence of changes:

val step1 = State[Int, String] { state => (state + 1, s"Result of step 1 is $state") }
val step2 = State[Int, String] { state => (state * 2, s"Result of step 2 is $state") }

val combined: State[Int, (String, String)] = for {
  a <- step1
  b <- step2
} yield (a, b)

val (state2, result2) = combined.run(3).value
// state2: Int = 8
// result2: (String, String) = (Result of step 1 is 3,Result of step 2 is 4)




// The general approach for using the `State` monad is to represent
// each step of a computation as an instance and compose the steps
// using the standard monad operators:

val program: State[Int, (Int, Int, Int)] = for {
  a <- State.get[Int]                         // extract the state as the result
  _ <- State.set[Int](a + 1)                  // update the state and return `Unit` as the result
  b <- State.get[Int]
  _ <- State.modify[Int](s => s + 1)          // update the state using a function
  c <- State.inspect[Int, Int](s => s * 1000) // extract the state through a transformation function
} yield (a, b, c)

val (state3, result3) = program.run(3).value
// state3: Int = 5
// result3: (Int, Int, Int) = (3,4,5000)




