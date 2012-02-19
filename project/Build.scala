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
                            organization := "com.banno",

                            resolvers := Seq(
                              "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/",
                              bannoSnapshots,
                              bannoReleases
                            ),
                            externalResolvers <<= resolvers map { rs =>
                              Resolver.withDefaultResolvers(rs, mavenCentral = false, scalaTools = false)
                            },
                            publishTo <<= (version) { v =>
                              if (v.trim.endsWith("SNAPSHOT")) Some(bannoSnapshots)
                              else Some(bannoReleases)
                            },
                            credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials"),
                            publishMavenStyle := true)

}
