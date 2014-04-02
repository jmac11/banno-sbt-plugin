package com.banno
import sbtrelease.Version

object Nexus {
  import org.apache.ivy.util.url.CredentialsStore
  import dispatch._
  import Http._

  val versionLinkRegex = "a href=\".+?\">([0-9.]+?)/</a>".r

  def latestReleasedVersionFor(groupId: String, artifactId: String): Option[String] = {
    try {
      val versionStrings: List[String] =
        Http(releaseDirectoryPath(groupId, artifactId) >- versionLinkRegex.findAllMatchIn).map(_.group(1)).toList
      val versions = versionStrings.flatMap(s => Version.apply(s))
      VersionUtil.newestVersion(versions).map(_.string)
    } catch {
      case StatusCode(404, _) => None
    }
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
