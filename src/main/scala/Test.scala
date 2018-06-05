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

  fooToBar(Foo())
  println(Foo().baz)
}