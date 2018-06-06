// ****************************************************************
//                              Eval
// ****************************************************************
import cats.Eval




// Eval is a monad that allows us to abstract over different models
// of evaluation (ie. eager, lazy) as well as whether or not a
// result is memoized.

// `val`s are both eager and momoized (you will notice how the
// print statement is only executed once regardless of how many
// times `x` is accessed):

val x = {
  println("computing x...")
  math.random
}

x // first access

x // second access




// `def`s are lazy and not momoized (you will noticed how the print
// statement is re-run on every access of `y`):

def y = {
  println("computing y...")
  math.random
}

y // first access

y // second access




// `lazy val`s are lazy and memoized (you will noticed that nothing
// gets printed when `z` is defined and that once it is accessed,
// only the first access will execute the print and memoize the
// resulting value):

lazy val z = {
  println("computing z...")
  math.random
}

z // first access

z // second access




// `Eval` has 3 subtypes: `Now`, `Later`, and `Always`.

// *****************************************************
// *  Scala          Cats          Properties          *
// *****************************************************
// *  val            Now           eager, momoized     *
// *  lazy val       Later         lazy, memoized      *
// *  def            Always        lazy, not memoized  *
// *****************************************************

// `Eval.now` captures a value right now similar to a `val`:

val evalX = Eval.now {
  println("computing x...")
  math.random
}

evalX.value

evalX.value

// `Eval.always` captures a lazy computation similar to a `def`:

val evalY = Eval.always {
  println("computing y...")
  math.random
}

evalY.value

evalY.value

// `Eval.later` captures a lazy, momoized computation similar to a
// `lazy val`:

val evalZ = Eval.later {
  println("computing y...")
  math.random
}

evalZ.value

evalZ.value




// Like any monad, Eval's `map` and `flatMap` operations add
// computations to a chain while still keeping the semantics of the
// origination Eval instances. Mapping functions, however, are
// always called lazily on demand:

val result = for {
  a <- Eval.now {
    println("calculating a..."); 3
  }
  b <- Eval.always {
    println("calculating b..."); 2
  }
} yield {
  println("multiplying a and b...")
  a * b
}

result.value

result.value

// Note that in the example above, the `map` function expressed by
// the `yield` block is evaluated lazily.




// One useful property of Eval is that its `map` and `flatMap`
// operations are trampolined. This means we can nest calls to
// `map` and `flatMap` arbitrarily without consuming stack space.
// This is also known as "stack safety".

// Eval has a `defer` operation which takes an existing instance of
// Eval and defers its evaluation. `defer` is trampolined just like
// `map` and `flatMap` so we can use it as a quick way to make an
// existing operation stack safe:

def factorial(n: BigInt): BigInt = {
  if (n == 1) n
  else n * factorial(n - 1)
}

//factorial(50000).value // stackoverflow!




def factorial2(n: BigInt): Eval[BigInt] = {
  if (n == 1) Eval.now(n)
  else Eval.defer(factorial2(n - 1).map(_ * n))
}

factorial2(50000).value

// Keep in mind that trampolining is not free. It uses the heap to
// create a chain of function objects to avoid consuming stack
// space. In this case, computations are bounded by the size of the
// heap rather than the stack.






