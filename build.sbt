import com.softwaremill.Publish
import sbt.{addSbtPlugin, _}
import Keys._
import sbtsoftwaremill.BuildInfo

val commonSettings = Publish.ossPublishSettings ++ Seq(
  scalaVersion := "2.12.20",
  organization := "com.softwaremill.sbt-softwaremill",
  sbtVersion in Global := {
    scalaBinaryVersion.value match {
      case "2.10" => "0.13.17"
      case "2.12" => "1.10.2"
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
  .aggregate(common, publish, extra, browserTestJs)

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
    ),
    addSbtPlugin("org.typelevel" % "sbt-tpolecat" % "0.5.2")
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
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.2"
  )
  .settings(
    addSbtPlugin(
      "com.github.sbt" % "sbt-ci-release" % BuildInfo.sbtCiReleaseVersion
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
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.2"
  )
  .settings(
    addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4"),
    addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "5.1.0")
  )

lazy val browserTestJs = project
  .in(file("browser-test-js"))
  .enablePlugins(SbtPlugin)
  .settings(commonSettings)
  .settings(
    name := "sbt-softwaremill-browser-test-js",
    description := "Build configuration for SBT projects: browser test JS",
    sbtPlugin := true,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value)
  )
  .settings(    
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1"),
    // playwright dependencies, copied from https://github.com/gmkumar2005/scala-js-env-playwright/blob/main/build.sbt
    libraryDependencies ++= Seq(
      "com.microsoft.playwright" % "playwright" % "1.49.0",
      "org.scala-js" %% "scalajs-js-envs" % "1.4.0",
      "com.google.jimfs" % "jimfs" % "1.3.0",
      "com.outr" %% "scribe" % "3.15.2",
      "org.typelevel" %% "cats-effect" % "3.5.7"
    )
  )
