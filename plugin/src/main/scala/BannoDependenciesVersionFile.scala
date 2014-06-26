package com.banno
import sbt._

object BannoDependenciesVersionFile {
  val bannoDependenciesFileName = "versions-banno-deps.sbt"
  val bannoDependenciesFile = new File(bannoDependenciesFileName)

  def writeBannoDependenciesVersionsToFile(log: Logger, versions: Iterable[(ModuleID, String)]): Unit = {
    val newSettingsContent =  versions.map {
      case (dep, latest) =>
        log.info("updating \"%s\" to %s".format(dep, latest))
        settingKeyLine(dep.name, latest)
    }

    if (newSettingsContent.nonEmpty)
      IO.write(bannoDependenciesFile, newSettingsContent.mkString("\n\n"))
  }

  def appendSnapshotBannoDependencyVersionToFileIfMissing(name: String): Unit = {
    if (!bannoDependenciesFile.exists) IO.touch(bannoDependenciesFile)

    val existingContent = IO.read(bannoDependenciesFile)
    if (!existingContent.contains(settingKeyString(name))) {
      IO.append(bannoDependenciesFile, "\n\n" + settingKeyLine(name, "1-SNAPSHOT"))
    }
  }

  private[this] def settingKeyLine(name: String, version: String): String =
    "SettingKey[String](%s) in Global := \"%s\"".format(settingKeyString(name), version)

  private[this] def settingKeyString(name: String): String = "\"%s-released-version\"".format(name)
}
