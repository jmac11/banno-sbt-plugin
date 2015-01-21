package com.banno
import sbtrelease.Version
import dispatch.classic._

object Nexus {
  import org.apache.ivy.util.url.CredentialsStore

  // httpclient is noisy
  System.setProperty("org.apache.commons.logging.Log",
                     "org.apache.commons.logging.impl.NoOpLog")
  
  import Http._

  val versionLinkRegex = "a href=\".+?\">(.+?)/</a>".r

  def latestReleasedVersionStringsFor(groupId: String, artifactId: String): List[String] = {
    try {
      Http(releaseDirectoryPath(groupId, artifactId) >- versionLinkRegex.findAllMatchIn).map(_.group(1)).toList
    } catch {
      case StatusCode(404, _) => Nil
    }
  }

  def latestReleasedVersionFor(groupId: String, artifactId: String): Option[String] = {
    val versions = latestReleasedVersionStringsFor(groupId, artifactId).flatMap(s => Version.apply(s))
    VersionUtil.newestVersion(versions).map(_.string)
  }

  def releaseDirectoryPath(org: String, name: String) = {
    val metadataPath = org.replaceAll("\\.", "/") + "/" + name + "/"
    nexusBase / "content/repositories/releases/" / metadataPath
  }

  lazy val nexusBase = :/("nexus.banno.com") / "nexus"

  def nexusAuthenticated(req: Request) = {
    val creds = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "nexus.banno.com")
    req as (creds.getUserName, creds.getPasswd)
  }
}
