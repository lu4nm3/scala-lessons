// ****************************************************************
//                             Kleisli
// ****************************************************************
import cats.Id
import cats.data.Kleisli
import monix.eval.Task
import scala.language.higherKinds




// The Scala standard library already allows us to compose
// functions of type `A => B` together using the `compose` and
// `andThen` operators so long as the output type of one function
// matches the input type of the next function:

val getValueFromDb: Unit => Int = _ => 3
val processValue: Int => Int = value => value * 2
val writeValueToDb: Int => Boolean = _ => true

val combo1: Unit => Boolean = writeValueToDb compose processValue compose getValueFromDb

val combo2: Unit => Boolean = getValueFromDb andThen processValue andThen writeValueToDb




// Often times, we have to deal with functions that produce effects
// (ie. Option, Future, Task, etc) especially when dealing with
// things like uncertainty or asynchronous calls:

val getValueFromDbIO: Unit => Task[Int] = _ => Task.now(3)
val processValueIO: Int => Task[Int] = value => Task.now(value * 2)
val writeValueToDbIO: Int => Task[Boolean] = _ => Task.now(true)

// Now, the output type of one function no longer matches the input
// type of the next function so we can't compose things like we did
// before:

//val combo1IO: Unit => Task[Boolean] = writeValueToDbIO compose processValueIO compose getValueFromDbIO // type mismatch
//
//val combo2IO: Unit => Task[Boolean] = getValueFromDbIO andThen processValueIO andThen writeValueToDbIO // type mismatch




// There are a few things we can do here. First, we can try using
// the `flatMap` operation of the effects to compose the functions:

val combo3IO: Unit => Task[Boolean] = { _ =>
  getValueFromDbIO() flatMap { value =>
    processValueIO(value) flatMap { processed =>
      writeValueToDbIO(processed)
    }
  }
}

// We can also use a for-comprehension to re-write the above:

val combo4IO: Unit => Task[Boolean] = _ => for {
  value <- getValueFromDbIO()
  processed <- processValueIO(value)
  result <- writeValueToDbIO(processed)
} yield result




// This leads us to the Kleisli monad which we can use as yet
// another way to compose these functions together in a much
// cleaner and succinct manner. Kleisli[F[_], A, B] can be thought
// of as a wrapper  for functions of type `A => F[B]` where `F[_]`
// is a type constructor.

val f: Int => Task[String] = ???
val fk: Kleisli[Task, Int, String] = ???




// Using our previous IO example, we can rewrite it using Kleisli
// as follows:

// val getValueFromDbIO: Unit => Task[Int] = _ => Task.now(3)
// val processValueIO: Int => Task[Int] = value => Task.now(value * 2)
// val writeValueToDbIO: Int => Task[Boolean] = _ => Task.now(true)

val getValueFromDbK: Kleisli[Task, Unit, Int] = Kleisli(getValueFromDbIO)
val processValueK: Kleisli[Task, Int, Int] = Kleisli(processValueIO)
val writeValueToDbK: Kleisli[Task, Int, Boolean] = Kleisli(writeValueToDbIO)

val combineK1: Kleisli[Task, Unit, Boolean] = getValueFromDbK andThen processValueK andThen writeValueToDbK

combineK1.run // Unit => Task[Boolean]




// For even cleaner syntax, you can inline the lifting of the first
// function in the chain and then simply compose the rest of the
// `A => F[B]` functions directly without lifting them to a Kleisli
// by using Kleisli's `andThen` operator:

val combineK2: Kleisli[Task, Unit, Boolean] = Kleisli(getValueFromDbIO) andThen processValueIO andThen writeValueToDbIO

combineK2.run // Unit => Task[Boolean]




// One important thing to note is that a Reader is simply a more
// specialized form of Kleisli that abstracts the type constructor
// of the result type. In fact, Reader is actually defined as a
// type alias of Kleisli that uses `Id` for the effect type:

type Reader[I, O] = Kleisli[Id, I, O]




// This means that we can use Kleisli to sequence operations that
// rely on some common input the same way we did with `Reader`:

case class Dog(name: String, favoriteFood: String)

val greetDog: Dog => Task[String] = dog => Task.now(s"Hello ${dog.name}")
val feedDog: Dog => Task[String] = dog => Task.now(s"Have a nice bowl of ${dog.favoriteFood}")

val greetAndFeed = for {
  greet <- Kleisli[Task, Dog, String](greetDog)
  feed <- Kleisli[Task, Dog, String](feedDog)
} yield s"$greet. $feed."

greetAndFeed.run // Dog => Task[String]









