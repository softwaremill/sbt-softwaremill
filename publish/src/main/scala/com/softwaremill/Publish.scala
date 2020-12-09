package com.softwaremill

import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

trait Publish {
  lazy val ossPublishSettings = Seq(
    sonatypeProfileName := "com.softwaremill",
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
    val org = Project.extract(state).get(organization)

    state.log.info(s"Tagging release as: $tag")
    val state2 = Command.process(s"git tag $tag", state)

    val files = UpdateVersionInDocs(state2, org, version)
    val state3 = addFilesToGit(state2, files)

    state.log.info(s"\nDocs updated, git status:\n")
    val state4 = Command.process(s"git status", state3)
    state.log.info(s"\n")

    val state5 = Command.process(
      s"""git commit -m "Release $version"""",
      state4
    )

    val state6 = pushChanges(state5)
    state6
  }

  private def readNextVersion(): String =
    SimpleReader.readLine("Release version: ") match {
      case Some("")    => sys.error("Aborting, empty version provided!")
      case None        => sys.error("Aborting, no version provided!")
      case Some(input) => input
    }

  private def addFilesToGit(state: State, fs: Seq[File]): State =
    fs.foldLeft(state) { case (s, f) =>
      Command.process(s"git add ${f.getAbsolutePath}", s)
    }

  private def pushChanges(state: State): State =
    SimpleReader.readLine("Push changes? [y/n] ") match {
      case Some("y") =>
        val state2 = Command.process(s"git push", state)
        Command.process(s"git push --tags", state2)
      case _ => sys.error("Aborting, not pushing changes"); state
    }
}

object Publish extends Publish
