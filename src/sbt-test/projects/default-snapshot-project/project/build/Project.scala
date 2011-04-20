import sbt._
import Process._
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

  lazy val gitCreateRepo = task {
    "git init" #&& "git add ." #&& "git commit -m init_commit" ! (log)
    None
  }

  lazy val checkGitLog = task { (args) => task {
    val msg = args(0)
    val gitLog: String = ("git --no-pager log" !!)
    if (gitLog.contains(msg)) {
      None
    } else {
      Some("Log:\n%s\n\texpected to contain message: %s".format(gitLog, msg))
    }
  }}

  lazy val checkGitTag = task { (args) => task {
    val msg = args(0)
    val gitTagLog: String = ("git --no-pager tag -l -n" !!)
    if (gitTagLog.contains(msg)) {
      None
    } else {
      Some("Tag List:\n%s\n\texpected to contain message: %s".format(gitTagLog, msg))
    }
  }}
}
