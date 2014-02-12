package com
import sbt._
import Keys._

package object banno {
  val bannoDependencies = SettingKey[Seq[ModuleID]]("banno-dependencies")
  val bannoDependenciesFileName = "versions-banno-deps.sbt"

  def addBannoDependency(artifactId: String,
                         groupId: String = "com.banno",
                         crossVersion: Boolean = true,
                         snapshotVersion: String = "1-SNAPSHOT",
                         isProvided: Boolean = true): Seq[Setting[_]] = {

    val bannoDepVersion = SettingKey[String]("%s-version".format(artifactId))
    val bannoDepReleasedVersion = SettingKey[String]("%s-released-version".format(artifactId),
                                                     "If you get a warning about this, please add a setting to versions-banno-deps.sbt")

    val depVersion = bannoDepVersion <<= (version, bannoDepReleasedVersion) { (v, rv) =>
      if (v.trim.endsWith("SNAPSHOT")) {
        snapshotVersion
      } else {
        rv
      }
    }

    val withDep = bannoDependencies <+= (bannoDepVersion) { (bv) =>
      val dep = if (crossVersion) groupId %% artifactId % bv
                else groupId % artifactId % bv
      if(isProvided) dep else dep % "provided"
    }

    Seq(depVersion, withDep)
  }

  def addBannoDependencies(artifactIds: String*) =
    artifactIds.foldLeft(Seq[Setting[_]]()) { (settings, artifactId) =>
      settings ++ addBannoDependency(artifactId)
    }
}
