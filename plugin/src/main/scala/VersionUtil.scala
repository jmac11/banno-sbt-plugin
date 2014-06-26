package com.banno

import sbtrelease.Version

object VersionUtil {
  def newestVersion(versions: Seq[Version]): Option[Version] =
    versions.sortBy(v => Tuple3(v.major, v.minor, v.bugfix)).reverse.headOption
}
