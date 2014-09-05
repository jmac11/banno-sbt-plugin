### *DISCLAIMER* This is now open source under Apache License 2 (see [LICENSE.txt](./LICENSE.txt) for it). This still contains a few things that are specific to Banno, but it's not that bad.

# banno-sbt-plugin #

This is Banno's [sbt][] plugin which provides a structured method of which all Banno sbt projects should abide by. It also includes many commonly used dependencies.

## Installation ##

For sbt versions 0.13.x, please use the latest banno-sbt-plugin 1.3.x version.

In `project/plugins.sbt`, add

```scala
resolvers ++= Seq("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots",
                  "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases",
                  "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/")

addSbtPlugin("com.banno" % "banno-sbt-plugin" % "1.3.5")
```

and in the `build.sbt`, add

```scala
import com.banno._

name := "project-name"

BannoSettings.settings
```

It is also a good idea to configure which version of sbt you wish to use by designating in the `project/build.properties`: `sbt.version=0.13.1`

## Things the banno-sbt-plugin can do

### Publishing information

It handles the repositories for snapshots and releases. It also will publish to our maven repository using https://github.com/arktekk/sbt-aether-deploy

### Common dependencies and exclusions

The `banno-sbt-plugin` will by default add some common dependencies: They are:

 - joda-time
 - slf4j-api
 - log4j-over-slf4j
 - jcl-over-slf4j
 - logback-core
 - logback-classic

It also will add many entries to the `ivyXML` setting that either interfere with the above or have been troublesome in the past.

### Commonly used dependencies

`banno-sbt-plugin` has settings for many commonly used dependencies. They are

 - `Akka.settings` will add akka-actor, akka-remote, akka-slf4j, akka-testkit
 - `AsyncHttpClient.settings`
 - `Spray.caching` will add the spray-caching dependency
 - `Spray.client` will add the spray-client dependency
 - `Spray.server` will add spray-can, spray-routing, spray-testkit
 - `Metrics.settings` will add http://metrics.codahale.com/ and https://github.com/erikvanoosten/metrics-scala
 - `Scalaz.settings` will add scalaz-core, scalaz-concurrent, scalaz-effect, scalaz-scalacheck-binding, scalaz-stream, scalaz-contrib-210
 - `Specs2.settings` will add http://specs2.org
 - `Scalacheck.settings` will add https://github.com/rickynils/scalacheck
 - `ScalaTest.settings` will add http://scalatest.org/
 - `HBaseTestingUtility.settings` will add many many hadoop test jars

Most of them commonly follow overriding version with: `Akka.version := "2.3.0"` (change for dependency setting you're using)

### Nice Banno dependencies

`addBannoDependency` will add a Banno library as a dependency for you very easily. It will also handle versioning from a SNAPSHOT to a released version while releasing. There is also `addBannoDependencies` for adding multiple of them.

### Releases

`banno-sbt-plugin` mixes in the [sbt-release][] plugin to do a standard release process:

 1. set the project version from SNAPSHOT to the next [semver][] version
 1. update banno dependencies to released versions
 1. run tests (via the `scripted` command)
 1. package/publish
 1. tag
 1. set back to snapshot version
 1. push the changes.

### Symlink-ing outside projects

It is easy to work accross libs/projects by symlinking in the external project underneath the working project with the same name as the banno dependency.

For example:

If I have `addBannoDependency("banno-utils")` in my working project and I want to work on banno-utils at the same time. If I have `object Build extends com.banno.BannoBuild("api")` or likewise in my `project/Build.scala`, I can symlink the banno-utils under my project with the name `banno-utils`. A reload and clean will ensure that it creates it as a submodule project.


### Misc

There a few other things that the banno-sbt-plugin does do.

 - Build a docker image by adding `Docker.settings`
 - Fat jar deployments via `Deployable.settings`
 - standard compile options
 - sane memory defaults for `run`
 - prettier prompt
 - [sbt-revolver](https://github.com/spray/sbt-revolver) support
 - `ci` task for test and publish
 - `clear-local-banno-artifacts` will clear any locally published and cached banno dependencies.

[sbt]: http://www.scala-sbt.org/
[sbt-release]: http://github.com/sbt/sbt-release
[semver]: http://semver.org
