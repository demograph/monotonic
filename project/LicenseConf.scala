import sbt.Keys._
import sbt._

object LicenseConf {

  lazy val settings = Seq(
    organizationName := "Merlijn Boogerd",
    startYear := Some(2017),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
  )
}