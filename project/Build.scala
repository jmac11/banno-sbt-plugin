import sbt._
import Keys._

object MyBuild extends Build {
  val bannoSnapshots = "Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots"
  val bannoReleases = "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases"

  lazy val root = Project(id = "root",
                          base = file("."),
                          settings = Project.defaultSettings ++ myBuildSettings)

  val myBuildSettings = Seq(sbtPlugin := true,
                            name := "banno-sbt-plugin",
                            version := "1.1.1",
                            organization := "com.banno",

                            publishArtifact in (Compile, packageSrc) := false,
                            publishArtifact in (Compile, packageDoc) := false,

                            resolvers := Seq(
                              "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/",
                              bannoSnapshots,
                              bannoReleases
                            ),
                            externalResolvers <<= resolvers map { rs =>
                              Resolver.withDefaultResolvers(rs, mavenCentral = false, scalaTools = false)
                            },

                            // necesary due toa bug in sbt with a plugin depending on multiple other plugins
                            libraryDependencies <++= (scalaVersion, sbtVersion) { (scalaV, sbtV) =>
                              Seq(
                                "com.github.gseitz" % "sbt-release_%s_%s".format(scalaV, sbtV) % "0.4",
                                "com.eed3si9n" % "sbt-assembly_%s_%s".format(scalaV, sbtV) % "0.8.1"
                              )
                            },

                            libraryDependencies += "net.databinder" %% "dispatch-http" % "0.7.8",

                            publishTo <<= (version) { v =>
                              if (v.trim.endsWith("SNAPSHOT")) Some(bannoSnapshots)
                              else Some(bannoReleases)
                            },
                            credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials"),
                            publishMavenStyle := true)

}
