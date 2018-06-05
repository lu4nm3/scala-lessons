// ****************************************************************
//                             Monoids
// ****************************************************************




// Formally, a monoid for type A is:
//
// - an operation `combine` with type (A, A) => A
// - an element `empty` of type A

trait Monoid[A] {
  def combine(x: A, y: A): A
  def empty: A
}

// In addition, a monoid's `combine` must be associative and
// `empty` must be an identity element (ie. when a value is
// combined with `empty` the result must be the original value)




// Integer addition

// `+` is the associative combine operation

(1 + 2) + 3

1 + (2 + 3)

// `0` is the identity element

2 + 0 == 2

0 + 2 == 2

implicit val intAdd: Monoid[Int] = new Monoid[Int] {
  def combine(x: Int, y: Int): Int = x + y
  def empty: Int = 0
}

intAdd.combine(1, 2)
intAdd.combine(2, 1)
intAdd.combine(2, intAdd.empty)
intAdd.combine(intAdd.empty, 2)




// Integer multiplication

// `*` is the associative combine operation

(1 * 2) * 3

1 * (2 * 3)

// `1` is the identity element

2 * 1 == 2

1 * 2 == 2

implicit val intMult: Monoid[Int] = new Monoid[Int] {
  def combine(x: Int, y: Int): Int = x * y
  def empty: Int = 1
}

intMult.combine(1, 2)
intMult.combine(2, 1)
intMult.combine(2, intAdd.empty)
intMult.combine(intAdd.empty, 2)




// String concatenation

// `++` is the associative combine operation

"one" ++ "two"

// `""` (ie. empty string) is the identity element

"" ++ "one" == "one"

"one" ++ "" == "one"

implicit val strConcat: Monoid[String] = new Monoid[String] {
  def combine(x: String, y: String): String = x ++ y
  def empty: String = ""
}

strConcat.combine("one", "two")
strConcat.combine("one", strConcat.empty)
strConcat.combine(strConcat.empty, "one")







