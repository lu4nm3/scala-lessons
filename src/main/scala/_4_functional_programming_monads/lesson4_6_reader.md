<h4 align="right">
    <a href="lesson4_5_writer.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
    <a href="lesson4_7_kleisli.md">Next →</a>
</h4>

<h1>Reader</h1>

`Reader` is a monad that allows us to sequence operations that depend on some common input. Instances of Reader wrap up
functions of one argument and provide us with useful methods for composition.

```scala
import cats.data.Reader

case class Dog(name: String, favoriteFood: String)

val dogName: Reader[Dog, String] = Reader(dog => dog.name)
```

```scala
scala> dogName.run(Dog("Comet", "chicken"))
res0: cats.Id[String] = Comet
```

<h3>What advantages do Readers provide over simple functions?</h3>

Let's take the following simple functions as an example:

```scala
val greetDog: Dog => String = dog => s"Hello ${dog.name}"
val feedDog: Dog => String = dog => s"Have a nice bowl of ${dog.favoriteFood}"
```

We can't compose functions together because they don't have `map` or `flatMap`:

```scala
scala> for {
     |   greet <- greetDog
     |   feed <- feedDog
     | } yield s"$greet. $feed."
<console>:16: error: value flatMap is not a member of Dog => String
         greet <- greetDog
                  ^
<console>:17: error: value map is not a member of Dog => String
         feed <- feedDog
                 ^
```

The power of Readers comes from their `map` and `flatMap` operations which represent different kinds of function 
composition.

Let's re-write our functions to make use of `Reader`:

```scala
val greetDog = Reader[Dog, String](dog => s"Hello ${dog.name}")
val feedDog = Reader[Dog, String](dog => s"Have a nice bowl of ${dog.favoriteFood}")
```

Now we can compose them together just like any `Monad`:

```scala
val greetAndFeed = for {
  greet <- greetDog
  feed <- feedDog
} yield s"$greet. $feed."
```

And we can execute the composition using `Reader`'s `run` method:

```scala
scala> greetAndFeed.run(Dog("Comet", "chicken"))
res3: cats.Id[String] = Hello Comet. Have a nice bowl of chicken.
```

Notice also how we only had to pass our input value once when we call `run`. `Reader` takes care of providing this input 
to the rest of the reader functions. 

<h3>Where are <code>Reader</code>s useful?</h3>

Readers are useful in situations where:

  - we need to defer injection of a known parameter or set of parameters
  - we want to be able to test parts of the program in isolation

One common use for Readers is dependency injection (DI). The main difference between conventional DI through 
constructors and DI through the Reader monad is what the caller knows about the dependencies of the method it is 
calling.

In constructor-based DI, this knowledge is hidden:

```scala
import monix.eval.Task

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
```

Inside `Service`, when using the `UserNotifier` instance, the dependency on `EmailServer` is hidden from the caller. The
caller in this case has no idea what went into creating `UserNotifier`. It could be argued that this dependency is an 
implementation detail and that the caller shouldn't be concerned about it.

When using the `Reader` monad, the `UserNotifier` can become an object and the dependencies can be encoded in `notify`'s 
return type:

```scala
object UserNotifier {
  def notify(emailAddr: String, msg: String): Reader[EmailServer, Task[Unit]] = {
    Reader { emailServer =>
      emailServer.send(emailAddr, msg)
    }
  }
}
```

Now any caller of `notify` knows about the dependency on `EmailServer` which has to be satisfied or passed upwards as a
dependency:

```scala
object Service {
  def notifyUser(userId: String): Reader[EmailServer, Task[Unit]] = {
    Reader { emailServer =>
      // make use of UserNotifier.notify here
    }
  }
}
```

We gain one important thing with the `Reader` monad which is that callers of `notifyUser` know what kind of side-effects 
a given method can have just by looking at the signature. Thus, the `Reader` monad can be viewed as a basic way to track 
effects by explicitly stating that certain dependencies can be used down in the call chain.

<h3>Conventional dependency injection vs Reader</h3>

Which style is better? It depends on whether we want to abstract dependencies or whether we want to track effects.

Constructor based dependency injection is good when you want to hide the dependencies from a method's callers such as 
configuration.

`Reader` monad based dependency injection is useful for tracking effects and making dependencies explicit to a method's 
callers. They are best used when it is informative for the user of the code to know what dependencies can be used down 
in the call chain.

<h4 align="right">
    <a href="lesson4_5_writer.md">← Previous</a> |
    <a href="lesson4.md">Menu</a> |
    <a href="lesson4_7_kleisli.md">Next →</a>
</h4>