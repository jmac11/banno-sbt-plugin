import sbt._
import Keys._

object MyBuild extends Build {
  lazy val root = Project(id = "root",
                          base = file(".")).settings(publish := {}, publishLocal := {}).aggregate(plugin, depsTesting).settings(publish := {}, publishLocal := {})

  lazy val plugin = Project(id = "plugin", base = file("./plugin"))

  lazy val depsTesting = Project(id = "deps-testing",
                                 base = file("./deps-testing")).dependsOn(plugin).settings(publish := {}, publishLocal := {})
}
