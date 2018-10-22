# Collections

The Scala standard library supports a number of collections out-of-the-box:

```scala
@ List("one", "two", "three")
res0: List[String] = List("one", "two", "three")

@ Set("one", "two", "two", "three")
res1: Set[String] = Set("one", "two", "three")

@ Map("one" -> 1, "two" -> 2, "three" -> 3)
res2: Map[String, Int] = Map("one" -> 1, "two" -> 2, "three" -> 3)

@ ("three", 3)
res3: (String, Int) = ("three", 3)

@ Tuple2("three", 3)
res4: (String, Int) = ("three", 3)
```

By default, Scala exposes the immutable version of these collections:
 
```scala
@ val l = List(1, 2, 3)
l: List[Int] = List(1, 2, 3)

@ l :+ 4 // creates a new List containing the added element
res6: List[Int] = List(1, 2, 3, 4)

@ l
res7: List[Int] = List(1, 2, 3)
```
 
However, mutable versions also exist which can be found in the `scala.collection.mutable` package:

```scala
import scala.collection.mutable.ListBuffer

@ val l = ListBuffer(1, 2, 3)
l: ListBuffer[Int] = ListBuffer(1, 2, 3)

@ l += 4
res11: ListBuffer[Int] = ListBuffer(1, 2, 3, 4)

@ l
res12: ListBuffer[Int] = ListBuffer(1, 2, 3, 4)
```

## Iterating over collections

Normally, in an imperative language like Java, you would iterate over a list by using a loop.

For example, let's say that we wanted to multiply every element of a list by 2. In Java we would normally have to 1)
create a new empty list that we will populate with new elements 2) use some sort of loop to iterate over the existing
collection and 3) multiply each element by 2 and add it to our new list:

```java
import java.util.List;
import java.util.ArrayList;

List<Integer> input = Arrays.asList(1, 2, 3);
List<Integer> result = new ArrayList<>();

for (Integer i: input) {
    result.add(i * 2);
}
```

In Scala, on the other hand, we can use the `map` method to apply a function to every element in a list which results in
a new list with updated elements:

```scala
@ List(1, 2, 3).map(i => i * 2)
res13: List[Int] = List(2, 4, 6)
```

Java 8 improved things a bit with the new streams API which has a `map` method, however syntactically, things are not as
concise as Scala:

```java
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

List<Integer> input = Arrays.asList(1, 2, 3);

input.stream().map(i -> i * 2).collect(Collectors.toList());
```

There is also `foreach` which is similar to `map` except that it's used when you need to iterate over all the elements
of a collection without modifying them:

```scala
@ List(1, 2, 3).foreach(i => println(i))
1
2
3
```

In addition, there are other methods like `flatMap`, `filter`, `foldLeft`, `foldRight`, etc which you can use to
manipulate collections in different ways.