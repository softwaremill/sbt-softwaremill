package com.softwaremill


import better.files._
import java.io.{File => JFile}
import sbt.State
import scala.util.Try

object CheckUpdates {

  var updatesTaskExecuted = false
  val tmpDir = new JFile(System.getProperty("java.io.tmpdir")).toScala

  def needsChecking(projectName: String): Boolean = {
    val file = tmpDir / (projectName + "_sml_last_update")
    val now = System.currentTimeMillis()
    println(s"Checking $file")
    val lastUpdate = Try(file.contentAsString.toLong).getOrElse(0L)
    if (now - lastUpdate > 12 * 3600 * 1000) {
      file.overwrite(now.toString)
      true
    }
    else false
  }

  lazy val startupTransition: String => State => State = { p: String => {
      s: State =>
        if (!updatesTaskExecuted && needsChecking(p)) {
          updatesTaskExecuted = true
          "dependencyUpdates" :: s
        }
        else
          s
    }
  }
}
