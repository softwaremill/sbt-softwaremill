package com.softwaremill

import sbt._, Keys._

import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import sbtrelease.ReleasePlugin.autoImport.{
  releaseCrossBuild, releasePublishArtifactsAction}

class Publish {
  lazy val checkHeaders = taskKey[Unit]("Fail the build if createHeaders is not up-to-date")

  lazy val commonPublishSettings = Seq(
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseCrossBuild := true,
    autoAPIMappings := true,
    developers := List(
      Developer(
        id = "softwaremill",
        name = "SoftwareMill",
        email = "info@softwaremill.com",
        url = new URL("https://softwaremill.com")
      )
    )
  )

  lazy val noPublishSettings = Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false)
}

object Publish extends Publish
