package com.softwaremill


import better.files._
import java.io.{File => JFile}
import sbt.State
import scala.util.Try

object CheckUpdates {

  var updatesTaskExecuted = false
  val filename = "sml_sbt_last_update_check"
  val tmpDir = new JFile(System.getProperty("java.io.tmpdir")).toScala

  lazy val needsChecking: Boolean = {
    val file = tmpDir / filename
    val now = System.currentTimeMillis()
    val lastUpdate = Try(file.contentAsString.toLong).getOrElse(0L)
    if (now - lastUpdate > 12 * 3600 * 1000) {
      file.overwrite(now.toString)
      true
    }
    else false
  }

  lazy val startupTransition: State => State = { s: State =>
    if (!updatesTaskExecuted && needsChecking) {
      updatesTaskExecuted = true
      "dependencyUpdates" :: s
    }
    else
      s
  }
}
