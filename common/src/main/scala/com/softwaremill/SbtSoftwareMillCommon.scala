package com.softwaremill

import sbt._
import Keys._

object SbtSoftwareMillCommon extends AutoPlugin {
  lazy val isDotty = settingKey[Boolean]("Is the scala version dotty.")

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements
  object autoImport {
    lazy val commonSmlBuildSettings = Seq(
      isDotty := scalaVersion.value.startsWith("0.") || scalaVersion.value.startsWith("3."),
      libraryDependencies ++= {
        if (isDotty.value) Nil else Seq(compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
      },
      // silence transitive eviction warnings
      evictionWarningOptions in update := EvictionWarningOptions.empty,
      // use sbt-tpolecat, but without fatal warnings
      scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
      // when using 2.13, fail on non-exhaustive matches
      scalacOptions := {
        if (scalaVersion.value.startsWith("2.13")) scalacOptions.value :+ "-Wconf:cat=other-match-analysis:error"
        else scalacOptions.value
      }
    )
  }
}
