package com.softwaremill

import sbt._

import java.util.regex.Pattern

object UpdateVersionInDocs {
  val DefaultFilesForVersionUpdate: List[File] =
    List(file("README.md"), file("doc"), file("docs"))

  // based on https://github.com/EECOLOR/sbt-release-custom-steps/blob/master/src/main/scala/org/qirx/sbtrelease/UpdateVersionInFiles.scala
  def apply(
      log: Logger,
      organization: String,
      version: String,
      filesToUpdate: List[File] = DefaultFilesForVersionUpdate
  ): Seq[File] = {
    var allFiles: Seq[File] = Vector()
    filesToUpdate match {
      case Nil =>
        log.info(
          "[UpdateVersionInDocs] Received empty set of files to update. Skipping updating version in docs"
        )

      case nonEmptyFilesToUpdate =>
        val regexStr =
          s""""$organization" %{1,2} "[\\w\\.-]+" % "([\\w\\.-]+)""""
        val currentVersionPattern = regexStr.r

        def findCurrentVersion(oldFile: String): Option[String] = {
          currentVersionPattern.findFirstMatchIn(oldFile).map(_.group(1))
        }

        def replaceInFile(f: File): Unit = {
          val oldFile = IO.read(f)
          findCurrentVersion(oldFile).foreach(currentVersion => {
            log.info(
              s"[UpdateVersionInDocs] Replacing $currentVersion with $version in ${f.name}"
            )
            val newFile = oldFile.replaceAll(
              Pattern.quote(currentVersion),
              version
            )
            IO.write(f, newFile)

            allFiles = allFiles :+ f
          })
        }

        def replaceDocsInDirectory(d: File) {
          Option(d.listFiles()).foreach(_.foreach { f =>
            if (f.isDirectory) {
              replaceDocsInDirectory(f)
            } else if (f.getName.endsWith(".rst") || f.getName.endsWith(".md")) {
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
            log.warn(
              s"[UpdateVersionInDocs] ${notExistingFile.getPath} does not exist, skipping..."
            )
        }
    }
    allFiles
  }
}
