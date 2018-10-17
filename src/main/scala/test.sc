import cats.Traverse
import cats.data.OptionT
import cats.effect.IO
import monix.eval.Task

case class User(id: String, age: Int)

def getUser(id: String): IO[Option[User]] = IO.pure(None)


def lookUpUserAge2(id: String): IO[Option[Int]] = {
  OptionT(getUser(id))
    .map(user => user.age)
    .value
}

OptionT(getUser("sdf"))

def lookUpUserAge(id: String): IO[Option[Int]] = {
  for {
    optUser <- getUser(id)
  } yield {
    for {
      user <- optUser
    } yield user.age
  }
}


//def getUser(id: String): IO[Option[User]] = ???

def avgAge(userId1: String, userId2: String, userId3: String): IO[Option[Int]] = {
  val result = for {
    usr1 <- OptionT(getUser(userId1))  // use monad transformers locally to simplify composition
    usr2 <- OptionT(getUser(userId2))
    usr3 <- OptionT(getUser(userId3))
  } yield (usr1.age + usr2.age + usr3.age) / 3

  result.value // return untransformed stack
}

import cats.implicits._

val hostnames = List("one.example.com", "two.example.com", "three.example.com")
def getUptime(hostname: String): Task[Int] = {
  Task.now(hostname.length * 60)
}

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global

Task.sequence()
Task.traverse(hostnames)(getUptime)

Traverse[List].traverse(hostnames)(getUptime).runSyncUnsafe(1.second)