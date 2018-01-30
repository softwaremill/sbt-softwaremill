val sbtPgpVersion      = "1.1.0"
val sbtReleaseVersion  = "1.0.6"
val sbtSonatypeVersion = "2.0"
val sbtTravisCiVersion = "1.1.1"
val sbtClippyVersion   = "0.5.3"

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"          % sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"      % sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"     % sbtSonatypeVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"     % sbtTravisCiVersion),
    addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % sbtClippyVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtPgpVersion"      -> sbtPgpVersion,
      "sbtReleaseVersion"  -> sbtReleaseVersion,
      "sbtSonatypeVersion" -> sbtSonatypeVersion,
      "sbtTravisCiVersion" -> sbtTravisCiVersion,
      "sbtClippyVersion"   -> sbtClippyVersion),
    buildInfoPackage := "sbtsoftwaremill")
