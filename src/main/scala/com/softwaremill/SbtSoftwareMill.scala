package com.softwaremill

import sbt._, Keys._

import java.io.File
import java.nio.file.attribute.PosixFilePermission, PosixFilePermission.OWNER_EXECUTE
import java.nio.file.Files
import scala.collection.JavaConverters._

import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{headerCreate, headerLicense, HeaderLicense}
import sbtrelease.ReleasePlugin.autoImport.{
  releaseCrossBuild, releasePublishArtifactsAction}
import wartremover.{wartremoverWarnings, Wart, Warts}

// Inspired by sbt-catalysts

object SbtSoftwareMill extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends Base

  class Base extends Publish {
    val scalacOptions_2_10 = Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xfuture",
      "-Xlint",
      "-Yno-adapted-args",
      "-Yno-imports",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard")

    val scalacOptions_2_11 = Seq(
      "-Ydelambdafy:method",
      "-Yliteral-types",
      "-Ypartial-unification",
      "-Ywarn-unused-import")

    val scalacOptions_2_12 = Seq(
      "-Xstrict-patmat-analysis",
      "-Yinduction-heuristics",
      "-Ykind-polymorphism")

    lazy val commonBuildSettings = Seq(
      outputStrategy := Some(StdoutOutput),
      autoCompilerPlugins := true,
      autoAPIMappings := true,
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
        "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
        "bintray/non" at "http://dl.bintray.com/non/maven"),
      addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
      addCompilerPlugin("org.scalamacros" %  "paradise"       % "2.1.0" cross CrossVersion.patch),

      scalacOptions := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => scalacOptions_2_10 ++ scalacOptions_2_11 ++ scalacOptions_2_12
        case Some((2, 11)) => scalacOptions_2_10 ++ scalacOptions_2_11
        case _             => scalacOptions_2_10
      }),
      scalacOptions in (Test, console) --= Seq(
        "-Yno-imports",
        "-Ywarn-unused-import"),
      wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
        Wart.Any,                   // - see puffnfresh/wartremover#263
        Wart.ExplicitImplicitTypes, // - see puffnfresh/wartremover#226
        Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
        Wart.Nothing),              // - see puffnfresh/wartremover#263
      wartremoverWarnings in (Compile, compile) --=
        (CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 11)) | Some((2, 12)) => Nil
          case _                             => Seq(Wart.Overloading) // Falsely triggers on 2.10
        })
    )
  }

  lazy val transferPublishAndTagResources = {
    lazy val transferPublishAndTagResources = taskKey[Unit](
      "Transfers publishAndTag script and associated resources")

    transferPublishAndTagResources := {
      val log = streams.value.log

      val baseDir = (baseDirectory in ThisBuild).value

      def transfer(src: String, dst: File, permissions: Set[PosixFilePermission] = Set()) = {
        val srcʹ = getClass.getClassLoader.getResourceAsStream(src)

        log.info(s"transferring $src to $dst")

        IO.transfer(srcʹ, dst)

        Files.setPosixFilePermissions(
          dst.toPath,
          (Files.getPosixFilePermissions(dst.toPath).asScala ++ permissions).asJava)
      }

      transfer("publishAndTag",       baseDir / "scripts" / "publishAndTag", Set(OWNER_EXECUTE))
      transfer("credentials.sbt.enc", baseDir / "credentials.sbt.enc")
      transfer("pubring.pgp.enc",     baseDir / "pubring.pgp.enc")
      transfer("secring.pgp.enc",     baseDir / "secring.pgp.enc")
    }
  }
}
