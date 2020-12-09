package com.softwaremill

import sbt._
import Keys._

object SbtSoftwareMillCommon extends AutoPlugin {
  lazy val isDotty = settingKey[Boolean]("Is the scala version dotty.")

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements
  object autoImport {
    lazy val commonSmlBuildSettings = Seq(
      isDotty := scalaVersion.value.startsWith("0.") || scalaVersion.value.startsWith("3."),
      outputStrategy := Some(StdoutOutput),
      autoCompilerPlugins := true,
      autoAPIMappings := true,
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
        "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
        "bintray/non" at "https://dl.bintray.com/non/maven"
      ),
      libraryDependencies ++= {
        if (isDotty.value) Nil else Seq(compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"))
      },
      // silence transitive eviction warnings
      evictionWarningOptions in update := EvictionWarningOptions.default
        .withWarnTransitiveEvictions(false),
      // use sbt-tpolecat, but without fatal warnings
      scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings")))
    )
  }
}
