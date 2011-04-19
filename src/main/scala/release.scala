import sbt._

sealed case class BannoDep(groupId: String, artifactId: String, snapshotVersion: String)  
  
trait VariableBannoDepVersions extends BasicScalaProject with SnapshotOrRelease {
  import java.util.Properties
  
  var bannoDependencies: Set[BannoDep] = Set() 
  override def libraryDependencies = super.libraryDependencies ++ bannoDependenciesAsModuleIds

  def bannoDependency(groupId: String, artifactId: String): Unit = {
    bannoDependency(groupId, artifactId, "1.0-SNAPSHOT")
  }

  def bannoDependency(groupId: String, artifactId: String, snapshotVersion: String) : Unit = {
    bannoDependencies += BannoDep(groupId, artifactId, snapshotVersion)
  }

  lazy val bannoVersionsPath = path("project") / "banno-versions.properties"
  
  def bannoVersions: Properties = {
    val bannoVersions = new Properties()
    FileUtilities.readStream(bannoVersionsPath.asFile, log) { stream =>
      bannoVersions.load(stream)
      None                                                   
    }
    bannoVersions
  }
  
  private def versionForBannoDep(dep: BannoDep): String= {
    if (isSnapshot) {
      dep.snapshotVersion
    } else {
      bannoVersions.getProperty(dep.groupId + "." + dep.artifactId)                        
    }
  }

  
  private def bannoDependenciesAsModuleIds(): Set[ModuleID] = {
    bannoDependencies map { dep =>
      val BannoDep(groupId, artifactId, snapshotVersion) = dep
      ModuleID(groupId, appendScalaVersion(artifactId), versionForBannoDep(dep))
    }
  }
  
  private def appendScalaVersion(artifactId: String): String = {
    artifactId + "_" + buildScalaVersion
  }
  
}

  // def updateBannoReleaseVersions
  // def versionSnapshotToRelease
  // def versionReleaseToSnapshot
  // override def releaseAction

trait BannoReleaseProcess extends VariableBannoDepVersions
