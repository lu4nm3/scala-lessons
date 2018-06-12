import java.util.concurrent.TimeUnit

import cats.Monad
import monix.eval.Task

import scala.concurrent.duration.Duration
import scala.language.higherKinds

object Test extends App {
  import scala.language.implicitConversions

  case class Foo() {
    def foo: String = "foo"
  }
  case class Bar() {
    def bar: String = "bar"
  }
  case class Baz() {
    def baz: String = "baz"
  }

  implicit def fooToBar[F](foo: F)(implicit f: F => Foo): Bar = {
    println(implicitly[F => Foo](f))
    Bar()
  }
  implicit def barToBaz[B](bar: B)(implicit f: B => Bar): Baz = Baz()


  trait MonadState[F[_], S] extends Monad[F] { self =>
    def get: F[S]

    def put(s: S): F[Unit]
  }

  case class AppState(values: List[Int])
import cats.implicits._
  def program[F[_]](implicit S: MonadState[F, AppState]): F[Unit] = {
    for {
      appState <- S.get
      _ <- S.put(appState)
//      x = s.values
//      _ <- S.put(state.copy)
    } yield ()
  }

  implicit val mapTaskMonadState: MonadState[Task, AppState] = new MonadState[Task, AppState] {
    var state = AppState(List.empty[Int])

    def get: Task[AppState] = Task.now(state)

    def put(s: AppState): Task[Unit] = Task.now { state = s }

    def flatMap[A, B](fa: Task[A])(f: A => Task[B]) = fa.flatMap(f)

    def tailRecM[A, B](a: A)(f: A => Task[Either[A, B]]) = Task.tailRecM(a)(f)

    def pure[A](x: A) = Task.pure(x)
  }


  import monix.execution.Scheduler.Implicits.global

  val x = program[Task].runSyncUnsafe(Duration(3, TimeUnit.SECONDS))//()

  fooToBar(Foo())
  println(Foo().baz)
}