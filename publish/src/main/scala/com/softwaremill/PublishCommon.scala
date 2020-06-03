package com.softwaremill

import sbt.settingKey
import sbtrelease.ReleasePlugin.autoImport.ReleaseStep

trait PublishCommon {
  val beforeCommitSteps = settingKey[Seq[ReleaseStep]](
    "List of release steps to execute before committing a new release."
  )
}
