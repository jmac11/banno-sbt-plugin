import sbt._
import java.util.Properties

sealed case class BannoDep(groupId: String, artifactId: String, snapshotVersion: String) {
  def propKey = groupId + "." + artifactId
}
  
trait VariableBannoDepVersions extends BasicScalaProject with SnapshotOrRelease {
  var bannoDependencies: Set[BannoDep] = Set()
  
  override def libraryDependencies = super.libraryDependencies ++ bannoDependenciesAsModuleIds

  def bannoDependency(groupId: String, artifactId: String): Unit = {
    bannoDependency(groupId, artifactId, "1.0-SNAPSHOT")
  }

  def bannoDependency(groupId: String, artifactId: String, snapshotVersion: String) : Unit = {
    bannoDependencies += BannoDep(groupId, artifactId, snapshotVersion)
  }

  lazy val updateBannoReleaseVersions = task {
    val newVersions = new Properties
    bannoDependencies foreach { dep =>
      val latestVersion = Nexus.latestReleasedVersionFor(dep.groupId, appendScalaVersion(dep.artifactId)).getOrElse(throw new RuntimeException("Unable to find release for " + dep))
      newVersions.setProperty(dep.propKey, latestVersion)                         
    }
    log.info("Setting Banno Versions to:")
    newVersions.list(new java.io.PrintWriter(new LoggerWriter(log, Level.Info)))
    FileUtilities.writeStream(bannoVersionsPath.asFile, log) { stream =>
      newVersions.store(stream, null)
      None
    }
    // TODO: commit change
  }

  lazy val bannoVersionsPath = path("project") / "banno-versions.properties"
  
  protected def bannoVersions: Properties = {
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
      bannoVersions.getProperty(dep.propKey)                        
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

trait ReleaseVersioning extends BasicScalaProject {
  lazy val versionSnapshotToRelease = task {
    modifyVersion("Release version") { currentVersion =>
      val lastVersionStr = Nexus.latestReleasedVersionFor(organization, name + "_" + buildScalaVersion)
      val lastVersion = lastVersionStr.map { v =>
        Version.fromString(v) match {
          case Right(version: BasicVersion) => version
          case value => throw new RuntimeException("Unable to parse version: " + value)
        }
      }
      val nextVersionMaybe = lastVersion.map(v => v.incrementMicro)     
      nextVersionMaybe.getOrElse(currentVersion.incrementMicro.withExtra(None))
    }
  }
  
  lazy val versionReleaseToSnapshot = task {
    modifyVersion("Snapshot version") { v => BasicVersion(v.major, v.minor, None, Some("SNAPSHOT"))}
  }

  private def modifyVersion(msgHeader: String)(f: (BasicVersion) => Version): Option[String] = {
    projectVersion.get match {
      case Some(version: BasicVersion) =>
        val newVersion = f(version)
        log.info("Changing version to " + newVersion)
        projectVersion() = newVersion
        saveEnvironment()
        Git.commit("project/build.properties", "%s: %s".format(msgHeader, newVersion), log)
      case weird => 
        Some("Can't modify " + weird)
    }
  }

  // noChangeSinceLastRelease
}

trait BannoReleaseProcess extends VariableBannoDepVersions with ReleaseVersioning
  // override def releaseAction
