import sbt._
import com.github.retronym.OneJarProject
import reaktor.scct.ScctProject

trait BannoRepo extends BasicDependencyProject with SnapshotOrRelease { 
  override def managedStyle = ManagedStyle.Maven
  lazy val BannoExternalRepo   = "Banno External Repo" at "http://10.3.0.26:8081/nexus/content/groups/external/"
  lazy val BannoSnapshotsRepo  = "Banno Snapshots Repo" at "http://10.3.0.26:8081/nexus/content/repositories/snapshots"
  lazy val BannoReleasesRepo   = "Banno Releases Repo" at "http://10.3.0.26:8081/nexus/content/repositories/releases"

  override def ivyRepositories = BannoExternalRepo ::
                                 BannoReleasesRepo ::
                                 BannoSnapshotsRepo ::
                                 Resolver.defaultLocal(None) ::
                                 ("Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository") ::
                                 Nil

  Credentials(Path.userHome / ".ivy2" / ".banno_credentials", log)
  
  override def defaultPublishRepository = Some(if (isSnapshot) BannoSnapshotsRepo else BannoReleasesRepo)
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

trait SnapshotOrRelease extends BasicDependencyProject {
  def isSnapshot: Boolean = version.toString.endsWith("SNAPSHOT")
}

trait UpdateMavenMetadataAfterPublish extends BasicDependencyProject with SnapshotOrRelease {
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

trait FatJar extends DefaultProject with OneJarProject {
  lazy val packageJar = super.packageAction
  override def packageAction = onejarTask(onejarTemporaryPath,
    onejarClasspath,
    onejarExtraJars
  ) dependsOn (packageJar) describedAs ("Builds a single-file, executable JAR using One-JAR")
  lazy val oneJarArtifact = Artifact(artifactID, "onejar")
}

trait HadoopShim extends DefaultProject {
  lazy val hadoopShimArtifact = Artifact(artifactID, "hadoop-shim")
  
  def hadoopShimTask = packageTask(hadoopShimPathFinder,
                                   defaultJarPath("-hadoop-shim.jar")) dependsOn (compile, hadoopShimCopy) describedAs ("Packages a jar to be sent to Hadoop")

  lazy val hadoopShim = hadoopShimTask
  
  lazy val packageJar = super.packageAction
  override def packageAction = hadoopShimTask dependsOn (packageJar)

  def hadoopShimTemporaryPath = outputPath / "hadoop-shim"
  def hadoopShimClasspath = runClasspath +++ mainDependencies.scalaJars
  def hadoopShimPathFinder = ((hadoopShimTemporaryPath ##) ** "*")
  
  lazy val hadoopShimCopy = task {
    FileUtilities.clean(hadoopShimTemporaryPath, log)
    
    val (libs, dirs) = hadoopShimClasspath.get.partition(ClasspathUtilities.isArchive)
    
    val tempLibPath = hadoopShimTemporaryPath / "lib"
    FileUtilities.copyFlat(libs, tempLibPath, log)

    dirs.foreach { dir =>
      FileUtilities.copy(((dir ##) ** "*").get, hadoopShimTemporaryPath, log)
    }
    
    None
  }
}
