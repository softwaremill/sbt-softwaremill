package com.softwaremill

import sbt._
import Keys._
import wartremover.{wartremoverWarnings, Wart, Warts}
import com.softwaremill.clippy.ClippySbtPlugin.autoImport.clippyFatalWarnings

object SbtSoftwareMill extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements
  object autoImport extends Base

  // @formatter:off
  class Base extends Publish {
    val commonScalacOptions = Seq(
      "-deprecation",                   // Emit warning and location for usages of deprecated APIs.
      "-encoding", "UTF-8",             // Specify character encoding used by source files.
      "-explaintypes"  ,                // Explain type errors in more detail.
      "-feature",                       // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",         // Existential types (besides wildcard types) can be written and inferred
      "-language:higherKinds",          // Allow higher-kinded types
      "-language:experimental.macros",  // Allow macro definition (besides implementation and application)
      "-language:implicitConversions",  // Allow definition of implicit functions called views
      "-language:postfixOps",           // Allow postfix operators
      "-unchecked",                     // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                    // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfuture",                       // Turn on future language features.
      "-Yno-adapted-args",              // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Ywarn-dead-code",               // Warn when dead code is identified.
      "-Ywarn-inaccessible",            // Warn about inaccessible types in method signatures.
      "-Ywarn-nullary-override",        // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit",            // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen",           // Warn when numerics are widened.
      "-Ywarn-value-discard")           // Warn when non-Unit expression results are unused.

    val scalacOptionsGte211 = Seq(
      "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
      "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
      "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
      "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",            // Option.apply used implicit view.
      "-Xlint:package-object-classes",     // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
      "-Xlint:unsound-match",              // Pattern match may not be typesafe.
      "-Ywarn-infer-any")                  // Warn when a type argument is inferred to be `Any`.

    val scalacOptionsGte212 = Seq(
      "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
      "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",          // Warn if a local definition is unused.
      "-Ywarn-unused:params",          // Warn if a value parameter is unused.
      "-Ypartial-unification",         // Improves type constructor inference with support for partial unification (SI-2712)
      "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",        // Warn if a private member is unused.
      "-Ywarn-extra-implicit")         // Warn when more than one implicit parameter section is defined.


    val scalacOptionsEq211 = List(
      "-Ywarn-unused-import"          // Warn if an import selector is not referenced.
    )

    val scalacOptionsEq210 = List(
      "-Xlint"
    )
    // @formatter:off

    def scalacOptionsFor(version: String): Seq[String] =
      commonScalacOptions ++ (CrossVersion.partialVersion(version) match {
        case Some((2, min)) if min >= 12 => scalacOptionsGte212 ++ scalacOptionsGte211
        case Some((2, min)) if min >= 11 => scalacOptionsGte211 ++ scalacOptionsEq211
        case _                           => scalacOptionsEq210
      })

    val filterConsoleScalacOptions = { options: Seq[String] =>
      options.filterNot(
        Set(
          "-Ywarn-unused:imports",
          "-Ywarn-unused-import",
          "-Ywarn-dead-code",
          "-Xfatal-warnings"
        )
      )
    }

    import autoImport._

    val acyclicVersion    = "0.1.8"
    lazy val acyclicSettings = Seq(
      libraryDependencies += "com.lihaoyi" %% "acyclic" % acyclicVersion % "provided",
      autoCompilerPlugins := true,
      scalacOptions += "-P:acyclic:force",
      addCompilerPlugin("com.lihaoyi" %% "acyclic" % acyclicVersion)
    )

    // @formatter:off
    lazy val wartRemoverSettings = Seq(
      wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
        Wart.NonUnitStatements,
        Wart.Overloading,
        Wart.PublicInference,
        Wart.Equals,
        Wart.ImplicitParameter,
        Wart.Serializable,
        Wart.Product,
        Wart.Any,                   // - see puffnfresh/wartremover#263
        Wart.ExplicitImplicitTypes, // - see puffnfresh/wartremover#226
        Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
        Wart.Nothing),              // - see puffnfresh/wartremover#263
      wartremoverWarnings in (Test, compile) ++= Warts.allBut(
        Wart.NonUnitStatements,
        Wart.DefaultArguments,
        Wart.Overloading,
        Wart.Serializable,
        Wart.Product,
        Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
        Wart.Nothing),              // - see puffnfresh/wartremover#263
      wartremoverWarnings in (Compile, compile) --=
        (CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 11)) | Some((2, 12)) => Nil
          case _                             => Seq(Wart.Overloading) // Falsely triggers on 2.10
        })
    )
    // @formatter:off

    lazy val commonSmlBuildSettings = Seq(
      outputStrategy := Some(StdoutOutput),
      autoCompilerPlugins := true,
      autoAPIMappings := true,
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
        "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
        "bintray/non" at "http://dl.bintray.com/non/maven"
      ),
      addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.9"),
      addCompilerPlugin("org.scalamacros" % "paradise"        % "2.1.1" cross CrossVersion.patch),
      scalacOptions ++= scalacOptionsFor(scalaVersion.value),
      scalacOptions.in(Compile, console) ~= filterConsoleScalacOptions,
      scalacOptions.in(Test, console) ~= filterConsoleScalacOptions,
      // silence transitive eviction warnings
      evictionWarningOptions in update := EvictionWarningOptions.default
        .withWarnTransitiveEvictions(false)
    )

    lazy val clippyBuildSettings = Seq(
      com.softwaremill.clippy.ClippySbtPlugin.clippyColorsEnabled := true,
      clippyFatalWarnings ++= List(
        "match may not be exhaustive[\\s\\S]*"
      )
    )

    lazy val splainSettings = Seq(
      libraryDependencies += {
        val splainVersion = CrossVersion.partialVersion(scalaVersion.value) match {
          // splain has dropped support of Scala 2.10 with version 0.3.1
          // last version supporting Scala 2.10 is splain 0.2.10
          case Some((2, 11)) | Some((2, 12)) =>
            "0.4.0"
          case _ =>
            "0.2.10"
        }
        compilerPlugin("io.tryp" % "splain" % splainVersion cross CrossVersion.patch)
      },
      scalacOptions += "-P:splain:all"
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

    lazy val smlBuildSettings =
      commonSmlBuildSettings ++
        wartRemoverSettings ++
        acyclicSettings ++
        ossPublishSettings ++
        clippyBuildSettings ++
        splainSettings ++
        dependencyUpdatesSettings
  }
}
