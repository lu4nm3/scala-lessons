# Scala Lessons

These are a series of short lessons that cover various topics of the Scala programming language, especially as it
relates to functional programming. The lessons are based in part of the open source books by underscore.io along with
additional material and examples. They are by no means meant to provide an exhaustive overview the entirety of Scala but
rather on what I felt were some of the more practical aspects of the language and of functional programming that you
might use your real-world, day-to-day work.

## Setup

You can jump right in and start going through the lessons starting at the beginning or jumping around to the topics that
interest you. I recommend doing this directly from GitHub as it offers pretty good support for markdown and the lessons
themselves have navigation links that let you move around pretty easily. 

In addition, you can also run all of the code snippets on the REPL. I encourage you to do this and to play around with
the examples as it really helps to learn the concepts.

### SBT

If you want to interact with the examples presented in this project using the REPL, you will need to install SBT. Follow
the instructions listed here based on your local environment: https://www.scala-sbt.org/download.html

If you're running on Mac, you can use [Homebrew](https://brew.sh/) to install SBT:

```bash
$ brew install sbt@1
```

### Ammonite

The REPL that I used in the examples throughout the lesson was [Ammonite](http://ammonite.io/#Ammonite-REPL) (denoted by
the `@`) due to the many improvements that it brings over the standard REPL. It works pretty well for the most part but
once in a while you might run into a few minor quirks.

To set it up on your local environment, simply run the following command:

```bash
$ sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/1.3.2/2.12-1.3.2) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm' && amm
```

You will need to clone a copy of this project as the `build.sbt` includes configurations to load the necessary
dependencies that the lessons need:

```bash
$ git clone https://github.com/lu4nm3/scala-lessons.git
```

Afterwards, cd into the root directory and run `sbt test:run` to start a new Ammonite shell session:

```bash
$ cd scala-lessons/
$ sbt test:run
...
@
```

### Default REPL

Alternatively, you can use the default Scala REPL by running `sbt console` from within the root directory of the project
which will start a new shell session:

```bash
$ sbt console
...
scala> 
```

## Table of Contents

<ol>
    <li><h3>Basics</h3>
        <ol>
            <li><a href="src/main/scala/_0_basics/lesson0_1_super_basics.md">Super Basics</a></li>
            <li><a href="src/main/scala/_0_basics/lesson0_2_functions.md">Functions</a></li>
            <li><a href="src/main/scala/_0_basics/lesson0_3_collections.md">Collections</a></li>
            <li><a href="src/main/scala/_0_basics/lesson0_4_classes_traits_objects.md">Classes, Traits, & Objects</a></li>
            <li><a href="src/main/scala/_0_basics/lesson0_5_pattern_matching.md">Pattern Matching</a></li>
        </ol>
    </li>
    <li><h3>Implicits</h3>
        <ol>
            <li><a href="src/main/scala/_1_implicits/lesson1_1_params.md">Parameters</a></li>
            <li><a href="src/main/scala/_1_implicits/lesson1_2_conversions.md">Conversions</a></li>
            <li><a href="src/main/scala/_1_implicits/lesson1_3_classes.md">Classes</a></li>
            <li><a href="src/main/scala/_1_implicits/lesson1_4_nested_conversions.md">Nested Conversions</a></li>
        </ol>
    </li>
    <li><h3>Type Classes</h3>
        <ol>
            <li><a href="src/main/scala/_2_type_classes/lesson2_1_classes.md">The Type Class</a></li>
            <li><a href="src/main/scala/_2_type_classes/lesson2_2_instances.md">Instances</a></li>
            <li><a href="src/main/scala/_2_type_classes/lesson2_3_1_interface_objects.md">Interface Objects</a></li>
            <li><a href="src/main/scala/_2_type_classes/lesson2_3_2_interface_syntax.md">Interface Syntax</a></li>
            <li><a href="src/main/scala/_2_type_classes/lesson2_4_context_bounds.md">Context Bounds</a></li>
            <li><a href="src/main/scala/_2_type_classes/lesson2_5_implicit_scope_packaging.md">Packaging</a></li>
            <li><a href="src/main/scala/_2_type_classes/lesson2_6_recursive_implicit_resolution.md">Recursive Implicit Resolution</a></li>
        </ol>
    </li>
    <li><h3>Functional Programming</h3>
        <ol>
            <li><h4>Basics</h4>
                <ol>
                    <li><a href="src/main/scala/_3_functional_programming_basics/lesson3_1_monoids.md">Monoids</a></li>
                    <li><a href="src/main/scala/_3_functional_programming_basics/lesson3_2_semigroups.md">Semigroups</a></li>
                    <li><a href="src/main/scala/_3_functional_programming_basics/lesson3_3_functors.md">Functors</a></li>
                    <li><a href="src/main/scala/_3_functional_programming_basics/lesson3_4_higher_kinded_types.md">Higher-Kinded Types</a></li>
                </ol>
            </li>
            <li><h4>Monads</h4>
                <ol>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_1_monads.md">Monads</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_2_id.md">Id</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_3_monad_error.md">MonadError</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_4_eval.md">Eval</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_5_writer.md">Writer</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_6_reader.md">Reader</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_7_kleisli.md">Kleisli</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_8_state.md">State</a></li>
                    <li><a href="src/main/scala/_4_functional_programming_monads/lesson4_9_monad_transformers.md">Monad Transformers</a></li>
                </ol>
            </li>
            <li><h4>Applicatives</h4>
                <ol>
                    <li><a href="src/main/scala/_5_functional_programming_applicatives/lesson5_1_semigroupal.md">Semigroupal</a></li>
                    <li><a href="src/main/scala/_5_functional_programming_applicatives/lesson5_2_validated.md">Validated</a></li>
                    <li><a href="src/main/scala/_5_functional_programming_applicatives/lesson5_3_apply_applicative.md">Apply & Applicative</a></li>
                    <li><a href="src/main/scala/_5_functional_programming_applicatives/lesson5_4_foldable.md">Foldable</a></li>
                    <li><a href="src/main/scala/_5_functional_programming_applicatives/lesson5_5_traverse.md">Traverse</a></li>
                </ol>
            </li>
            <li><h4>Patterns</h4>
                <ol>
                    <li><a href="src/main/scala/_6_functional_programming_patterns/lesson6_1_mtl.md">MTL</a></li>
                </ol>
            </li>
        </ol>
    </li>    
</ol>


