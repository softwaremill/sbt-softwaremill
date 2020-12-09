package com.softwaremill

import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations.{
  checkSnapshotDependencies,
  inquireVersions,
  pushChanges,
  tagRelease
}

trait Publish {
  lazy val ossPublishSettings = Seq(
    organizationHomepage := Some(url("https://softwaremill.com")),
    homepage := Some(url("http://softwaremill.com/open-source")),
    licenses := Seq(
      "Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        id = "softwaremill",
        name = "SoftwareMill",
        email = "info@softwaremill.com",
        url = new URL("https://softwaremill.com")
      )
    ),
    // sbt-release
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      tagRelease,
      UpdateVersionInDocs(organization.value),
      pushChanges
    )
  )

  lazy val noPublishSettings =
    Seq(publish := {}, publishLocal := {}, publishArtifact := false)
}

object Publish extends Publish
