import sbtsoftwaremill.BuildInfo
import com.softwaremill.Publish

lazy val root = Project("root", file("."))
  .settings(
    name         := "sbt-softwaremill",
    organization := "com.softwaremill.sbt-softwaremill",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true,
    sbtVersion in Global := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.16"
        case "2.12" => "1.1.0"
      }
    })
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"          % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"      % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"     % BuildInfo.sbtSonatypeVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"     % BuildInfo.sbtTravisCiVersion),
    addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.5.3"),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover"  % "2.2.1"),
    addSbtPlugin("com.geirsson"      % "sbt-scalafmt"     % "1.4.0"),
    addSbtPlugin("com.timushev.sbt"  % "sbt-updates"      % "0.3.4"),
    addSbtPlugin("io.spray"          % "sbt-revolver"     % "0.9.1")
  )
  .settings(publishSettings)

lazy val publishSettings = Publish.commonPublishSettings ++ Seq(
  organizationName := "SoftwareMill",
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  organizationHomepage := Some(url("https://softwaremill.com")),
  homepage := Some(url("https://github.com/softwaremill/sbt-softwaremill")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/softwaremill/sbt-softwaremill"),
      "scm:git@github.com:softwaremill/sbt-softwaremill.git")),
  pomExtra := (
    <scm>
      <url>git@github.com/softwaremill/sbt-softwaremill.git</url>
      <connection>scm:git:git@github.com/softwaremill/sbt-softwaremill.git</connection>
    </scm>
      <developers>
        <developer>
          <id>kciesielski</id>
          <name>Krzysztof Ciesielski</name>
        </developer>
      </developers>
    ))
