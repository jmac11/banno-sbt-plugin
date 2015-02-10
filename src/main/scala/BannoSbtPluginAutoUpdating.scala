package com.banno
import sbt._
import Keys._
import sbtrelease.Version
import sbtrelease.Utilities.Yes

object BannoSbtPluginAutoUpdating extends SbtPluginAutoUpdating {
  val currentPluginVersion = BuildInfo.version

  val pluginsFile = SettingKey[File]("The plugins.sbt file (usually project/plugins.sbt)")

  val settings = Seq(
    pluginsFile := baseDirectory.value / "project" / "plugins.sbt",
    onLoad in Global := { state =>
      val _ = (onLoad in Global).value
      // TODO how to not run in jenkins or if offline
      if (updatePluginIfNecessary("com.banno", "banno-sbt-plugin", currentPluginVersion, pluginsFile.value))
        state.reload
      else
        state
    }
  )
}

trait SbtPluginAutoUpdating {

  def updatePluginIfNecessary(groupId: String, name: String,
                              currentPluginVersion: String,
                              pluginsFile: File): Boolean = {

    val allPluginArtifactIds = Nexus.getMatchingArtifactNamesUnder(groupId, startsWith = name)
    val scalaSbtBinaryVersionsForPlugin = allPluginArtifactIds.flatMap(extractSbtAndScalaVersions)
    val newestScalaSbtVersions = scalaSbtBinaryVersionsForPlugin.sortBy(scalaSbtBinaryOrdering).reverse.headOption

    (for {
      (scalaV, sbtV) <- newestScalaSbtVersions
      newestPluginVersionInNexus <- Nexus.latestReleasedVersionFor(groupId, s"${name}_${scalaV}_${sbtV}")

      if (VersionUtil.isNewerThanByStrings(newestPluginVersionInNexus, currentPluginVersion)) 
    } yield { 
      SimpleReader.readLine(s"Update '${name}' from '${currentPluginVersion}' to '${newestPluginVersionInNexus}' (y/n)? [y] : ") match {
        case Yes() =>
          // TODO also update the project/build.properties to the newest sbt
          updatePluginsFileFor(pluginsFile, groupId, name, newestPluginVersionInNexus)
          true
        case _ => false
      }
    }) getOrElse false
  }

  private[this] def updatePluginsFileFor(pluginsFile: File, groupId: String, name: String, version: String) = {
    val pluginsFileLines = IO.readLines(pluginsFile)
    val pluginLine = ("addSbtPlugin\\(\"" + groupId + "\" % \"" + name + "\" % \"[0-9\\.]+(-SNAPSHOT)?\"\\)").r
    val updatedLines = pluginsFileLines.collect {
      case pluginLine(_) =>
        "addSbtPlugin(\"" + groupId + "\" % \"" + name + "\" % \"" + version + "\")"
      case line => line
    }
    IO.writeLines(pluginsFile, updatedLines)
  }


  // i gave up trying to find something in sbt to do this
  private[this] def extractSbtAndScalaVersions(str: String): Option[(String,String)] =
    "_([0-9\\.]+)_([0-9\\.]+)".r.findFirstMatchIn(str).map(m => Pair(m.group(1), m.group(2)))

  private[this] def scalaSbtBinaryOrdering(scalaSbtVersion: (String, String)) =
    (for {
       scalaV <- Version(scalaSbtVersion._1)
       sbtV <- Version(scalaSbtVersion._2)
     } yield ((sbtV.major, sbtV.minor), (scalaV.major, scalaV.minor))) get


}
