<h4 align="right">
    <a href="lesson1_1_params.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson1_3_classes.md">Next →</a>
</h4>

<h1>Implicit Conversions</h1>

You can tag any single-arg method with `implicit` keyword to allow compiler to automatically convert the input type to 
the output type:

```scala
class Greet(name: String) {
  def hello: String = s"hello $name!"
}

import scala.language.implicitConversions

implicit def stringToGreet(s: String): Greet = new Greet(s)
```

Then we can try calling the `hello` method on a string:

```scala
@ "bob".hello
res1: String = hello bob!
```
Here the `String` type doesn't have a method called `hello`. So the compiler will search the implicit scope to see if 
there are any implicit conversions that it can use to convert `String` to another type that does have a method `hello`.

Be mindful when using implicit conversions as they can cause problems if you are not careful. It's easy to define very 
general implicit conversions that can mess with the semantics of a program. This is part of the reason why the compiler 
requires you to use the special `scala.language.implicitConversions` import when using implicit conversions.

Let's take the following implicit conversion as an example:

```scala
import scala.language.implicitConversions

implicit def intToBoolean(i: Int): Boolean = i == 0
```

Now let's use an `Int` somewhere that a `Boolean` is expected:

```scala
@ if (1) "yes" else "no"
res3: String = no

@ if (0) "yes" else "no"
res4: String = yes
```

Even though the following is a contrived example, it demonstrates what can happen if you're not careful when using
implicit conversions.

<h4 align="right">
    <a href="lesson1_1_params.md">← Previous</a> |
    <a href="../../../../README.md">Menu</a> |
    <a href="lesson1_3_classes.md">Next →</a>
</h4>