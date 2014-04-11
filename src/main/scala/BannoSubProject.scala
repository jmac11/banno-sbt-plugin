package com.banno
import sbt._
import Keys._

case class BannoSubProject(pid: String, root: Project,
                           defaultSettings: Seq[Setting[_]] = BannoSettings.settings ++ Seq(publish := {}, publishLocal := {})) {

  lazy val proj = project.dependsOn(root % "compile->test").settings(name := pid).settings(defaultSettings: _*)
}
