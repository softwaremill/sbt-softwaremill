package com.softwaremill

import sbt.{Def, Global, Task, TaskKey, taskKey, _}
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{jsEnv, scalaJSLinkerConfig}

object SbtSoftwareMillBrowserTestJS {
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
    browserCommonTestSetting ++ Seq(
      Test / jsEnv := new jsenv.playwright.PWEnv(
        browserName = "chrome",
        headless = true,
        showLogs = true
      )
    )

  val browserGeckoTestSettings: Seq[Def.Setting[_]] =
    browserCommonTestSetting ++ Seq(
      Test / jsEnv := new jsenv.playwright.PWEnv(
        browserName = "firefox",
        headless = true,
        showLogs = true
      )
    )
}
