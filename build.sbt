import com.softwaremill.Publish
import com.softwaremill.PublishTravis
import sbt.{addSbtPlugin, _}
import Keys._
import sbtsoftwaremill.BuildInfo

val commonSettings = Publish.ossPublishSettings ++ Seq(
  scalaVersion := "2.12.12",
  organization := "com.softwaremill.sbt-softwaremill",
  sbtVersion in Global := {
    scalaBinaryVersion.value match {
      case "2.10" => "0.13.17"
      case "2.12" => "1.3.8"
    }
  }
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "sbt-softwaremill-root",
    description := "Build configuration for SBT projects"
  )
  .settings(Publish.noPublishSettings)
  .settings(PublishTravis.publishTravisSettings)
  .aggregate(common, publish, extra)

lazy val common = project
  .in(file("common"))
  .enablePlugins(SbtPlugin)
  .settings(commonSettings)
  .settings(
    name := "sbt-softwaremill-common",
    description := "Build configuration for SBT projects: common",
    sbtPlugin := true,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value)
  )
  .settings(
    addSbtPlugin(
      "org.scalameta" % "sbt-scalafmt" % BuildInfo.sbtScalafmtVersion
    )
  )

lazy val publish = project
  .in(file("publish"))
  .enablePlugins(SbtPlugin)
  .settings(commonSettings)
  .settings(
    name := "sbt-softwaremill-publish",
    description := "Build configuration for SBT projects: publishing",
    sbtPlugin := true,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1"
  )
  .settings(
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % BuildInfo.sbtPgpVersion),
    addSbtPlugin(
      "com.github.gseitz" % "sbt-release" % BuildInfo.sbtReleaseVersion
    ),
    addSbtPlugin(
      "org.xerial.sbt" % "sbt-sonatype" % BuildInfo.sbtSonatypeVersion
    )
  )

lazy val extra = project
  .in(file("extra"))
  .enablePlugins(SbtPlugin)
  .settings(commonSettings)
  .settings(
    name := "sbt-softwaremill-extra",
    description := "Build configuration for SBT projects: extra",
    sbtPlugin := true,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1"
  )
  .settings(
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.13"),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1"),
    addSbtPlugin("com.dwijnand" % "sbt-reloadquick" % "1.0.0"),
    addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.1"),
    addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "2.1.0")
  )
