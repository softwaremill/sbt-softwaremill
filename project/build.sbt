val sbtCiReleaseVersion = "1.5.10"
val sbtScalafmtVersion = "2.4.6"

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.github.sbt" % "sbt-ci-release" % sbtCiReleaseVersion),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % sbtScalafmtVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtCiReleaseVersion" -> sbtCiReleaseVersion,
      "sbtScalafmtVersion" -> sbtScalafmtVersion
    ),
    buildInfoPackage := "sbtsoftwaremill"
  )
