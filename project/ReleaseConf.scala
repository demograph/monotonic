import com.typesafe.sbt.SbtPgp.autoImportImpl._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

object ReleaseConf {

  lazy val tagName = Def.setting{
    s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
  }

  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

  lazy val publishSettings = Seq(
    homepage := Some(url("https://github.com/mboogerd/monotonic-map")),
    licenses := Seq("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0")),
    scmInfo := Some(ScmInfo(url("https://github.com/mboogerd/monotonic-map"), "scm:git:git@github.com:mboogerd/monotonic-map.git")),
    autoAPIMappings := true,
    pomExtra := (
      <developers>
        <developer>
          <id>merlijn</id>
          <name>Merlijn Boogerd</name>
          <url>https://github.com/mboogerd/</url>
        </developer>
      </developers>
      )
  ) ++ credentialSettings ++ sharedPublishSettings ++ sharedReleaseProcess

  lazy val sharedPublishSettings = Seq(
    releaseCrossBuild := true,
    releaseTagName := tagName.value,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("Snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("Releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )

  lazy val sharedReleaseProcess = Seq(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
      pushChanges)
  )


  lazy val credentialSettings = Seq(
    // For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )
}