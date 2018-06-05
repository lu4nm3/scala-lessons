// ****************************************************************
//                    Implicit Values & Parameters
// ****************************************************************
import util._




// You can use the `implicit` keyword inside a parameter list to
// make specifying the parameters optional:

def foo(implicit ctx: Context): Unit = ()

// We can specify the context as normal

foo(Context())

// Or we can omit the implicit argument to have the compiler search
// for implicit values of the correct type that it can use to fill
// the missing arg.

implicit val ctx: Context = Context()
foo




// Note that the `implicit` keyword applies to the whole parameter
// list, not just an individual parameter:

def bar(implicit ctx: Context, conf: Config): Unit = ()




// Any implicit value must always be declared inside an object,
// class, or trait.

// We can create an implicit value by tagging any `val`, `var`,
// `object`, or 0-argument `def` with the `implicit` keyword. This
// makes it a potential candidate as an implicit parameter.

implicit val x1: String = ""
implicit var x2: String = ""
implicit object x3 {}
implicit def x4: String = ""




// What are they good for?

// More tedious and verbose. Need to manually pass in conf arg:

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def initDb1(conf: Config): Future[Unit] = ???
def initDb2(conf: Config): Future[Unit] = ???
def initDb3(conf: Config): Future[Unit] = ???

val conf: Config = Config()

def init: Future[Unit] = {
  for {
    _ <- initDb1(conf)
    _ <- initDb2(conf)
    _ <- initDb3(conf)
  } yield ()
}




// Can instead pass conf implicitly along to every place that needs
// it:

//import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
//
//def initDb1(implicit conf: Config): Future[Unit] = ???
//def initDb2(implicit conf: Config): Future[Unit] = ???
//def initDb3(implicit conf: Config): Future[Unit] = ???
//
//implicit val conf: Config = Config()
//
//def init: Future[Unit] = {
//  for {
//    _ <- initDb1
//    _ <- initDb2
//    _ <- initDb3
//  } yield ()
//}




// What happens when multiple implicit values of the same type are
// in scope?

implicit val x: Int = 1
implicit val y: Int = 2
implicit val z: Int = 3

def useInt(implicit i: Int): Int = i
//useInt