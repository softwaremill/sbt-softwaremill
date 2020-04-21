package com.softwaremill

import sbt._, Keys._
import com.typesafe.sbt.SbtPgp.autoImportImpl._
import com.softwaremill.Publish.Release._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

trait PublishTravis {
  // release entry points

  val commitRelease = taskKey[Unit](
    "Update version.sbt, change docs, create git tag, commit and push changes"
  )
  val publishRelease = taskKey[Unit](
    "Publish the current release (basing on version.sbt) to sonatype"
  )

  //

  val isCommitRelease =
    settingKey[Boolean](
      "A hacky way to differentiate between commitRelease and publishRelease invocations."
    )

  lazy val publishTravisSettings = Seq(
    isCommitRelease := true,
    useGpg := false, // use the gpg implementation from the sbt-pgp plugin
    pgpSecretRing := baseDirectory.value / "secring.asc", // unpacked from secrets.tar.enc
    pgpPublicRing := baseDirectory.value / "pubring.asc", // unpacked from secrets.tar.enc
    commands += Command.command("commitRelease") { state =>
      "set com.softwaremill.PublishTravis.isCommitRelease := true" ::
        "release" ::
        state
    },
    commands += Command.command("publishRelease") { state =>
      "set com.softwaremill.PublishTravis.isCommitRelease := false" ::
        "release" ::
        state
    },
    // the steps from Publish.Release.steps are broken down into two segments
    releaseProcess := {
      if (isCommitRelease.value) {
        Seq(
          checkSnapshotDependencies,
          inquireVersions,
          runClean,
          runTest,
          setReleaseVersion,
          updateVersionInDocs(organization.value),
          commitReleaseVersion,
          tagRelease,
          setNextVersion,
          commitNextVersion,
          pushChanges
        )
      } else {
        Seq(
          publishArtifacts,
          releaseStepCommand("sonatypeBundleRelease")
        )
      }
    }
  )
}

object PublishTravis extends PublishTravis
