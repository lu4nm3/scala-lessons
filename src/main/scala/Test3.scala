import Test.AppState
import cats.data.{StateT, WriterT}
import cats.effect.IO
import cats.implicits._
import cats.{Applicative, FlatMap, Functor, Monad, Monoid, Semigroup}

import scala.language.higherKinds

object Test3 extends App {

  final class WriterStateT[F[_], L, SA, SB, A](val runF: F[SA => F[(L, SB, A)]]) {
    def flatMap[SC, B](f: A => WriterStateT[F, L, SB, SC, B])(implicit F: FlatMap[F], L: Semigroup[L]): WriterStateT[F, L, SA, SC, B] = {
      WriterStateT.applyF[F, L, SA, SC, B] {
        F.map(runF) { wsfa =>
          (sa: SA) => {
            F.flatMap(wsfa(sa)) { case (la, sb, a) =>
              F.flatMap(f(a).runF) { wsfb =>
                F.map(wsfb(sb)) { case (lb, sc, b) =>
                  (L.combine(la, lb), sc, b)
                }
              }
            }
          }
        }
      }
    }

    /**
      * Modify the result of the computation using `f`.
      */
    def map[B](f: A => B)(implicit F: Functor[F]): WriterStateT[F, L, SA, SB, B] = {
      transform { (l, s, a) => (l, s, f(a)) }
    }

    /**
      * Transform the resulting log, state and value using `f`.
      */
    def transform[LL, SC, B](f: (L, SB, A) => (LL, SC, B))(implicit F: Functor[F]): WriterStateT[F, LL, SA, SC, B] = {
      WriterStateT.applyF[F, LL, SA, SC, B] {
        F.map(runF) { wsfa =>
          (sa: SA) =>
            F.map(wsfa(sa)) { case (l, sb, a) =>
              val (ll, sc, b) = f(l, sb, a)
              (ll, sc, b)
            }
        }
      }
    }

    /**
      * Run the computation using the provided initial state.
      */
    def run(initial: SA)(implicit F: Monad[F]): F[(L, SB, A)] = {
      F.flatMap(runF)(_.apply(initial))
    }
  }

  object WriterStateT {
    def apply[F[_], L, SA, SB, A](runF: SA => F[(L, SB, A)])(implicit F: Applicative[F]): WriterStateT[F, L, SA, SB, A] = {
      new WriterStateT[F, L, SA, SB, A](F.pure(runF))
    }

    def applyF[F[_], L, SA, SB, A](runF: F[SA => F[(L, SB, A)]]): WriterStateT[F, L, SA, SB, A] = {
      new WriterStateT[F, L, SA, SB, A](runF)
    }

    def pure[F[_], L, S, A](a: A)(implicit F: Applicative[F], L: Monoid[L]): WriterStateT[F, L, S, S, A] = {
      WriterStateT(s => F.pure((L.empty, s, a)))
    }

    /**
      * Return the input state without modifying it.
      */
    def get[F[_], L, S](implicit F: Applicative[F], L: Monoid[L]): WriterStateT[F, L, S, S, S] = {
      WriterStateT(s => F.pure((L.empty, s, s)))
    }

    /**
      * Set the state to `s`.
      */
    def set[F[_], L, S](s: S)(implicit F: Applicative[F], L: Monoid[L]): WriterStateT[F, L, S, S, Unit] = {
      WriterStateT(_ => F.pure((L.empty, s, ())))
    }

    /**
      * Add a value to the log, without modifying the input state.
      */
    def tell[F[_], L, S](l: L)(implicit F: Applicative[F]): WriterStateT[F, L, S, S, Unit] = {
      WriterStateT(s => F.pure((l, s, ())))
    }
  }

  /**
    * StateT[WriterT[F, L, ?], S, A]
    *
    * WriterT[F, L, (S, A)]  // after calling `.run` on StateT
    *
    * F[(L, (S, A))] // after calling `.run` on WriterT
    *
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

  def program[F[_]](implicit M: Monad[F]): WriterStateT[F, Vector[AppState], AppState, AppState, AppState] = {
    for {
      state <- WriterStateT.get[F, Vector[AppState], AppState]

      _ <- WriterStateT.tell[F, Vector[AppState], AppState](Vector(state))

      x <- WriterStateT.pure[F, Vector[AppState], AppState, AppState](AppState(state.value * 3))  // some "computation" that uses the program's state
      _ <- WriterStateT.set[F, Vector[AppState], AppState](x)                                     // update the state with a new value
      _ <- WriterStateT.tell[F, Vector[AppState], AppState](Vector(x))                            // log the value of the new state

      state2 <- WriterStateT.get[F, Vector[AppState], AppState]

      y <- WriterStateT.pure[F, Vector[AppState], AppState, AppState](AppState(state2.value + 2))
      _ <- WriterStateT.set[F, Vector[AppState], AppState](y)
      _ <- WriterStateT.tell[F, Vector[AppState], AppState](Vector(y))

      finalState <- WriterStateT.get[F, Vector[AppState], AppState]
    } yield finalState
  }


  val op = program[IO].run(AppState(3))

  val runtime = Runtime.getRuntime()
  runtime.gc()

  val bytesBefore = runtime.totalMemory() - runtime.freeMemory()
  val megaBytesBefore = bytesBefore / (1024L * 1024L)

  for (i <- 1 to 10000){
    op.unsafeRunSync()
  }

  val res = op.unsafeRunSync()

  val bytesAfter = runtime.totalMemory() - runtime.freeMemory()
  val megaBytesAfter = bytesAfter / (1024L * 1024L)

  val result = program[IO].run(AppState(3)).unsafeRunSync()
  println("hi" + res)

  while (true){
    op.unsafeRunSync()
//    Thread.sleep(100)
  }
}
