import sbtsoftwaremill.BuildInfo
import com.softwaremill.Publish

val commonSettings = Publish.ossPublishSettings ++ Seq(
  organization := "com.softwaremill.sbt-softwaremill",
  sbtVersion in Global := {
    scalaBinaryVersion.value match {
      case "2.10" => "0.13.17"
      case "2.12" => "1.1.6"
    }
  }
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(
    name            := "sbt-softwaremill",
    publishArtifact := false)
  .aggregate(perproject, global)

lazy val perproject = project.in(file("perproject"))
  .settings(commonSettings)
  .settings(
    name         := "sbt-softwaremill",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true
  )
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"          % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"      % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"     % BuildInfo.sbtSonatypeVersion),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover"  % "2.3.2"),
    addSbtPlugin("com.lucidchart"    % "sbt-scalafmt-coursier" % "1.15"),
    addSbtPlugin("io.spray"          % "sbt-revolver"     % "0.9.1"),
    addSbtPlugin("io.get-coursier"   % "sbt-coursier"     % "1.0.3"),
    addSbtPlugin("com.dwijnand"      % "sbt-reloadquick"  % "1.0.0")
  )

lazy val global = project.in(file("global"))
  .settings(commonSettings)
  .settings(
    name         := "sbt-softwaremill-global",
    description  := "Common build configuration for SBT projects - global",
    sbtPlugin    := true,
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.6.0",
  )
  .settings(
    addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.5.3"),
    addSbtPlugin("com.timushev.sbt"  % "sbt-updates"      % "0.3.4")
  )
