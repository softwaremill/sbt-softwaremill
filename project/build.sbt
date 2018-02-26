val sbtPgpVersion      = "1.1.0"
val sbtReleaseVersion  = "1.0.7"
val sbtSonatypeVersion = "2.3"

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"          % sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"      % sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"     % sbtSonatypeVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtPgpVersion"      -> sbtPgpVersion,
      "sbtReleaseVersion"  -> sbtReleaseVersion,
      "sbtSonatypeVersion" -> sbtSonatypeVersion),
    buildInfoPackage := "sbtsoftwaremill")
