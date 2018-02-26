# sbt-softwaremill
[![Build Status](https://travis-ci.org/softwaremill/sbt-softwaremill.svg?branch=master)](https://travis-ci.org/softwaremill/sbt-softwaremill)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill_2.12)  
A sane set of common build settings.

## Usage

First, add the plugin to your `project/plugins.sbt` file:

````scala
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill" % "1.1")
````

Now you can add `smlBuildSettings` to any set of build settings:

````scala
lazy val commonSettings = Seq(
  // your settings
) ++ smlBuildSettings
````

If you only want to import some settings, you can use any subset of `smlBuildSettings`:

````scala
    lazy val smlBuildSettings =
      commonSmlBuildSettings    ++ // compiler flags
      wartRemoverSettings       ++ // warts
      clippyBuildSettings       ++ // enable clippy colors
      acyclicSettings           ++ // check circular dependencies between packages
      splainSettings            ++ // gives rich output on implicit resolution errors 
      dependencyUpdatesSettings ++ // check dependency updates on startup (max once per 12h)
      ossPublishSettings           // configures common publishing process for all OSS libraries
````

`sbt-softwaremill` comes with:
- Coursier
- Scalafmt
- sbt-pgp
- sbt-release
- sbt-sonatype
- scala-clippy
- sbt-updates
- splain
- sbt-reloadquick
- sbt-revolver
- acyclic