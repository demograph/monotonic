lazy val api = project.in(file("api"))
  .settings(
    inThisBuild(List(
      organization := "com.github.mboogerd",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "MonotonicMap")
  .settings(GenericConf.settings())
  .settings(DependenciesConf.common)
  .settings(LicenseConf.settings)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(TutConf.settings)
  .enablePlugins(TutPlugin)

lazy val mem = project.in(file("mem"))
  .settings(GenericConf.settings())
  .settings(DependenciesConf.common)
  .settings(DependenciesConf.akka)
  .settings(LicenseConf.settings)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(TutConf.settings)
  .enablePlugins(TutPlugin)
  .dependsOn(api)
  .aggregate(api)