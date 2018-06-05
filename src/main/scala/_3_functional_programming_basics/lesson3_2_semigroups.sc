// ****************************************************************
//                            Semigroups
// ****************************************************************




// A semigroup is just the `combine` part of a monoid.

trait Semigroup[A] {
  def combine(x: A, y: A): A
}

// While many semigroups are also monoids, there are some data
// types for which we cannot define and `empty` element. For
// example, Cats has a `NonEmptyList` data type which is a list
// that by definition can not be empty. It has an implementation of
// `Semigroup` but no implementation of `Monoid`




// A more accurate definitions of Cats' Monoid is:

trait Monoid[A] extends Semigroup[A] {
  def empty: A
}


