package com.banno

import sbtrelease.Version

object VersionUtil {
  def newestVersion(versions: Seq[Version]): Option[Version] =
    versions.sortBy(v => Tuple3(v.major, v.minor, v.bugfix)).reverse.headOption

  def newestVersionByStrings(versions: Seq[String]): Option[Version] =
    versions.flatMap(s => Version.apply(s)).sortBy(v => Tuple3(v.major, v.minor, v.bugfix)).reverse.headOption

  def isNewerThanByStrings(version: String, isNewerThanThisVersion: String): Boolean =
    newestVersionByStrings(Seq(version, isNewerThanThisVersion)).headOption.exists(v => v.toString == version)
}
