package com.banno
import sbt._
import Keys._

object Deployable {
  val noScalaVersionInArtifact = (crossPaths := false)

  val settings = defaults()

  def defaults(fatjar: Boolean = true): Seq[Project.Setting[_]] =
    if (fatjar)
      noScalaVersionInArtifact +: FatJar.settings
    else
      Seq(noScalaVersionInArtifact)
}
