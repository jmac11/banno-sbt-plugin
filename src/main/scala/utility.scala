import sbt._
import reaktor.scct.ScctProject

trait BannoRepo extends BasicScalaProject with SnapshotOrRelease { 
  override def managedStyle = ManagedStyle.Maven
  lazy val BannoExternalRepo   = "Banno External Repo" at "http://10.3.0.26:8081/nexus/content/groups/external/"
  lazy val BannoSnapshotsRepo  = "Banno Snapshots Repo" at "http://10.3.0.26:8081/nexus/content/repositories/snapshots"
  lazy val BannoReleasesRepo   = "Banno Releases Repo" at "http://10.3.0.26:8081/nexus/content/repositories/releases"

  override def ivyRepositories = Resolver.defaultLocal(None) ::
                                 BannoExternalRepo ::
                                 BannoReleasesRepo ::
                                 BannoSnapshotsRepo ::
                                 ("Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository") ::
                                 Nil

  Credentials(Path.userHome / ".ivy2" / ".banno_credentials", log)
  lazy val publishTo = if (isSnapshot) BannoSnapshotsRepo else BannoReleasesRepo
}

trait JRebelScan extends BasicWebScalaProject {
  // For use with JRebel - kills SBT's redeploy detection in favor of JRebel's
  override def scanDirectories = Nil
}

trait CiTask extends BasicScalaProject { scct: ScctProject =>
  lazy val ciActions = List(clean, cleanLib, update, test, scct.testCoverage, doc, publish)
  lazy val ci = task {
    ciActions.foldLeft(None: Option[String]) { (result, task) => result orElse act(task.name) }
  } describedAs "Runs ci tasks"
  lazy val ccq = consoleQuickAction dependsOn compile
}

trait SnapshotOrRelease extends BasicScalaProject {
  def isSnapshot: Boolean = version.toString.endsWith("SNAPSHOT")
}

trait UpdateMavenMetadataAfterPublish extends BasicScalaProject with SnapshotOrRelease {
  import org.apache.ivy.util.url.CredentialsStore
  import dispatch._
  import Http._
  
  // TODO: instead of invoking nexus to update the maven-metadata.xml, upload a maven-metadata.xml
  def updateMavenMetadata = task {
    if (isSnapshot) {
      log.info("Not updating maven metadata for SNAPSHOT")
    } else {
      log.info("Updating maven metadata")
      val creds = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "10.3.0.26")
      val request = :/("10.3.0.26", 8081) / "nexus/service/local/schedule_run/41" as (creds.getUserName, creds.getPasswd)
      Http(request <> { xml =>
        val status = (xml \\ "status")(0).text
                       val created = (xml \\ "created")(0).text
                       log.info("Maven Metadata Update - " + status + " at " + created)
                     })
    }
    None
  } describedAs ("Updates the maven-metadata.xml on the repo to publish")

  override def publishAction = updateMavenMetadata dependsOn super.publishAction
  
}
