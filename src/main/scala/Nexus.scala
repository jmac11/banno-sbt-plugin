package com.banno

object Nexus {
  import org.apache.ivy.util.url.CredentialsStore
  import dispatch._
  import Http._

  def runScheduledJob(jobId: String)(f: (String, String) => Unit) {
    val request =  nexusBaseAuthenticated / "schedule_run" / jobId
    Http(request <> { xml =>
      val status = (xml \\ "status")(0).text
      val created = (xml \\ "created")(0).text
      f(status,created)
   })
  }

  def latestReleasedVersionFor(groupId: String, artifactId: String): Option[String] = {
    val params = Map("g" -> groupId, "a" -> artifactId, "v" -> "LATEST", "r" -> "releases", "e" -> "pom")
    val request = nexusBase / "artifact/maven/resolve" <<? params
    try {
      Http(request <> { xml => Some((xml \\ "version").text) })
    } catch {
      case StatusCode(404, _) => None
    }
  }

  lazy val nexusBase = :/("nexus.banno.com") / "nexus/service/local"

  lazy val nexusBaseAuthenticated = {
    val creds = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "nexus.banno.com")
    nexusBase as (creds.getUserName, creds.getPasswd)
  }
}
