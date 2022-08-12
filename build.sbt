name := "backend-exercise"

ThisBuild / version := "1.0.0"

routesGenerator := InjectedRoutesGenerator

ThisBuild / scalaVersion := "2.13.8"

val akkaManagementVersion = "1.1.3"
val akkaVersion = "2.6.19"
val akkaHTTPVersion = "10.2.9"

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

libraryDependencies ++= Seq(
  guice,

  //JSON Web Token
  "com.auth0" % "java-jwt" % "3.3.0",

  "org.projectlombok" % "lombok" % "1.18.12",
  "org.glassfish" % "javax.el" % "3.0.0",
  "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.hibernate" % "hibernate-validator" % "6.1.5.Final",

  // akka related stuff
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,

// akka http related stuff
"com.typesafe.akka" %% "akka-http-core" % akkaHTTPVersion,
"com.typesafe.akka" %% "akka-http2-support" % akkaHTTPVersion,
"com.typesafe.akka" %% "akka-http-spray-json" % akkaHTTPVersion,
"com.typesafe.akka" %% "akka-http" % akkaHTTPVersion,

// akka cluster related stuff
"com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
"com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion
)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file(".")).enablePlugins(PlayScala)