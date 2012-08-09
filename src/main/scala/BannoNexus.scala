package com.banno
import sbt._
import Keys._

object BannoNexus {
  val bannoSnapshots = "Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots"
  val bannoReleases = "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases"
  val NEXUS_UPDATE_METADATA_JOB_ID = "6"

  val settings: Seq[Project.Setting[_]] = Seq(
    resolvers := Seq(
      Resolver.defaultLocal,
      "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/",
      bannoSnapshots,
      bannoReleases
    ),
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = false)
    },
    publishTo <<= (version) { v =>
      if (v.trim.endsWith("SNAPSHOT")) {
        Some(bannoSnapshots)
      } else {
        Some(bannoReleases)
      }
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials"),
    publishMavenStyle := true,
    publish <<= publish map { (u: Unit) =>
      Nexus.runScheduledJob(NEXUS_UPDATE_METADATA_JOB_ID) { (status, created) =>
        println("Updated nexus, status = %s, created = %s".format(status, created))
      }
    }
  )
}
