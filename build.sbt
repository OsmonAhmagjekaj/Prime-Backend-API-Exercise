ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """Prime-Backend-API-Exercise""",
    libraryDependencies ++= Seq(
      guice
    )
  )