import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val akka = "com.typesafe.akka" %% "akka-actor" % "2.5.25"
  lazy val akkaTest = "com.typesafe.akka" %% "akka-testkit" % "2.5.25"
}
