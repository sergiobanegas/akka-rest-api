name := "Akka-rest-api"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies ++= {
  val akkaVersion = "2.5.12"
  val scalaTestVersion = "3.0.5"
  val scalaMockVersion = "4.1.0"
  val slickVersion = "3.2.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http" % "10.1.1",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1" % Test,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1",
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "org.postgresql" % "postgresql" % "9.4.1209",
    "org.flywaydb" % "flyway-core" % "3.2.1",
    "org.scalatest" %% "scalatest" % scalaTestVersion,
    "org.scalamock" %% "scalamock" % scalaMockVersion,
    "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
    "com.google.code.gson" % "gson" % "1.7.1",
    "org.mindrot" % "jbcrypt" % "0.3m"
  )
}