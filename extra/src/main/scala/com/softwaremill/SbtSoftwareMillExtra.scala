package com.softwaremill

import sbt._
import Keys._
import net.vonbuchholtz.sbt.dependencycheck.DependencyCheckPlugin

object SbtSoftwareMillExtra extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements
  object autoImport {
    lazy val dependencyUpdatesSettings = Seq(
      // onLoad is scoped to Global because there's only one.
      onLoad in Global := {
        val old = (onLoad in Global).value
        // compose the new transition on top of the existing one
        // in case your plugins are using this hook.
        CheckUpdates.startupTransition(organization.value + "_" + name.value) compose old
      }
    )

    lazy val dependencyCheckSettings = Seq(
      DependencyCheckPlugin.autoImport.dependencyCheckCveUrlModified := Some(new URL("http://nvdmirror.sml.io/")),
      DependencyCheckPlugin.autoImport.dependencyCheckCveUrlBase := Some("http://nvdmirror.sml.io/"),
      DependencyCheckPlugin.autoImport.dependencyCheckAssemblyAnalyzerEnabled := Some(false),
      DependencyCheckPlugin.autoImport.dependencyCheckFormat := "All"
    )

    lazy val extraSmlBuildSettings =
        dependencyUpdatesSettings ++
        dependencyCheckSettings
  }
}
