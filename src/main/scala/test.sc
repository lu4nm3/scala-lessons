import cats.data.{StateT, WriterT}
import cats.effect.IO
import cats.{Monad, Monoid}
import cats.implicits._

import scala.language.higherKinds

def createNewS[F[_], L, S](initialState: S)(implicit monad: Monad[F], monoid: Monoid[L]): StateT[WriterT[F, L, ?], S, Unit] = {
  StateT.set[WriterT[F, L, ?], S](initialState)
}

val s = createNewS[IO, Vector[String], Int](3).runEmptyS//.run.unsafeRunSync()

val s2 = for {
  x <- StateT.set[WriterT[IO, Vector[String], ?], Int](3).get
  y <- createNewS[IO, Vector[String], Int](2).get
} yield x * y

s2.run(0).run.unsafeRunSync()
//s.flatMap()