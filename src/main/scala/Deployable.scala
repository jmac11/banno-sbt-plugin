package com.banno
import sbt._
import Keys._

object Deployable {
  val settings = defaults()

  val noScalaVersionInArtifact = (crossPaths := false)

  def defaults(fatjar: Boolean = true): Seq[Project.Setting[_]] =
    if (fatjar)
      noScalaVersionInArtifact +: FatJar.settings
    else
      Seq(noScalaVersionInArtifact)
}
