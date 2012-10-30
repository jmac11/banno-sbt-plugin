package com.banno

object Nexus {
  import org.apache.ivy.util.url.CredentialsStore
  import dispatch._
  import Http._

  def latestReleasedVersionFor(groupId: String, artifactId: String): Option[String] = {
    try {
      Http(releaseMetadataPath(groupId, artifactId) <> { xml => Some((xml \\ "release").text) })
    } catch {
      case StatusCode(404, _) => None
    }
  }

  def releaseMetadataPath(org: String, name: String) = {
    val metadataPath = org.replaceAll("\\.", "/") + "/" + name + "/maven-metadata.xml"
    nexusBase / "content/repositories/releases/" / metadataPath
  }

  lazy val nexusBase = :/("nexus.banno.com") / "nexus"

  def nexusAuthenticated(req: Request) = {
    val creds = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "nexus.banno.com")
    req as (creds.getUserName, creds.getPasswd)
  }
}
