package com.banno
import sbt._
import Keys._
import sbtrelease._
import sbtrelease.ReleaseKeys._

object BannoRelease {
  val settings = Release.releaseSettings ++ Seq(
    tagName <<= (version in ThisBuild)(identity),
    releaseVersion <<= (organization, name, scalaVersion) { (g, a, s) => { _ =>
      val latestReleasedVersion = Nexus.latestReleasedVersionFor(g, a + "_" + s)
      latestReleasedVersion.flatMap(v => Version(v).map(_.bumpBugfix.string)).getOrElse(versionFormatError)
    }},
    nextVersion := { ver => Version(ver).map(_.copy(bugfix = None)).map(_.asSnapshot.string).getOrElse(versionFormatError) }
  )
}
