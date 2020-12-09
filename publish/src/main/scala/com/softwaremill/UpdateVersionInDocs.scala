package com.softwaremill

import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys.versions
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseVcs}

import java.util.regex.Pattern

object UpdateVersionInDocs {
  val DefaultFilesForVersionUpdate: List[File] =
    List(file("README.md"), file("doc"), file("docs"))

  // based on https://github.com/EECOLOR/sbt-release-custom-steps/blob/master/src/main/scala/org/qirx/sbtrelease/UpdateVersionInFiles.scala
  def apply(
      organization: String,
      filesToUpdate: List[File] = DefaultFilesForVersionUpdate
  ): ReleaseStep = { s: State =>
    filesToUpdate match {
      case Nil =>
        s.log.info(
          "[UpdateVersionInDocs] Received empty set of files to update. Skipping updating version in docs"
        )

      case nonEmptyFilesToUpdate =>
        val regexStr =
          s""""$organization" %{1,2} "[\\w\\.-]+" % "([\\w\\.-]+)""""
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
            s.log.info(
              s"[UpdateVersionInDocs] Replacing $currentVersion with $releaseVersion in ${f.name}"
            )
            val newFile = oldFile.replaceAll(
              Pattern.quote(currentVersion),
              releaseVersion
            )
            IO.write(f, newFile)

            vcs.add(f.getAbsolutePath) !! s.log
          })
        }

        def replaceDocsInDirectory(d: File) {
          Option(d.listFiles()).foreach(_.foreach { f =>
            if (f.isDirectory) {
              replaceDocsInDirectory(f)
            } else if (
              f.getName.endsWith(".rst") || f.getName.endsWith(".md")
            ) {
              replaceInFile(f)
            }
          })
        }

        nonEmptyFilesToUpdate.foreach {
          case file: File if !file.isDirectory && file.exists() =>
            replaceInFile(file)
          case directory: File if directory.exists() =>
            replaceDocsInDirectory(directory)
          case notExistingFile =>
            s.log.warn(
              s"[UpdateVersionInDocs] ${notExistingFile.getPath} does not exist, skipping..."
            )
        }
    }

    s
  }
}
