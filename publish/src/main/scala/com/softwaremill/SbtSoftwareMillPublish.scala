package com.softwaremill

import sbt._

object SbtSoftwareMillPublish extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {}
}
