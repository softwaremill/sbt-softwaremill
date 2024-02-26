package com.softwaremill

import sbt.{Def, Global, Task, TaskKey, taskKey, _}
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{jsEnv, scalaJSLinkerConfig}

object SbtSoftwareMillBrowserTestJS {
  val downloadChromeDriver: TaskKey[Unit] = taskKey[Unit](
    "Download chrome driver corresponding to installed google-chrome version"
  )

  lazy val downloadGeckoDriver: TaskKey[Unit] = taskKey[Unit](
    "Download gecko driver"
  )

  lazy val geckoDriverVersion: SettingKey[String] =
    settingKey[String]("Gecko driver version to download")

  val downloadChromeDriverSettings: Seq[Def.Setting[Task[Unit]]] = Seq(
    Global / downloadChromeDriver := {
      if (java.nio.file.Files.notExists(new File("target", "chromedriver").toPath)) {
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
        val chromeVersion = Seq(chromeVersionExecutable, "--version").!!.split(' ')(2)
        println(s"Detected google-chrome version: $chromeVersion")
        val withoutLastPart = chromeVersion.split('.').dropRight(1).mkString(".")
        println(s"Selected release: $withoutLastPart")
        val latestVersion = IO.readLinesURL(new URL(s"https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_$withoutLastPart")).mkString
        val platformSuffix = if (isMac) {
          if (System.getProperty("os.arch") == "x86_64") "mac-x64" else "mac-arm64"
        } else if (isWin) { "win32" } else { "linux64" }
        println(s"Downloading chrome driver version $latestVersion for $osName")
        IO.unzipURL(
          new URL(s"https://storage.googleapis.com/chrome-for-testing-public/$latestVersion/$platformSuffix/chromedriver-$platformSuffix.zip"),
          new File("target")
        )
        IO.move(
          new File(new File("target", s"chromedriver-$platformSuffix"), "chromedriver"),
          new File("target", "chromedriver"))
        IO.chmod("rwxrwxr-x", new File("target", "chromedriver"))
      } else {
        println("Detected chromedriver binary file, skipping downloading.")
      }
    }
  )

  val downloadGeckoDriverSettings: Seq[Def.Setting[_]] = Seq(
    Global / geckoDriverVersion := "v0.28.0",
    Global / downloadGeckoDriver := {
      if (java.nio.file.Files.notExists(new File("target", "geckodriver").toPath)) {
        val version = (geckoDriverVersion in Global).value
        println(s"geckodriver binary file not found")
        import sys.process._
        val osName = sys.props("os.name")
        val isMac = osName.toLowerCase.contains("mac")
        val isWin = osName.toLowerCase.contains("win")
        val platformDependentName = if (isMac) {
          "macos.tar.gz"
        } else if (isWin) {
          "win64.zip"
        } else {
          "linux64.tar.gz"
        }
        println(s"Downloading gecko driver version $version for $osName")
        val geckoDriverUrl =
          s"https://github.com/mozilla/geckodriver/releases/download/$version/geckodriver-$version-$platformDependentName"
        if (!isWin) {
          url(geckoDriverUrl) #> file("target/geckodriver.tar.gz") #&&
            "tar -xz -C target -f target/geckodriver.tar.gz" #&&
            "rm target/geckodriver.tar.gz" !
        } else {
          IO.unzipURL(new URL(geckoDriverUrl), new File("target"))
        }
        IO.chmod("rwxrwxr-x", new File("target", "geckodriver"))
      } else {
        println("Detected geckodriver binary file, skipping downloading.")
      }
    }
  )

  val browserCommonTestSetting: Seq[Def.Setting[_]] = Seq(
    // https://github.com/scalaz/scalaz/pull/1734#issuecomment-385627061
    scalaJSLinkerConfig ~= {
      _.withBatchMode(
        System.getenv("GITHUB_ACTIONS") == "true" || System.getenv(
          "CONTINUOUS_INTEGRATION"
        ) == "true"
      )
    }
  )

  val browserChromeTestSettings: Seq[Def.Setting[_]] =
    downloadChromeDriverSettings ++ browserCommonTestSetting ++ Seq(
      jsEnv in Test := {
        val debugging = false // set to true to help debugging
        System.setProperty("webdriver.chrome.driver", "target/chromedriver")
        new org.scalajs.jsenv.selenium.SeleniumJSEnv(
          {
            val options = new org.openqa.selenium.chrome.ChromeOptions()
            val args = Seq(
              "auto-open-devtools-for-tabs", // devtools needs to be open to capture network requests
              "no-sandbox",
              "allow-file-access-from-files" // change the origin header from 'null' to 'file'
            ) ++ (if (debugging) Seq.empty else Seq("headless"))
            options.addArguments(args: _*)
            val capabilities =
              org.openqa.selenium.remote.DesiredCapabilities.chrome()
            capabilities.setCapability(
              org.openqa.selenium.chrome.ChromeOptions.CAPABILITY,
              options
            )
            capabilities
          },
          org.scalajs.jsenv.selenium.SeleniumJSEnv
            .Config()
            .withKeepAlive(debugging)
        )
      },
      test in Test := (test in Test)
        .dependsOn(downloadChromeDriver)
        .value
    )

  val browserGeckoTestSettings: Seq[Def.Setting[_]] =
    downloadGeckoDriverSettings ++ browserCommonTestSetting ++ Seq(
      jsEnv in Test := {
        val debugging = false // set to true to help debugging
        System.setProperty("webdriver.gecko.driver", "target/geckodriver")
        new org.scalajs.jsenv.selenium.SeleniumJSEnv(
          {
            val options = new org.openqa.selenium.firefox.FirefoxOptions()
            val args = (if (debugging) Seq("--devtools") else Seq("-headless"))
            options.addArguments(args: _*)
            options
          },
          org.scalajs.jsenv.selenium.SeleniumJSEnv
            .Config()
            .withKeepAlive(debugging)
        )
      },
      test in Test := (test in Test)
        .dependsOn(downloadGeckoDriver)
        .value
    )
}
