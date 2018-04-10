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
      addCompilerPlugin("io.tryp" % "splain" % "0.2.10" cross CrossVersion.patch),
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
