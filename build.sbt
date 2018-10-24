lazy val scalaLessons = (project in file("."))
  .settings(
    name := "scala-lessons",
    scalacOptions := Seq("-feature", "-Ypartial-unification"),
    scalacOptions += "-Ypartial-unification",
    scalaVersion := "2.12.6"
  )
  .settings(resolvers ++=
    Seq(
      Resolver.sonatypeRepo("releases")
    )
  )
  .settings(libraryDependencies ++=
    Seq(
      "com.lihaoyi" % "ammonite" % "1.2.1" % "test" cross CrossVersion.full,
      git      "io.monix" % "monix_2.12" % "3.0.0-RC1",
      "org.typelevel" %% "cats-core" % "1.1.0" withSources() withJavadoc(),
      "org.typelevel" %% "cats-effect" % "1.0.0-RC2" withSources() withJavadoc(),
      "org.typelevel" %% "cats-mtl-core" % "0.2.1" withSources() withJavadoc()
    )
  )
  .settings(
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7"),
//    addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary)
  )
  .settings(
    sourceGenerators in Test += Def.task {
      val file = (sourceManaged in Test).value / "amm.scala"
      IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
      Seq(file)
    }.taskValue
  )
