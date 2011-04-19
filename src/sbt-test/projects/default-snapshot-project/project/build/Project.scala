import sbt._
import java.util.Properties

class Project(info: ProjectInfo) extends DefaultBannoProject(info) {
  bannoDependency("com.banno", "test-dep")

  lazy val checkFile = task { (args) => task {
      val file = args(0)
      if (new java.io.File(file).exists) None else Some("File: " + file + " does not exist.")
    }
  }

  lazy val checkBannoVersion = task { (args) => task {
    val key = args(0)
    val expectedVersion = args(1)
    val actualVersion = bannoVersions.getProperty(key)
    if (expectedVersion == actualVersion) {
      None
    } else {
      Some("Expected version %s for %s, but was %s".format(expectedVersion, key, actualVersion))
    }
  }}
}
