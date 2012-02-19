import sbt._

class Project(info: ProjectInfo) extends DefaultBannoProject(info) {
  bannoDependency("com.banno", "test-dep")
  
  lazy val checkFile = task { (args) => task {
      val file = args(0)
      if (new java.io.File(file).exists) None else Some("File: " + file + " does not exist.")
    }
  }
}
