val sbtCiReleaseVersion = "1.11.2"
val sbtScalafmtVersion = "2.6.1"
val sbtSaladDaysVersion = "0.2.0"

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.github.sbt" % "sbt-ci-release" % sbtCiReleaseVersion),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % sbtScalafmtVersion),
    addSbtPlugin("com.eed3si9n" % "sbt-salad-days" % sbtSaladDaysVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtCiReleaseVersion" -> sbtCiReleaseVersion,
      "sbtScalafmtVersion" -> sbtScalafmtVersion,
      "sbtSaladDaysVersion" -> sbtSaladDaysVersion
    ),
    buildInfoPackage := "sbtsoftwaremill"
  )
