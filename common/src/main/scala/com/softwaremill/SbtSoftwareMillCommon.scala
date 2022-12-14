package com.softwaremill

import sbt._
import Keys._

object SbtSoftwareMillCommon extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  lazy val commonSmlBuildSettings = Seq(
    libraryDependencies ++= {
      if (ScalaArtifacts.isScala3(scalaVersion.value)) Nil
      else Seq(compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full))
    },
    // silence transitive eviction warnings
    evictionWarningOptions in update := EvictionWarningOptions.empty,
    // use sbt-tpolecat, but without fatal warnings
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    // when using 2.13, fail on non-exhaustive matches
    scalacOptions := {
      val current = scalacOptions.value
      if (scalaVersion.value.startsWith("2.13"))
        current :+ "-Wconf:cat=other-match-analysis:error"
      else current
    }
  )
}
