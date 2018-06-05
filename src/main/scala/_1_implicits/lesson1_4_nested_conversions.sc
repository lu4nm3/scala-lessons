// ****************************************************************
//                    Nested Implicit Conversions
// ****************************************************************




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




// We have seen implicit conversions from one type to another:

implicit def fooToBar(foo: Foo): Bar = Bar()
implicit def barToBaz(bar: Bar): Baz = Baz()

Foo().bar
Bar().baz

// However, just because we have one conversion from `Foo` to `Bar`
// and another from `Bar` to `Baz` does not mean that we can
// convert an instance of `Foo` directly to an instance of `Baz`:

//Foo().baz

// We can get around this by supplying an implicit parameter to the
// conversion methods:

//implicit def fooToBar[F](foo: F)(implicit f: F => Foo): Bar = Bar()
//implicit def barToBaz[B](bar: B)(implicit f: B => Bar): Baz = Baz()

//Foo().baz // Magic! ...Not really

// Here the compiler will first search the implicit scope to see if
// there is an implicit conversion from `Foo` to a type that has
// the method `baz` (ie. the type `Baz`). Such a conversion doesn't
// exist. HOWEVER, there is an implicit conversion between the
// generic type `B` and type `Baz`:

//barToBaz(Foo())

// Next, it will search the implicit scope for an implicit function
// that takes the generic type `B`, which is `Foo` in our case, and
// outputs the type `Bar`:

//barToBaz(Foo())(fooToBar(_))

// You might be wondering what implicit parameter is used for the
// `fooToBar` function. Here, the compiler simply uses the
// `identity` function from the Scala standard library:

//barToBaz(Foo())(fooToBar(_)(identity))




// This approach of doing nested implicit conversions also works
// for implicit classes:

case class ImplicitFoo() {
  def ifoo: String = "foo"
}
implicit class ImplicitBar[F](foo: F)(implicit f: F => ImplicitFoo) {
  def ibar: String = "bar"
}
implicit class ImplicitBaz[F, B](bar: B)(implicit f: B => ImplicitBar[F]) {
  def ibaz: String = "baz"
}

ImplicitFoo().ibaz



