# sbt-softwaremill
[![Build Status](https://travis-ci.org/softwaremill/sbt-softwaremill.svg?branch=master)](https://travis-ci.org/softwaremill/sbt-softwaremill)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill)  

A sane set of common build settings.

There are two plugins: one which should be added to projects, and another for global settings.

## Usage: project settings 

For each project where you'd like to use the build settings, add the following your `project/plugins.sbt` file:

````scala
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill" % "1.3.0")
````

Now you can add `smlBuildSettings` to any set of build settings in your `build.sbt`:

````scala
lazy val commonSettings = smlBuildSettings ++ Seq(
  // your settings, which can override some of smlBuildSettings
) 
````

If you only want to import a subset of settings, you can select from:

````scala
lazy val smlBuildSettings =
  commonSmlBuildSettings    ++ // compiler flags
  wartRemoverSettings       ++ // warts
  acyclicSettings           ++ // check circular dependencies between packages
  splainSettings            ++ // gives rich output on implicit resolution errors 
  dependencyUpdatesSettings ++ // check dependency updates on startup (max once per 12h)
  ossPublishSettings           // configures common publishing process for all OSS libraries
````

`sbt-softwaremill` comes with:
- [Coursier](https://github.com/coursier/coursier)
- [neo-sbt-scalafmt](https://github.com/lucidsoftware/neo-sbt-scalafmt)
- [sbt-pgp](https://github.com/sbt/sbt-pgp)
- [sbt-release](https://github.com/sbt/sbt-release)
- [sbt-sonatype](https://github.com/xerial/sbt-sonatype)
- [sbt-reloadquick](https://github.com/dwijnand/sbt-reloadquick)
- [sbt-revolver](https://github.com/spray/sbt-revolver)
- [acyclic](https://github.com/lihaoyi/acyclic)

## Usage: global settings

For the global settings, add to `~/.sbt/1.0/plugins/build.sbt` (or any other `.sbt` file):

````scala
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-global" % "1.3.0")
````

Now you can add `smlGlobalBuildSettings` to `~/.sbt/1.0/build.sbt`:

````scala
smlGlobalBuildSettings
````

If you only want to import a subset of settings, you can select from:

````scala
lazy val smlGlobalBuildSettings =
  clippyBuildSettings       ++ // enable clippy colors
  splainSettings            ++ // gives rich output on implicit resolution errors 
  dependencyUpdatesSettings    // check dependency updates on startup (max once per 12h)
````

`sbt-softwaremill-global` comes with:
- [scala-clippy](https://github.com/softwaremill/scala-clippy)
- [splain](https://github.com/tek/splain)
- [sbt-updates](https://github.com/rtimush/sbt-updates)

## Releasing your library

`sbt-softwaremill` exposes a default configuration suitable for releasing open source libraries.
Add `smlBuildSettings` or `ossPublishSettings` to your project's settings and you're all set, just run the `release` command.
Consider that:
- You need an [OSS Sonatype account](https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html) and sbt-pgp plugin properly configured with generated and published keys.
- Your `README.md` and `docs` directory will be parsed for `"[organization]" %(%) "artifactId" % "someVersion"` and that version value will be bumped.
- If you a have multi-module project, you may need to add `publishArtifact := false` to your root project's settings. 
