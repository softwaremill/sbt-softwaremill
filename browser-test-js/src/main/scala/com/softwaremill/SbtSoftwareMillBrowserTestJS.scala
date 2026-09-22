package com.softwaremill

import sbt.{Def, Global, Task, TaskKey, taskKey, _}
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{jsEnv, scalaJSLinkerConfig}

object SbtSoftwareMillBrowserTestJS {
  val browserCommonTestSetting: Seq[Def.Setting[?]] = Seq(
    // https://github.com/scalaz/scalaz/pull/1734#issuecomment-385627061
    scalaJSLinkerConfig ~= {
      _.withBatchMode(
        System.getenv("GITHUB_ACTIONS") == "true" || System.getenv(
          "CONTINUOUS_INTEGRATION"
        ) == "true"
      )
    }
  )

  private def playwrightEnv(browserName: String) =
    new jsenv.playwright.PWEnv(browserName = browserName, headless = true, showLogs = true)

  val browserChromeTestSettings: Seq[Def.Setting[?]] =
    browserCommonTestSetting ++ Seq(
      Test / jsEnv := Def.uncached(playwrightEnv("chrome"))
    )

  val browserGeckoTestSettings: Seq[Def.Setting[?]] =
    browserCommonTestSetting ++ Seq(
      Test / jsEnv := Def.uncached(playwrightEnv("firefox"))
    )
}
