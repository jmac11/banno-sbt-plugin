package com.banno
import sbt._
import Keys._
import aether.Aether

object BannoNexus {
  val bannoSnapshots = "Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots"
  val bannoReleases = "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases"
  val NEXUS_UPDATE_METADATA_JOB_ID = "6"

  val settings: Seq[Project.Setting[_]] = Seq(
    resolvers := Seq(
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
    credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials")
  ) ++ Aether.aetherPublishSettings
}
