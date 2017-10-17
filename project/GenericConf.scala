import sbt.Keys._
import sbt._

object GenericConf {

  def commonSettings: Seq[_root_.sbt.Def.Setting[String]] = Seq(
    organization := "io.demograph"
  )

  def javaVersionSettings(version: String): Seq[Def.Setting[Task[Seq[String]]]] = Seq(
    javacOptions ++= Seq("-source", version, "-target", version),
    scalacOptions += s"-target:jvm-$version"
  )

  def settings(javaVersion: String = "1.8"): Seq[Setting[_]] = commonSettings ++ Seq(
    scalaVersion := "2.12.2",
    scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions", "-deprecation", "-Ydelambdafy:method"),
    javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-Xlink:-warn-missing-interpolator", "-g:vars"),
    cancelable in Global := true,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    //    parallelExecution in Test := false,
//    fork in Test := true
  ) ++ javaVersionSettings(javaVersion)
}