package com.softwaremill

import sbt.Keys._
import sbt._
import sbtdynver.DynVerPlugin.autoImport.dynverTagPrefix
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
    updateDocs := UpdateVersionInDocs(sLog.value, organization.value, version.value),
    commands += releaseCommand
  )

  lazy val noPublishSettings =
    Seq(publish := {}, publishLocal := {}, publishArtifact := false)

  val updateDocs = taskKey[Seq[File]]("Update docs during a release")

  private val releaseCommand = Command.command("release") { state =>
    var s = state
    s.log.info("Current version:")
    s = Command.process("version", s)
    val version = readNextVersion()

    val tagPrefix = Project.extract(s).getOpt(ThisBuild / dynverTagPrefix).getOrElse("v")
    val tag = tagPrefix + version

    s = Command.process(s"""set ThisBuild/version := "$version"""", s)

    val (s2, files) = Project.extract(s).runTask(updateDocs, s)
    s = s2
    s = addFilesToGit(s, files)

    s.log.info(s"\nDocs updated, git status:\n")
    s = Command.process(s"git status", s)
    s.log.info(s"\n")

    s = Command.process(s"""git commit -m "Release $version"""", s)

    s.log.info(s"Tagging release as: $tag")
    s = Command.process(s"git tag $tag", s)

    s = pushChanges(s)
    s
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
