val sbtPgpVersion      = "1.1.1"
val sbtReleaseVersion  = "1.0.8"
val sbtSonatypeVersion = "2.3"

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"  % sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype" % sbtSonatypeVersion),
    addSbtPlugin("com.geirsson"      % "sbt-scalafmt" % "1.5.1"),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtPgpVersion"      -> sbtPgpVersion,
      "sbtReleaseVersion"  -> sbtReleaseVersion,
      "sbtSonatypeVersion" -> sbtSonatypeVersion
    ),
    buildInfoPackage := "sbtsoftwaremill"
  )
