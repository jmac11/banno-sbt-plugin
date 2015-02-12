package com.banno
import sbtrelease.Version
import dispatch.classic._

object Nexus {
  import org.apache.ivy.util.url.CredentialsStore

  // httpclient is noisy
  System.setProperty("org.apache.commons.logging.Log",
                     "org.apache.commons.logging.impl.NoOpLog")
  
  import Http._

  val linkRegex = "a href=\".+?\">(.+?)/</a>".r

  def latestReleasedVersionStringsFor(groupId: String, artifactId: String): List[String] =
    getAllLinkNamesAt(releaseDirectoryPath(groupId, artifactId))

  def getMatchingArtifactNamesUnder(groupId: String, startsWith: String): List[String] = {
    getAllLinkNamesAt(artifactsDirectoryPath(groupId)).filter(name => name.startsWith(startsWith))
  }

  def latestReleasedVersionFor(groupId: String, artifactId: String): Option[String] = {
    val versions = latestReleasedVersionStringsFor(groupId, artifactId).flatMap(s => Version.apply(s))
    VersionUtil.newestVersion(versions).map(_.string)
  }

  def releaseDirectoryPath(org: String, name: String) =
    artifactsDirectoryPath(org) /  name

  lazy val nexusBase = :/("nexus.banno.com") / "nexus"

  def nexusAuthenticated(req: Request) = {
    val creds = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "nexus.banno.com")
    req as (creds.getUserName, creds.getPasswd)
  }

  private[this] def artifactsDirectoryPath(groupId: String) =
    nexusBase / "content/repositories/releases/" / (groupId.replaceAll("\\.", "/") + "/")

  private[this] def getAllLinkNamesAt(path: Request): List[String] =
    try {
      Http(path >- linkRegex.findAllMatchIn).map(_.group(1)).toList
    } catch {
      case StatusCode(404, _) => Nil
    }


}
