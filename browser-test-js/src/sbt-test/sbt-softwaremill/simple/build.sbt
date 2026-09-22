import com.softwaremill.SbtSoftwareMillBrowserTestJS.browserChromeTestSettings

scalaVersion := "3.3.8"

enablePlugins(ScalaJSPlugin)

browserChromeTestSettings

// sbt-scalajs adds -scalajs flag
TaskKey[Unit]("checkScalaJsFlag") := {
  assert((Test / scalacOptions).value.contains("-scalajs"), "-scalajs missing from Test / scalacOptions")
}
