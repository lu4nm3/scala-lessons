// ****************************************************************
//                             Traverse
// ****************************************************************
import cats.Traverse
import cats.implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._




// `foldLeft` and `foldRight` are flexible iteration methods but
// they require us to do a lot of work to define accumulator and
// combinator functions. The `Traverse` type class is a higher
// level tool that leverages `Applicative`s to provide a more
// convenient pattern for iteration.




val hostnames = List("one.example.com", "two.example.com", "three.example.com")

def getUptime(hostname: String): Future[Int] = Future(hostname.length * 60)

def getAllUptimes: Future[List[Int]] = {
  hostnames.foldLeft(Future(List.empty[Int])) { (futureAccum, host) =>
    val futureUptime = getUptime(host)

    for {
      accum <- futureAccum
      uptime <- futureUptime
    } yield accum :+ uptime
  }
}

Await.result(getAllUptimes, 1.second)
//res0: List[Int] = List(900, 900, 1020)

// Here we iterate over hostnames, call `getUptime` for each host
// and combine the results into a list. We can improve on things by
// using `Future.traverse`:

def getAllUptimes2: Future[List[Int]] = Future.traverse(hostnames)(getUptime)

Await.result(getAllUptimes2, 1.second)
//res1: List[Int] = List(900, 900, 1020)

// `Future.traverse` gives us a clean, high-level interface to do
// what we want:
//
// 1) start with a `List[A]`
// 2) provide a function `A => Future[B]`
// 3) end up with a `Future[List[B]]`




// The standard library also provides another method `sequence`
// that assume we're starting with a `List[Future[B]]`:

def sequence[B](futures: List[Future[B]]): Future[List[B]] = {
  Future.traverse(futures)(identity)
}

// In this case we:
//
// 1) start with a `List[Future[A]]`
// 2) end up with a `Future[List[A]]`




// The Cats `Traverse` type class generalizes these patterns to
// work with any type of `Applicative`.




def getAllUptimes3: Future[List[Int]] = Traverse[List].traverse(hostnames)(getUptime)

Await.result(getAllUptimes3, 1.second)
//res2: List[Int] = List(900, 900, 1020)




val uptimes = List(Future(900), Future(900), Future(1020))

val allUptimes = Traverse[List].sequence(uptimes)

Await.result(allUptimes, 1.second)
//res3: List[Int] = List(900, 900, 1020)




// `Traverse` abstracts and generalizes the `traverse` and
// `sequence` methods we know from `Future`. Using these methods we
/// can turn any `F[G[A]]` into a `G[F[A]]` for any `F` with an
// instance of `Traverse` and any `G` with an instance of
// `Applicative`.



