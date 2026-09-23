val sbtCiReleaseVersion = "1.12.1"
val sbtScalafmtVersion = "2.6.2"
val sbtSaladDaysVersion = "0.2.0"
val sbtGitVersion = "2.2.0"

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.github.sbt" % "sbt-ci-release" % sbtCiReleaseVersion),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % sbtScalafmtVersion),
    addSbtPlugin("com.eed3si9n" % "sbt-salad-days" % sbtSaladDaysVersion),
    addSbtPlugin("com.github.sbt" % "sbt-git" % sbtGitVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtCiReleaseVersion" -> sbtCiReleaseVersion,
      "sbtScalafmtVersion" -> sbtScalafmtVersion,
      "sbtSaladDaysVersion" -> sbtSaladDaysVersion,
      "sbtGitVersion" -> sbtGitVersion
    ),
    buildInfoPackage := "sbtsoftwaremill"
  )
