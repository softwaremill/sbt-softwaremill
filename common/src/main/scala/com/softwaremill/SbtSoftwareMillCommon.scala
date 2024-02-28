package com.softwaremill

import sbt._
import Keys._

object SbtSoftwareMillCommon extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  lazy val commonSmlBuildSettings = Seq(
    libraryDependencies ++= {
      if (ScalaArtifacts.isScala3(scalaVersion.value)) Nil
      else Seq(compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full))
    },
    // silence transitive eviction warnings
    update / evictionWarningOptions := EvictionWarningOptions.empty,
    // use sbt-tpolecat, but without fatal warnings
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    // when using 2.13, fail on non-exhaustive matches
    scalacOptions := {
      val current = scalacOptions.value
      if (scalaVersion.value.startsWith("2.13"))
        current :+ "-Wconf:cat=other-match-analysis:error"
      else current
    },
    // scala.js on scala3 needs an additional compiler option
    scalacOptions ++= {
      val isScalaJS = libraryDependencies.value.exists(_.organization == "org.scala-js")
      val isScala3 = ScalaArtifacts.isScala3(scalaVersion.value)
      if (isScalaJS && isScala3) Seq("-scalajs") else Seq()
    }
  )

  object autoImport {}
}
