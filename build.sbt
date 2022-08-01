ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """Prime-Backend-API-Exercise""",
    libraryDependencies ++= Seq(
      guice,

      //JSON Web Token
      "com.auth0" % "java-jwt" % "3.3.0",

      "org.projectlombok" % "lombok" % "1.18.12",
      "org.glassfish" % "javax.el" % "3.0.0",
      "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
      "org.mindrot" % "jbcrypt" % "0.4"
    )
  )
