import sbt.Keys._
import sbt._

object GenericConf {

  def javaVersionSettings(version: String): Seq[Def.Setting[Task[Seq[String]]]] = Seq(
    javacOptions ++= Seq("-source", version, "-target", version),
    scalacOptions += s"-target:jvm-$version"
  )

  def settings(javaVersion: String = "1.8"): Seq[Setting[_]] = Seq(
    scalaVersion := "2.12.2",
    scalacOptions ++= Seq("-feature", "-language:higherKinds", "-language:implicitConversions", "-deprecation", "-Ydelambdafy:method"),
    javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-Xlink:-warn-missing-interpolator", "-g:vars"),
    cancelable in Global := true
//    parallelExecution in Test := false,
//    fork in Test := true
  ) ++ javaVersionSettings(javaVersion)
}