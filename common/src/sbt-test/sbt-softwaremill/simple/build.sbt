import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

scalaVersion := "3.3.8"
crossScalaVersions := Seq("2.12.20", "2.13.18", "3.3.8", "3.9.0")

scalacOptions += "-Xfatal-warnings"

commonSmlBuildSettings
