package com.softwaremill

import sbt.{Def, File, Global, IO, Task, TaskKey, URL, taskKey}

object DownloadChromeDriver {
  val downloadChromeDriver: TaskKey[Unit] = taskKey[Unit](
    "Download chrome driver corresponding to installed google-chrome version"
  )
  val downloadChromeDriverSettings: Seq[Def.Setting[Task[Unit]]] = Seq(
    Global / downloadChromeDriver := {
      if (
        java.nio.file.Files.notExists(new File("target", "chromedriver").toPath)
      ) {
        println(
          "ChromeDriver binary file not found. Detecting google-chrome version..."
        )
        import sys.process._
        val osName = sys.props("os.name")
        val isMac = osName.toLowerCase.contains("mac")
        val isWin = osName.toLowerCase.contains("win")
        val chromeVersionExecutable =
          if (isMac)
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
          else "google-chrome"
        val chromeVersion =
          Seq(chromeVersionExecutable, "--version").!!.split(' ')(2)
        println(s"Detected google-chrome version: $chromeVersion")
        val withoutLastPart =
          chromeVersion.split('.').dropRight(1).mkString(".")
        println(s"Selected release: $withoutLastPart")
        val latestVersion =
          IO.readLinesURL(
            new URL(
              s"https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$withoutLastPart"
            )
          ).mkString
        val platformDependentName = if (isMac) {
          "chromedriver_mac64.zip"
        } else if (isWin) {
          "chromedriver_win32.zip"
        } else {
          "chromedriver_linux64.zip"
        }
        println(s"Downloading chrome driver version $latestVersion for $osName")
        IO.unzipURL(
          new URL(
            s"https://chromedriver.storage.googleapis.com/$latestVersion/$platformDependentName"
          ),
          new File("target")
        )
        IO.chmod("rwxrwxr-x", new File("target", "chromedriver"))
      } else {
        println("Detected chromedriver binary file, skipping downloading.")
      }
    }
  )
}
