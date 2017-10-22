import sbt.Keys._
import sbt._


object DependenciesConf {

  lazy val scala: Seq[Setting[_]] = Seq(
    scalaVersion := "2.12.4",
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      Resolver.sonatypeRepo("releases")
    )
  )

  lazy val common: Seq[Setting[_]] = scala ++ Seq(
    libraryDependencies ++= commonDeps
  )

  lazy val akka: Seq[Setting[_]] = scala ++ Seq(
    libraryDependencies ++= akkaDeps
  )

  def commonDeps = {
    val catsVersion = "1.0.0-MF"
    val refinedVersion = "0.8.4"
    val pureconfigVersion = "0.7.2"
    val algebraVersion = "0.7.0"
    Seq(
      // logging
      "ch.qos.logback" % "logback-classic" % "1.2.3",

      // scala
      "org.log4s" %% "log4s" % "1.4.0",
      "com.github.pathikrit" %% "better-files" % "3.2.0",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.github.nscala-time" %% "nscala-time" % "2.16.0",

      // typelevel
      "com.chuusai" %% "shapeless" % "2.3.2",
      "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-enumeratum" % pureconfigVersion,
      "eu.timepit" %% "refined" % refinedVersion,
      "eu.timepit" %% "refined-pureconfig" % refinedVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-kernel" % catsVersion,
      "org.typelevel" %% "cats-macros" % catsVersion,
      "org.typelevel" %% "cats-laws" % catsVersion,
      "org.typelevel" %% "cats-effect" % "0.4",
      "org.typelevel" %% "alleycats-core" % "0.2.0",
      "org.typelevel" %% "algebra" % algebraVersion,
      "org.typelevel" %% "algebra-laws" % algebraVersion,
      "org.typelevel" %% "spire-extras" % "0.14.1",

      // reactive streams
      "org.reactivestreams" % "reactive-streams" % "1.0.1",

      // test
      "org.scalatest" %% "scalatest" % "3.2.0-SNAP9" % Test,
      "org.scalacheck" %% "scalacheck" % "1.13.5" % Test,
      "org.typelevel" %% "cats-testkit" % catsVersion % Test,
      "eu.timepit" %% "refined-scalacheck" % refinedVersion % Test,
      "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test
    )
  }

  def akkaDeps = {
    val akkaVersion = "2.5.6"
    Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
    )
  }
}