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
  private val NEXUS_UPDATE_METADATA_JOB_ID = "41"
  // TODO: instead of invoking nexus to update the maven-metadata.xml, upload a maven-metadata.xml
  def updateMavenMetadata = task {
    if (isSnapshot) {
      log.info("Not updating maven metadata for SNAPSHOT")
    } else {
      log.info("Updating maven metadata")
      Nexus.runScheduledJob(NEXUS_UPDATE_METADATA_JOB_ID) { (status, createdAt) =>
        log.info("Maven Metadata Update - " + status + " at " + createdAt) }
    }
    None
  } describedAs ("Updates the maven-metadata.xml on the repo to publish")

  override def publishAction = updateMavenMetadata dependsOn super.publishAction
  
}
