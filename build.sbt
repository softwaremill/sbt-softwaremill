import sbtsoftwaremill.BuildInfo
import com.softwaremill.Publish

lazy val root = Project("root", file("."))
  .settings(
    name         := "sbt-softwaremill",
    organization := "com.softwaremill",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true,
    sbtVersion in Global := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.16"
        case "2.12" => "1.1.0"
      }
    })
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"     % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % BuildInfo.sbtSonatypeVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % BuildInfo.sbtTravisCiVersion),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.2.1"))
  .settings(publishSettings)

lazy val publishSettings = Publish.commonPublishSettings ++ Seq(
  organizationName := "SoftwareMill",
  organizationHomepage := Some(url("https://softwaremill.com")),
  homepage := Some(url("https://github.com/softwaremill/sbt-softwaremill")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/softwaremill/sbt-softwaremill"),
      "scm:git@github.com:softwaremill/sbt-softwaremill.git")))
