# banno-sbt-plugin #

## Description ##

This is Banno's [sbt][] plugin which provides a structured method of which all Banno sbt projects should abide by. It also includes many commonly used dependencies.

[sbt]: http://www.scala-sbt.org/

## Usage ##

For sbt versions 0.13.x, please use the latest banno-sbt-plugin 1.3.x version.

In `project/plugins.sbt`, add

```scala
resolvers ++= Seq("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots",
                  "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases",
                  "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/")

addSbtPlugin("com.banno" % "banno-sbt-plugin" % "1.3.0")
```

and in the `build.sbt`, add

```scala
import com.banno._

name := "project-name"

BannoSettings.settings
```

It is also a good idea to configure which version of sbt you wish to use by designating in the `project/build.properties`:

    sbt.version=0.13.0
