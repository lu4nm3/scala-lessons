// ****************************************************************
//                             Validated
// ****************************************************************
import cats.Semigroupal
import cats.data.Validated
import cats.implicits._
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}




// Cats provides a data type called `Validated` that has an
// instance of `Semigroupal` which allows it to accumulate errors:

type AllErrorOr[A] = Validated[List[String], A]

Semigroupal[AllErrorOr].product(
  Validated.invalid(List("error 1")),
  Validated.invalid(List("error 2"))
)
// res1: AllErrorOr[(Nothing, Nothing)] = Invalid(List(error 1, error 2))




// `Validated` has 2 subtypes, `Valid` and `Invalid` which are
// equivalent to `Either`'s `Right` and `Left`:

Validated.valid[List[String], Int](123)
// res2: cats.data.Validated[List[String],Int] = Valid(123)

Validated.invalid[List[String], Int](List("error"))
// res3: cats.data.Validated[List[String],Int] = Invalid(List(error))




(
  Vector("error 1").invalid[Int],
  Vector("error 2").invalid[Int]
).tupled
// res5: cats.data.Validated[scala.collection.immutable.Vector[String],(Int, Int)] = Invalid(Vector(error 1, error 2))




123.valid.map(_ * 10)
// res7: cats.data.Validated[Nothing,Int] = Valid(1230)

"error!".invalid.leftMap(_.toUpperCase)
// res9: cats.data.Validated[String,Nothing] = Invalid(ERROR!)




"error".invalid[Int]
// res2: cats.data.Validated[String,Int] = Invalid(error)

"error".invalid[Int].toEither
// res3: Either[String,Int] = Left(error)

"error".invalid[Int].toEither.toValidated
// res4: cats.data.Validated[String,Int] = Invalid(error)



