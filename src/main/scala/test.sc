import Test.MonadState
import cats.effect.concurrent.Ref
import cats.effect.IO
import cats.Monad

val r: IO[Ref[IO, Int]] = Ref.of[IO, Int](4)
r
r.map { ref =>

  ref.s
}
def createMonadState[S](initial: S): IO[MonadState[IO, S]] = {
  for {
    state <- Ref.of[IO, S](initial)
  } yield new MonadState[IO, S] {
    val monad: Monad[IO] = Monad[IO]

    def get: IO[S] = state.get

    def set(s: S): IO[Unit] = state.set(s)
  }
}