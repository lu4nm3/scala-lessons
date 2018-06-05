lazy val scalaLessons = (project in file("."))
  .settings(
    name := "scala-lessons",
    scalacOptions := Seq("-feature", "-Ypartial-unification"),
    scalaVersion := "2.12.6"
  )
  .settings(libraryDependencies ++=
    Seq(
      "com.typesafe.play" %% "play" % "2.6.13" withSources() withJavadoc(),
      "io.monix" % "monix_2.12" % "3.0.0-RC1",
      "org.typelevel" %% "cats-core" % "1.1.0" withSources() withJavadoc(),
      "org.typelevel" %% "cats-effect" % "1.0.0-RC" withSources() withJavadoc()
    )
  )