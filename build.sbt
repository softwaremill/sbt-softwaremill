import com.softwaremill.Publish
import sbt.addSbtPlugin
import sbt._
import Keys._
import sbtsoftwaremill.BuildInfo

val commonSettings = Publish.ossPublishSettings ++ Seq(
  scalaVersion := "2.12.10",
  organization := "com.softwaremill.sbt-softwaremill",
  sbtVersion in Global := {
    scalaBinaryVersion.value match {
      case "2.10" => "0.13.17"
      case "2.12" => "1.3.2"
    }
  }
)

lazy val root = project.in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(commonSettings)
  .settings(
    name         := "sbt-softwaremill",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
  )
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"          % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"      % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"     % BuildInfo.sbtSonatypeVersion),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover"  % "2.4.3"),
    addSbtPlugin("org.scalameta"     % "sbt-scalafmt"     % BuildInfo.sbtScalafmtVersion),
    addSbtPlugin("io.spray"          % "sbt-revolver"     % "0.9.1"),
    addSbtPlugin("io.get-coursier"   % "sbt-coursier"     % "1.0.3"),
    addSbtPlugin("com.dwijnand"      % "sbt-reloadquick"  % "1.0.0"),
    addSbtPlugin("com.timushev.sbt"  % "sbt-updates"      % "0.4.2"),
    addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "1.3.0")
  )
