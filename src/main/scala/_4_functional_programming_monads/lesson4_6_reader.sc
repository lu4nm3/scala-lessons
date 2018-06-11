// ****************************************************************
//                             Reader
// ****************************************************************
import cats.data.Reader
import monix.eval.Task
import scala.language.higherKinds




// Reader is a monad that allows us to sequence operations that
// depend on some common input. Instances of Reader wrap up
// functions of one argument and provide us with useful methods
// for composition.

case class Dog(name: String, favoriteFood: String)

val dogName: Reader[Dog, String] = Reader(dog => dog.name)

dogName.run(Dog("Comet", "chicken"))
// res0: cats.Id[String] = Comet




// What advantages do Readers provide over simple functions?

val greetDog: Dog => String = dog => s"Hello ${dog.name}"
val feedDog: Dog => String = dog => s"Have a nice bowl of ${dog.favoriteFood}"

// Can't compose because functions don't have `map` or `flatMap`:

//for {
//  greet <- greetDog
//  feed <- feedDog
//} yield s"$greet. $feed."
// value flatMap is not a member of Dog => String
// value map is not a member of Dog => String




// The power of Readers comes from their `map` and `flatMap`
// operations which represent different kinds of function
// composition.

val greetDogR = Reader[Dog, String](greetDog)
val feedDogR = Reader[Dog, String](feedDog)

val greetAndFeed = for {
  greet <- greetDogR
  feed <- feedDogR
} yield s"$greet. $feed."

greetAndFeed.run(Dog("Comet", "chicken"))
// res1: cats.Id[String] = Hello Comet. Have a nice bowl of chicken.




// Readers are useful in situations where:
//
//   - we need to defer injection of a known parameter or set of
//     parameters
//
//   - we want to be able to test parts of the program in isolation




// One common use for Readers is dependency injection. If we have a
// number of operations that all depend on some external
// configuration, we can chain them together using a Reader to
// generate a single operation that accepts the configuration as a
// parameter and runs our program in the order specified:




// One common use for Readers is dependency injection (DI). The
// main difference between conventional DI through constructors and
// DI through the Reader monad is what the caller knows about the
// dependencies of the method it is calling.

// In constructor-based DI, this knowledge is hidden:

class EmailServer {
  def send(to: String, body: String): Task[Unit] = ???
}

class UserNotifier(emailServer: EmailServer) {
  def notify(emailAddr: String, msg: String): Task[Unit] = {
    emailServer.send(emailAddr, msg)
  }
}

class Service(userNotifier: UserNotifier) {
  def notifyUser(userId: String): Task[Unit] = ??? // make use of userNotifier here
}

// Inside `Service`, when using the `UserNotifier` instance, the
// dependency on `EmailServer` is hidden from the caller. It could
// be argued that this dependency is an implementation detail and
// that the caller shouldn't be concerned about it.

// When using the Reader monad, the `UserNotifier` can become an
// object and the dependencies are encoded in `notify`'s return
// type:

object UserNotifier {
  def notify(emailAddr: String, msg: String): Reader[EmailServer, Task[Unit]] = {
    Reader { emailServer =>
      emailServer.send(emailAddr, msg)
    }
  }
}

// Now any caller of `notify` knows about the dependency on
// `EmailServer` that has to be either satisfied or passed upwards
// as a dependency:

object Service {
  def notifyUser(userId: String): Reader[EmailServer, Task[Unit]] = ??? // make use of UserNotifier.notify here
}

// We gain one important thing with the Reader monad which is that
// callers of `notifyUser` know what kind of side-effects a given
// method can have just by looking at the signature. Thus, the
// Reader monad can be viewed as a basic way to track effects by
// explicitly stating that certain dependencies can be used down in
// the call chain.




// Which style is better? It depends on whether we want to abstract
// dependencies or whether we want to track effects.

// Constructor based DI is good when you want to hide the
// dependencies from a method's callers such as configuration.

// Reader monad based DI is useful for tracking effects and making
// dependencies explicit to a method's callers. They are best used
// when it is informative for the user of the code to know what
// dependencies can be used down in the call chain.




