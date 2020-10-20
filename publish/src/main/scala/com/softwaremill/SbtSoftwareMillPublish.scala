package com.softwaremill

import sbt.Keys.{publish, publishArtifact, publishLocal}
import sbt._

object SbtSoftwareMillPublish extends AutoPlugin {
  lazy val isDotty = settingKey[Boolean]("Is the scala version dotty.")

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements
  object autoImport extends Publish with PublishTravis
}
