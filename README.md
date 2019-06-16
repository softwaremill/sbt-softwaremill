# sbt-softwaremill
[![Build Status](https://travis-ci.org/softwaremill/sbt-softwaremill.svg?branch=master)](https://travis-ci.org/softwaremill/sbt-softwaremill)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill)  

A sane set of common build settings.

#### Note: global plugin removed
Since version 1.6.0 the global plugin has been merged back into the per-project plugin. 
If you used that dependency, remove it from your .sbt files in `~/.sbt/1.0/plugins/` and `~/.sbt/1.0/`.

## Usage

For each project where you'd like to use the build settings, add the following your `project/plugins.sbt` file:

````scala
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill" % "1.7.0")
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
  splainSettings            ++ // gives rich output on implicit resolution errors 
  dependencyUpdatesSettings ++ // check dependency updates on startup (max once per 12h)
  wartRemoverSettings       ++ // warts
  acyclicSettings           ++ // check circular dependencies between packages
  ossPublishSettings           // configures common publishing process for all OSS libraries
````

#### Adding more ignored Warts
If you are annoyed by some Wartremover warnings and you'd like to extend the default set of ignored warnings,
you can define them like:

```scala
  .settings(
      wartremoverWarnings in (Compile, compile) --= Seq(
        Wart.DefaultArguments,
        Wart.JavaSerializable
      ),
      wartremoverWarnings in (Test, compile) --= Seq(
        Wart.Var,
        Wart.MutableDataStructures
      ),
      wartremoverWarnings in (IntegrationTest, compile) := Warts.all // custom scope
        .diff(smlWartremoverTestCompileExclusions) // default set
        .diff(Seq(Wart.Var, Wart.MutableDataStructures))
  )
```  

`sbt-softwaremill` comes with:
- [Coursier](https://github.com/coursier/coursier)
- [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html)
- [sbt-pgp](https://github.com/sbt/sbt-pgp)
- [sbt-release](https://github.com/sbt/sbt-release)
- [sbt-sonatype](https://github.com/xerial/sbt-sonatype)
- [sbt-reloadquick](https://github.com/dwijnand/sbt-reloadquick)
- [sbt-revolver](https://github.com/spray/sbt-revolver)
- [acyclic](https://github.com/lihaoyi/acyclic)
- [splain](https://github.com/tek/splain)
- [sbt-updates](https://github.com/rtimush/sbt-updates)
- [sbt-dependency-check](https://github.com/albuch/sbt-dependency-check)

## Releasing your library

`sbt-softwaremill` exposes a default configuration suitable for releasing open source libraries.
- Add `smlBuildSettings` or `ossPublishSettings` to your project's settings
- Ensure you have configured your repository credentials, for example you may need to add
`credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")` to `~/.sbt/1.0/cred.sbt` or
use other method. 
- Run the `release` command
Consider that:
- You need an [OSS Sonatype account](https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html) and sbt-pgp plugin properly configured with generated and published keys.
- Your `README.md` and `docs` directory will be parsed for `"[organization]" %(%) "artifactId" % "someVersion"` and that version value will be bumped.
- If you a have multi-module project, you may need to add `publishArtifact := false` to your root project's settings. 
