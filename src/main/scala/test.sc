import monix.eval.Task

val foo: String => Task[Int] = s => Task.now(s.toInt)
val bar: Int => Task[Int] = i => Task.now(i * 2)
val baz: Int => Task[Boolean] = i => Task.now(i % 2 == 0)

(s: String) => foo(s) flatMap { v1 =>
  bar(v1) flatMap { v2 =>
    baz(v2)
  }
}

(s: String) => {
  for {
    v1 <- foo(s)
    v2 <- bar(v1)
    result <- baz(v2)
  } yield result
}

(s: String) => for {
  v1 <- foo(s)
  v2 <- bar(v1)
  result <- baz(v2)
} yield result
