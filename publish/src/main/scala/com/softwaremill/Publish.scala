package com.softwaremill

import java.util.regex.Pattern

import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys._
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, _}
import sbtrelease.ReleaseStateTransformations._
import xerial.sbt.Sonatype.SonatypeKeys._
import xerial.sbt.Sonatype._

trait Publish extends PublishCommon {

  object Release {
    def steps(beforeCommitSteps: Seq[ReleaseStep]): Seq[ReleaseStep] =
      Seq(
        checkSnapshotDependencies,
        inquireVersions,
        // publishing locally so that the pgp password prompt is displayed early
        // in the process
        ReleaseStep(releaseStepCommand("publishLocalSigned")),
        runClean,
        runTest,
        setReleaseVersion
      ) ++ beforeCommitSteps ++
        Seq(
          commitReleaseVersion,
          tagRelease,
          publishArtifacts,
          ReleaseStep(releaseStepCommand("sonatypeBundleRelease")),
          setNextVersion,
          ReleaseStep(commitNextVersion),
          pushChanges
        )

    def stageChanges(fileNames: String*): ReleaseStep = { s: State =>
      val settings = Project.extract(s)
      val vcs = settings.get(releaseVcs).get
      fileNames.foreach(f => vcs.add(f) !! s.log)
      s
    }

    val DefaultFilesForVersionUpdate: Set[File] =
      Set(file("README.md"), file("doc"), file("docs"))

    // based on https://github.com/EECOLOR/sbt-release-custom-steps/blob/master/src/main/scala/org/qirx/sbtrelease/UpdateVersionInFiles.scala
    def updateVersionInDocs(
                             organization: String,
                             filesToUpdate: Set[File] = DefaultFilesForVersionUpdate
                           ): ReleaseStep = { s: State =>
      filesToUpdate match {
        case Set.empty =>
          s.log.info("Received empty set of files to update. Skipping updating version in docs") //TODO see if it's logged with task name

        case nonEmptyFilesToUpdate =>
          val regexStr = s""""$organization" %{1,2} "[\\w\\.-]+" % "([\\w\\.-]+)""""
          val currentVersionPattern = regexStr.r
          val releaseVersion = s.get(versions).get._1
          val settings = Project.extract(s)
          val vcs = settings.get(releaseVcs).get

          def findCurrentVersion(oldFile: String): Option[String] = {
            currentVersionPattern.findFirstMatchIn(oldFile).map(_.group(1))
          }

          def replaceInFile(f: File): Unit = {
            val oldFile = IO.read(f)
            findCurrentVersion(oldFile).map(currentVersion => {
              s.log.info(s"Replacing $currentVersion with $releaseVersion in ${f.name}")
              val newFile = oldFile.replaceAll(Pattern.quote(currentVersion), releaseVersion)
              IO.write(f, newFile)

              vcs.add(f.getAbsolutePath) !! s.log
            })
          }

          def replaceDocsInDirectory(d: File) {
            Option(d.listFiles()).foreach(_.foreach { f =>
              if (f.isDirectory) {
                replaceDocsInDirectory(f)
              } else if (
                f.getName.endsWith(".rst") || f.getName
                  .endsWith(".md")
              ) {
                replaceInFile(f)
              }
            })
          }

          nonEmptyFilesToUpdate.foreach {
            case file: File if !file.isDirectory => replaceInFile(file)
            case directory: File => replaceDocsInDirectory(directory)
          }
      }

      s
    }
  }

  lazy val ossPublishSettings = Seq(
    publishTo := {
      if (isSnapshot.value) Some(Opts.resolver.sonatypeSnapshots)
      else sonatypePublishToBundle.value
    },
    publishArtifact in Test := false,
    publishMavenStyle := true,
    sonatypeProfileName := "com.softwaremill",
    pomIncludeRepository := { _ =>
      false
    },
    organizationHomepage := Some(url("https://softwaremill.com")),
    homepage := Some(url("http://softwaremill.com/open-source")),
    licenses := Seq(
      "Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    sonatypeProjectHosting := Some(
      GitHubHosting("softwaremill", name.value, "info@softwaremill.com")
    ),
    autoAPIMappings := true,
    developers := List(
      Developer(
        id = "softwaremill",
        name = "SoftwareMill",
        email = "info@softwaremill.com",
        url = new URL("https://softwaremill.com")
      )
    ),
    // sbt-release
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseIgnoreUntrackedFiles := true,
    beforeCommitSteps := {
      Seq(Release.updateVersionInDocs(organization.value))
    },
    releaseProcess := Release.steps(beforeCommitSteps.value)
  )

  lazy val noPublishSettings =
    Seq(publish := {}, publishLocal := {}, publishArtifact := false)
}

object Publish extends Publish
