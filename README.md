# sbt-softwaremill
[![Build Status](https://travis-ci.org/softwaremill/sbt-softwaremill.svg?branch=master)](https://travis-ci.org/softwaremill/sbt-softwaremill)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill-common/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sbt-softwaremill/sbt-softwaremill-common)  

A sane set of common build settings.

#### Note: plugin split

Since version 1.9.0 the plugin is now split into three: `common`, `publish` and `extra`.

## Usage

For each project where you'd like to use the build settings, add some or all of the following your `project/plugins.sbt`
file:

````scala
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common" % "1.9.8")
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-publish" % "1.9.8")
addSbtPlugin("com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-extra" % "1.9.8")
````

Now you can add the appropriate settings in your `build.sbt`, e.g.:

````scala
lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  // your settings, which can override some of commonSmlBuildSettings
) 
````

Each dependency provides a choice of settings:

````scala
// common
commonSmlBuildSettings // compiler flags etc.

// publish
ossPublishSettings
publishTravisSettings
noPublishSettings

// extra - use all or choose
lazy val extraSmlBuildSettings =
  wartRemoverSettings ++        // warts
  acyclicSettings ++            // check circular dependencies between packages
  splainSettings ++             // gives rich output on implicit resolution errors
  dependencyUpdatesSettings ++  // check dependency updates on startup (max once per 12h)
  dependencyCheckSettings
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

`sbt-softwaremill-common` comes with:
- [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html)

`sbt-softwaremill-publish` comes with:
- [sbt-pgp](https://github.com/sbt/sbt-pgp)
- [sbt-release](https://github.com/sbt/sbt-release)
- [sbt-sonatype](https://github.com/xerial/sbt-sonatype)

`sbt-softwaremill-extra` comes with:
- [sbt-reloadquick](https://github.com/dwijnand/sbt-reloadquick)
- [sbt-revolver](https://github.com/spray/sbt-revolver)
- [acyclic](https://github.com/lihaoyi/acyclic)
- [splain](https://github.com/tek/splain)
- [sbt-updates](https://github.com/rtimush/sbt-updates)
- [sbt-dependency-check](https://github.com/albuch/sbt-dependency-check)

## Releasing your library

`sbt-softwaremill-publish` exposes a default configuration suitable for releasing open source libraries.
- Add `commonSmlBuildSettings` or `ossPublishSettings` to your project's settings
- Ensure you have configured your repository credentials, for example you may need to add
`credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")` to `~/.sbt/1.0/cred.sbt` or
use other method. 
- Run the `release` command
Consider that:
- You need an [OSS Sonatype account](https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html) and sbt-pgp plugin properly configured with generated and published keys.
- Your `README.md` and `docs` directory will be parsed for `"[organization]" %(%) "artifactId" % "someVersion"` and that version value will be bumped.
- If you a have multi-module project, you may need to add `publishArtifact := false` to your root project's settings. 

## Releasing your library using Travis

To use, you'll need to include `com.softwaremill.PublishTravis.publishTravisSettings` in the settings of your 
`root` project *only*:

```scala
lazy val rootProject = (project in file("."))
  .settings(publishTravisSettings)
```  
  
Moreover, you should include `ossPublishSettings` in the settings of all the projects you'd like to publish. You might 
need to customise settings such as `sonatypeProfileName`, `scmInfo`, `developers` etc. For example:  
  
```scala
val commonSettings = ossPublishSettings ++ Seq(
  sonatypeProfileName := "com.example"
)
```

The release process is broken into two steps:

1. *local*: `sbt commitRelease`. This sbt command prepares the next release: runs the tests, updates `version.sbt`,
creates the git tag, commits the changes and finally asks the user to push the changes.
2. *remote*: `sbt publishRelease`. This sbt command should be run on Travis, triggered when a new tag is pushed. It
publishes the artifacts to sonatype, and invokes repository release.

See `releaseProcess` in `PublishTravis` for details on how the release steps are defined in both cases.

### Configuration

To run the release, travis needs to know the keypair to sign the artifacts, as well as the username/password to
access `oss.sonatype.org`. These values all need to be prepared in file locally, then packaged, *encrypted* using
a secret that's only known to travis, and committed.

Here's how to do that:

1. create `secring.asc` and `pubring.asc` files with the PGP private/public keys
2. create a `credentials.sbt` file with the following content:

```scala
credentials += Credentials("Sonatype Nexus Repository Manager",
                           "oss.sonatype.org",
                           "SONATYPE_USERNAME",
                           "SONATYPE_PASSWORD")

pgpPassphrase := Some("KEY_PASSWORD").map(_.toArray)
```

3. create an archive: `tar cvf secrets.tar secring.asc pubring.asc credentials.sbt`
4. login to travis: `travis login` (you'll need the travis CLI to do that)
5. encrypt the archive: `travis encrypt-file secrets.tar --add`

This should have two effects:

1. an encrypted file should be created in the top-level directory: `secrets.tar.enc`. This file *should be* committed.
Take care not to commit `secrets.tar`, though :).
2. a `before_install` segment should be added to `travis.yml`. This segment decrypts the secrets file, using 
environmental variables provided by travis

In the `before_install` section, there should also be an entry unpacking the secrets file: `tar xvf secrets.tar`.

If you are doing a matrix build, the release should happen during one of the runs - as the release releases for all
cross-versions.

Example `travis.yml` file:

```
language: scala
scala:
  - 2.12.10
  - 2.13.0
before_install:
  - openssl aes-256-cbc -K $encrypted_f7a2d53f3383_key -iv $encrypted_f7a2d53f3383_iv
    -in secrets.tar.enc -out secrets.tar -d
  - tar xvf secrets.tar
before_cache:
  - du -h -d 1 $HOME/.ivy2/
  - du -h -d 2 $HOME/.sbt/
  - du -h -d 4 $HOME/.coursier/
  - find $HOME/.sbt -name "*.lock" -type f -delete
  - find $HOME/.ivy2/cache -name "ivydata-*.properties" -type f -delete
  - find $HOME/.coursier/cache -name "*.lock" -type f -delete
cache:
  directories:
    - "$HOME/.sbt/1.0"
    - "$HOME/.sbt/boot/scala*"
    - "$HOME/.sbt/cache"
    - "$HOME/.sbt/launchers"
    - "$HOME/.ivy2/cache"
    - "$HOME/.coursier"
script:
  - sbt ++$TRAVIS_SCALA_VERSION test
deploy:
  - provider: script
    script: sbt publishRelease
    skip_cleanup: true
    on:
      all_branches: true
      condition: $TRAVIS_SCALA_VERSION = "2.12.8" && $TRAVIS_TAG =~ ^v[0-9]+\.[0-9]+(\.[0-9]+)?
```

## Releasing sbt-softwaremill

Sbt-softwaremill release process is setup on travis. 
This plugin uses itself to publish binaries to oss-sonatype.

For more details refer to [Releasing your library using Travis](#releasing-your-library-using-travis).
