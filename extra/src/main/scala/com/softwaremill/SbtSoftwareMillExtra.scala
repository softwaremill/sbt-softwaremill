package com.softwaremill

import sbt._
import Keys._
import wartremover.{Wart, Warts}
import wartremover.WartRemover.autoImport._
import net.vonbuchholtz.sbt.dependencycheck.DependencyCheckPlugin

object SbtSoftwareMillExtra extends AutoPlugin {
  lazy val isDotty = settingKey[Boolean]("Is the scala version dotty.")

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements
  object autoImport {
    // @formatter:off
    val acyclicVersion = Def.setting(
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v <= 11 =>
          // dropped Scala 2.11 support since 0.2.0
          "0.1.9"
        case _ =>
          "0.2.0"
      }
    )
    lazy val acyclicSettings = Seq(
      libraryDependencies += "com.lihaoyi" %% "acyclic" % acyclicVersion.value % "provided",
      autoCompilerPlugins := true,
      scalacOptions += "-P:acyclic:force",
      (scalacOptions in Test) += "-P:acyclic:force",
      (scalacOptions in IntegrationTest) += "-P:acyclic:force",
      libraryDependencies += compilerPlugin("com.lihaoyi" %% "acyclic" % acyclicVersion.value)
    )

    val smlWartremoverCompileExclusions = Seq(
      Wart.NonUnitStatements,
      Wart.Overloading,
      Wart.PublicInference,
      Wart.Equals,
      Wart.ImplicitParameter,
      Wart.Serializable,
      Wart.DefaultArguments,
      Wart.Var,
      Wart.Product,
      Wart.Any,                   // - see puffnfresh/wartremover#263
      Wart.ExplicitImplicitTypes, // - see puffnfresh/wartremover#226
      Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
      Wart.Nothing,               // - see puffnfresh/wartremover#263
      Wart.FinalCaseClass
    )

    val smlWartremoverTestCompileExclusions = smlWartremoverCompileExclusions ++ Seq(
      Wart.DefaultArguments,
      Wart.Var,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf,
      Wart.TraversableOps,
      Wart.Option2Iterable,
      Wart.JavaSerializable,
      Wart.FinalCaseClass
    )

    lazy val wartRemoverSettings = Seq(
      wartremoverWarnings in (Compile, compile) := Warts.all.diff(smlWartremoverCompileExclusions),
      wartremoverWarnings in (Test, compile) := Warts.all.diff(smlWartremoverTestCompileExclusions)
    )

    lazy val splainSettings = Seq(
      libraryDependencies ++= {
        val splainVersion = CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, v)) if v >= 11 => Some("0.5.6")
          case Some((2, v)) if v == 11 => Some("0.2.10")
          case _ => None // dotty
        }
        splainVersion.map(v => compilerPlugin("io.tryp" % "splain" % v cross CrossVersion.patch)).toList
      },
      scalacOptions := scalacOptions.value ++ (if (isDotty.value) Nil else List("-P:splain:all"))
    )

    lazy val dependencyUpdatesSettings = Seq(
      // onLoad is scoped to Global because there's only one.
      onLoad in Global := {
        val old = (onLoad in Global).value
        // compose the new transition on top of the existing one
        // in case your plugins are using this hook.
        CheckUpdates.startupTransition(organization.value + "_" + name.value) compose old
      }
    )

    lazy val dependencyCheckSettings = Seq(
      DependencyCheckPlugin.autoImport.dependencyCheckCveUrlModified := Some(new URL("http://nvdmirror.sml.io/")),
      DependencyCheckPlugin.autoImport.dependencyCheckCveUrlBase := Some("http://nvdmirror.sml.io/"),
      DependencyCheckPlugin.autoImport.dependencyCheckAssemblyAnalyzerEnabled := Some(false),
      DependencyCheckPlugin.autoImport.dependencyCheckFormat := "All"
    )

    lazy val extraSmlBuildSettings =
        wartRemoverSettings ++
        acyclicSettings ++
        splainSettings ++
        dependencyUpdatesSettings ++
        dependencyCheckSettings
  }
}
