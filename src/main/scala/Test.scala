import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Monad, Monoid}

import scala.language.{higherKinds, implicitConversions}

object Test extends App {

  trait MonadState[F[_], S] {
    def monad: Monad[F]

    def get: F[S]

    def set(s: S): F[Unit]
  }

  trait MonadWriter[F[_], L] {
    def monad: Monad[F]

    def tell(l: L): F[Unit]

    def logs: F[L]
  }

  def createState[S](initial: S): IO[MonadState[IO, S]] = {
    for {
      state <- Ref.of[IO, S](initial)
    } yield new MonadState[IO, S] {
      def monad: Monad[IO] = Monad[IO]

      def get: IO[S] = state.get

      def set(s: S): IO[Unit] = state.set(s)
    }
  }

  def createWriter[L](implicit monoid: Monoid[L]): IO[MonadWriter[IO, L]] = {
    for {
      writer <- Ref.of[IO, L](monoid.empty)
    } yield new MonadWriter[IO, L] {
      def monad: Monad[IO] = Monad[IO]

      def tell(l: L): IO[Unit] = writer.update(_.combine(l))

      def logs: IO[L] = writer.get
    }
  }

  case class AppState(value: Int)

  def program[F[_]](implicit S: MonadState[F, AppState],
                       W: MonadWriter[F, Vector[String]]): F[(Vector[String], AppState)] = {
    implicit val monad: Monad[F] = S.monad

    for {
      state <- S.get

      _ <- W.tell(Vector(s"Initial state value: ${state.value}"))

      _ <- S.set(state.copy(state.value * 3))

      _ <- W.tell(Vector(s"Program complete"))

      logs <- W.logs
      finalState <- S.get
    } yield (logs, finalState)
  }

  def main() = {
    for {
      monadState <- createState(AppState(3))
      monadWriter <- createWriter[Vector[String]]
      result <- program(monadState, monadWriter)
    } yield result
  }

  val op = main()

  val runtime = Runtime.getRuntime()
  runtime.gc()

  val bytesBefore = runtime.totalMemory() - runtime.freeMemory()
  val megaBytesBefore = bytesBefore / (1024L * 1024L)

  for (i <- 1 to 10000) {
    op.unsafeRunSync()
  }

  val res = op.unsafeRunSync()

  val bytesAfter = runtime.totalMemory() - runtime.freeMemory()
  val megaBytesAfter = bytesAfter / (1024L * 1024L)

  val result = main().unsafeRunSync()
  println("hi" + res)

  while (true){
    op.unsafeRunSync()
//    Thread.sleep(100)
  }
}
