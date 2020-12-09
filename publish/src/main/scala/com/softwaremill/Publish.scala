package com.softwaremill

import sbt.Keys._
import sbt._

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
    commands += releaseCommand
  )

  lazy val noPublishSettings =
    Seq(publish := {}, publishLocal := {}, publishArtifact := false)

  private val releaseCommand = Command.command("release") { state =>
    val version = readNextVersion()
    val tag = "v" + version
    state.log.info(s"Tagging release as: $tag")
    val state2 = Command.process(s"git tag $tag", state)
    val files = UpdateVersionInDocs(
      state2,
      Project.extract(state).get(organization),
      version
    )
    val state3 = files.foldLeft(state2) { case (s, f) =>
      Command.process(s"git add ${f.getAbsolutePath}", s)
    }
    Command.process(s"git status", state3)
    state3
  }

  private def readNextVersion(): String =
    SimpleReader.readLine("Release version: ") match {
      case Some("")    => sys.error("Empty version provided!")
      case None        => sys.error("No version provided!")
      case Some(input) => input
    }
}

object Publish extends Publish
