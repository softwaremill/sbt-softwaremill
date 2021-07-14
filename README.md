# sbt-softwaremill

[![Build Status](https://travis-ci.org/softwaremill/sbt-softwaremill.svg?branch=master)](https://travis-ci.org/softwaremill/sbt-softwaremill)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill-common/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill-common)  

A sane set of common build settings.

## Usage

For each project where you'd like to use the build settings, add some or all of the following your `project/plugins.sbt`
file:

````scala
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common" % "2.0.6")
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-publish" % "2.0.6")
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-extra" % "2.0.6")
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-browser-test-js" % "2.0.6")
````

Now you can add the appropriate settings in your `build.sbt`, e.g.:

````scala
lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  // your settings, which can override some of commonSmlBuildSettings
) 
````

Each dependency provides a choice of settings:

````scala
// common - compiler flags
commonSmlBuildSettings

// publish
ossPublishSettings

// extra - use all or choose
lazy val extraSmlBuildSettings =
  dependencyUpdatesSettings ++  // check dependency updates on startup (max once per 12h)
  dependencyCheckSettings

// downloads the appropriate chrome/gecko driver for testing scala.js using scalajs-env-selenium and sets the jsenv
DownloadChromeDriver.browserChromeTestSettings
DownloadChromeDriver.browserGeckoTestSettings 
````

`sbt-softwaremill-common` comes with:
- [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html)
- [sbt-tpolecat](https://github.com/DavidGregory084/sbt-tpolecat)

`sbt-softwaremill-publish` comes with:
- [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release)

`sbt-softwaremill-extra` comes with:
- [sbt-updates](https://github.com/rtimush/sbt-updates)
- [sbt-dependency-check](https://github.com/albuch/sbt-dependency-check)

## Releasing your library

`sbt-softwaremill-publish` exposes a default configuration suitable for releasing open source libraries.
The release process is broken into two steps:

1. *local*: `sbt release`. This sbt command prepares the next release: asks about the version, updates the version
   in the docs & readme, commits the changes and finally asks the user to push the changes. Your `README.md`, 
   `docs` and `doc` directory will be parsed for `"[organization]" %(%) "artifactId" % "someVersion"` and that 
   version value will be bumped.
2. *remote*: `sbt ci-release`. This sbt command should be run on GH actions, triggered when a new tag is pushed. It
   publishes the artifacts to sonatype, and invokes repository release.
   
To setup the remote part, follow the guide on [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release). You can
also take a look at this project's `.github/workflows/ci.yml`.

You might need to explicitly set the sonatype profile name:

```scala
val commonSettings = ossPublishSettings ++ Seq(
  sonatypeProfileName := "com.example"
)
```

## Releasing sbt-softwaremill

sbt-softwaremill release process is setup on GH Actions. This plugin uses itself to publish binaries to oss-sonatype.

## Note for migrating from sbt-softwaremill 1.x series

You should remove `version.sbt` file as it's no longer used, and it may disrupt the release process. In the 2.x series the version is deduced from git tags and the current state using [https://github.com/dwijnand/sbt-dynver](https://github.com/dwijnand/sbt-dynver).

Moreover, a number of bundled plugins are removed, which aren't available for Scala3 and would cause build problems
