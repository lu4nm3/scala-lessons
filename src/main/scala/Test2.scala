import Test.AppState
import cats.data.{IndexedStateT, StateT, WriterT}
import cats.effect.IO
import cats.implicits._
import cats.{Monad, Monoid}

import scala.language.higherKinds

object Test2 extends App {

  //  type _WriterT[A] = WriterT[IO, Vector[String], A]
  //  type _StateT[S, A] = StateT[_WriterT, S, A]

  //  val s1: _WriterT[(Int, Int)] = StateT.pure[_WriterT, Int, Int](8).run(30)//.run//.run()
  //  val w: IO[(Vector[String], (Int, Int))] = WriterT.value[IO, Vector[String], (Int, Int)]((3, 3)).run

  trait Operation[F[_], L, SA, SB, A] {

    def wrapped: IndexedStateT[WriterT[F, L, ?], SA, SB, A]

//    def run: F[SA => F[(L, SB, A)]]

    //      def map[SB](f: SA => SB)(implicit M: Monad[WriterT[F, L, ?]]): Operation[F, L, SB] = {
    //        val currentWrapped = wrapped
    //
    //        new Operation[F, L, SB] {
    //          val wrapped: IndexedStateT[WriterT[F, L, ?], SB, SB, SB] = currentWrapped.bimap(f, f)//.modify(f)//.map(f)
    //        }
    //      }
    //
    //      def map[B](f: A => B)(implicit F: Functor[F]): Operation[F, L, SA, SB, B] = {
    //        transform { case (s, a) => (s, f(a)) }
    //      }

    def flatMap[SC, B](f: A => Operation[F, L, SB, SC, B])(implicit M: Monad[WriterT[F, L, ?]]): Operation[F, L, SA, SC, B] = {
      val currentWrapped = wrapped

//      M.map(wrapped.){_ => 3}

      new Operation[F, L, SA, SC, B] {
        val wrapped: IndexedStateT[WriterT[F, L, ?], SA, SC, B] = currentWrapped.flatMap(s => f(s).wrapped)
      }

//      null
    }

    //      def pure(initialState: SB)(implicit monad: Monad[F], monoid: Monoid[L]): Operation[F, L, SA, SB, A] = {
    //        new Operation[F, L, SA, SB, A] {
    //          val wrapped: IndexedStateT[WriterT[F, L, ?], SA, SB, A] = {
    //            StateT[_WriterT, S, S](_ =>
    //              WriterT[F, L, (S, S)](
    //                monad.pure(monoid.empty, (initialState, initialState))
    //              )
    //            )
    //            IndexedStateT.set[WriterT[F, L, ?], SA, SB](initialState)
    //          }
    //        }
    //      }
    //
    //      def map[SB](f: S => SB)(implicit F: Functor[F]): Operation[F, L, SB] = {
    //        val currentWrapped = wrapped
    //        flatMap(a => pure(f(a)))
    //      }
  }

//  object Operation {
//    def applyF[F[_], L, SA, SB, A](runF: F[SA => F[(L, SB, A)]]): Operation[F, L, SA, SB, A] =
//      new Operation[F, L, SA, SB, A] {
//        val run: F[SA => F[(L, SB, A)]] = runF
//      }
//  }

  /**
    * StateT[WriterT[F, L, ?], S, A]
    *
    * Here, StateT expects a type conts
    *
    * WriterT[F, L, (S, A)]  // after calling `.run` on StateT
    * WriterT[IO, Vector[String], (S, A)]
    *
    * IO[(Vector[String], (S, A))]
    */
  def createNew[F[_], L, S, A](initialState: S,
                               initialResult: A)(implicit monad: Monad[F], monoid: Monoid[L]): StateT[WriterT[F, L, ?], S, A] = {

    val s1: WriterT[F, L, (S, A)] = StateT.pure[WriterT[F, L, ?], S, A](initialResult).run(initialState) //.run//.run()
    val w: IO[(Vector[String], (Int, Int))] = WriterT.value[IO, Vector[String], (Int, Int)]((3, 3)).run
    StateT[WriterT[F, L, ?], S, A](_ =>
      WriterT[F, L, (S, A)](
        monad.pure(monoid.empty, (initialState, initialResult))
      )
    )
  }

  def createNewS[F[_], L, S](initialState: S)(implicit monad: Monad[F], monoid: Monoid[L]): StateT[WriterT[F, L, ?], S, Unit] = {

    val s1: WriterT[F, L, (S, S)] = StateT.pure[WriterT[F, L, ?], S, S](initialState).run(initialState) //.run//.run()
    val w: IO[(Vector[String], (Int, Int))] = WriterT.value[IO, Vector[String], (Int, Int)]((3, 3)).run
    StateT.set[WriterT[F, L, ?], S](initialState)
    //    StateT[WriterT[F, L, ?], S, S](_ =>
    //      WriterT.pure[F](
    //        (initialState, initialState)
    //        //        monad.pure(monoid.empty, (initialState, initialState))
    //      )
    ////      WriterT[F, L, (S, S)](
    ////        monad.pure(monoid.empty, (initialState, initialState))
    ////      )
    //    )
  }

  def get[F[_], L, S](initialState: S)(implicit monad: Monad[F], monoid: Monoid[L]): StateT[WriterT[F, L, ?], S, S] = {

    val s1: WriterT[F, L, (S, S)] = StateT.pure[WriterT[F, L, ?], S, S](initialState).run(initialState) //.run//.run()
    val w: IO[(Vector[String], (Int, Int))] = WriterT.value[IO, Vector[String], (Int, Int)]((3, 3)).run
    StateT[WriterT[F, L, ?], S, S](_ =>
      WriterT[F, L, (S, S)](
        monad.pure(monoid.empty, (initialState, initialState))
      )
    )
  }

  //  def createNew[F[_], L, S, A](initialState: S,
  //                               initialResult: A)(implicit monad: Monad[F], monoid: Monoid[L]): StateT[WriterT[F, L, A], S, A] = {
  //    StateT[WriterT[F, L, A], S, A](_ =>
  //      WriterT[F, L, (S, A)](
  //        monad.pure(monoid.empty, (initialState, initialResult))
  //      )
  //    )
  //  }

  import cats.implicits._
  //  def createSpecific[S, A](initialState: S,
  //                           initialResult: A): StateT[WriterT[IO, Vector[String], ?], S, A] = {
  //    createNew(initialState, initialResult)(Monad[IO], Monoid[Vector[String]])
  //  }


  def program[F[_]](s: StateT[WriterT[F, Vector[String], ?], AppState, Unit])(implicit monad: Monad[F]) = {
    val result = for {
      x <- s.get
      _ <- s.modify(_ => x)
    } yield {
      x

    }

    //    result.run(AppState(2)).map{case (logs, (state, result)) => (logs, state)}
  }

  //  def main() = {
  //    for {
  //      result <- program[IO](createNewS(AppState(3)))
  //    } yield result
  //  }

  //  val result = main().unsafeRunSync()
  // _StateT[S, A]
  // StateT[_WriterT, S, A]
  // _WriterrT[State[S, A]]
  // WriterT[_MonadError, Vector[String], State[S, A]]

  println("hi")

  //  type WT[A] = WriterT
}
