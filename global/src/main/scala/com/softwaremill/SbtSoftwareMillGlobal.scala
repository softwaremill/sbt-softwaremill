package com.softwaremill

import sbt._
import Keys._

object SbtSoftwareMillGlobal extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements
  object autoImport extends Base

  class Base {
    lazy val clippyBuildSettings = Seq(
      com.softwaremill.clippy.ClippySbtPlugin.clippyColorsEnabled := true
    )

    lazy val splainSettings = Seq(
      libraryDependencies += {
        val splainVersion = CrossVersion.partialVersion(scalaVersion.value) match {
          // splain has dropped support of Scala 2.10 with version 0.3.1
          // last version supporting Scala 2.10 is splain 0.2.10
          case Some((2, 11)) | Some((2, 12)) =>
            "0.3.3"
          case _ =>
            "0.2.10"
        }
        compilerPlugin("io.tryp" % "splain" % splainVersion cross CrossVersion.patch)
      },
      scalacOptions += "-P:splain:all"
    )

    lazy val dependencyUpdatesSettings = Seq(
      // onLoad is scoped to Global because there's only one.
      onLoad in Global := {
        val old = (onLoad in Global).value
        // compose the new transition on top of the existing one
        // in case your plugins are using this hook.
        CheckUpdates.startupTransition(organization.value + "_" + name.value) compose old
      }
    )

    lazy val smlGlobalBuildSettings =
      clippyBuildSettings ++
        splainSettings ++
        dependencyUpdatesSettings
  }
}
