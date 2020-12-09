val sbtReleaseVersion = "1.0.13"
val sbtCiReleaseVersion = "1.5.5"
val sbtScalafmtVersion = "2.4.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion),
    addSbtPlugin("com.geirsson" % "sbt-ci-release" % sbtCiReleaseVersion),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % sbtScalafmtVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtReleaseVersion" -> sbtReleaseVersion,
      "sbtCiReleaseVersion" -> sbtCiReleaseVersion,
      "sbtScalafmtVersion" -> sbtScalafmtVersion
    ),
    buildInfoPackage := "sbtsoftwaremill"
  )
