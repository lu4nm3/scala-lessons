// ****************************************************************
//                             Reader
// ****************************************************************
import cats.data.Reader

import scala.language.higherKinds




// Reader is a monad that allows us to sequence operations that
// depend on some input. Instances of Reader wrap up functions of
// one argument and provide us with useful methods for composition.

case class Dog(name: String, favoriteFood: String)

val dogName: Reader[Dog, String] = Reader(dog => dog.name)

dogName.run(Dog("Comet", "chicken"))
// res0: cats.Id[String] = Comet




// What advantages do Readers provide over simple functions?

val greetDog: Dog => String = dog => s"Hello ${dog.name}"
val feedDog: Dog => String = dog => s"Have a nice bowl of ${dog.favoriteFood}"

// Can't compose because functions don't have `map` or `flatMap`:

for {
  greet <- greetDog
  feed <- feedDog
} yield s"$greet. $feed."




// The power of Readers comes from their `map` and `flatMap`
// operations which represent different kinds of function
// composition.

val greetDogR = Reader[Dog, String](dog => s"Hello ${dog.name}")
val feedDogR = Reader[Dog, String](dog => s"Have a nice bowl of ${dog.favoriteFood}")

val greetAndFeed = for {
  greet <- greetDogR
  feed <- feedDogR
} yield s"$greet. $feed."

greetAndFeed.run(Dog("Comet", "chicken"))
// res1: cats.Id[String] = Hello Comet. Have a nice bowl of chicken.




//
// ****************************************************************
