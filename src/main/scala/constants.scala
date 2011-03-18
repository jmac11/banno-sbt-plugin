import sbt._
import java.net.URL

object BannoNexusRepositories {
  implicit val patterns: Patterns = Resolver.mavenStylePatterns
  val BannoExternalRepo   = Resolver.url("Banno External Repo", new URL("http://10.3.0.26:8081/nexus/content/groups/external/"))
  val BannoSnapshotsRepo  = Resolver.url("Banno Snapshots Repo", new URL("http://10.3.0.26:8081/nexus/content/repositories/snapshots"))
  val BannoReleasesRepo   = Resolver.url("Banno Releases Repo", new URL("http://10.3.0.26:8081/nexus/content/repositories/releases"))
}

