<h4 align="right">
    <a href="lesson4_7_kleisli.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
    <a href="lesson4_9_monad_transformers.md">Next →</a>
</h4>

<h1>State</h1>

The State monad allows us to pass state around as part of a computation. We define state instances representing atomic 
state operations and thread them together using `map` and `flatMap`. This allows us to model mutable state in a purely 
functional way without using mutation.

Instances of `State[S, A]` represent functions of type `S => (S, A)` where `S` is the type of the state and `A` is the 
type of the result. You can create an instance of `State` by wrapping such a function:

```scala
import cats.data.State

val s = State[Int, String] { state => (state, s"The state is $state") }
```

`State` can be thought of a function that transforms an input state to an output state and computes a result. We can 
execute our state monad by providing an initial state value to one of several `run` methods:

```scala
@ s.run(3).value
res0: (Int, String) = (3,The state is 3)

@ s.runS(3).value
res1: Int = 3

@ s.runA(3).value
res2: String = The state is 3
```

And as with any other monad, we can use `map` and `flatMap` to thread state from one instance to another where each 
state represents an atomic state transformation and their combination represents a complete sequence of changes:

Here we have 2 separate `State` transformations which we can compose together in order to create a single transformation
that combines things together:

```scala
val step1 = State[Int, String] { state => (state + 1, s"Result of step 1 is $state") }
val step2 = State[Int, String] { state => (state * 2, s"Result of step 2 is $state") }

val combined: State[Int, (String, String)] = for {
  a <- step1
  b <- step2
} yield (a, b)
```

When we run this composed `State` transformation, we get back the final state value along with a tuple of our result
values:

```scala
@ combined.run(3).value
res3: (Int, (String, String)) = (8,(Result of step 1 is 3,Result of step 2 is 4))
```

The general approach for using the `State` monad is to represent each step of a computation as an instance and compose 
the steps using the standard monad operators:

```scala
val program: State[Int, (Int, Int, Int)] = for {
  a <- State.get[Int]                         // extract the state as the result
  _ <- State.set[Int](a + 1)                  // update the state and return `Unit` as the result
  b <- State.get[Int]
  _ <- State.modify[Int](s => s + 1)          // update the state using a function
  c <- State.inspect[Int, Int](s => s * 1000) // extract the state through a transformation function
} yield (a, b, c)
```

After running it, we get back our final state value along with our result which is a tuple in this case:

```scala
@ program.run(3).value
res4: (Int, (Int, Int, Int)) = (5,(3,4,5000))
```

<h4 align="right">
    <a href="lesson4_7_kleisli.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
    <a href="lesson4_9_monad_transformers.md">Next →</a>
</h4>