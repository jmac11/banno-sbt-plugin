package com.banno
import sbt._
import Keys._
import sbtrelease.Version
import sbtrelease.Utilities.Yes
import java.net.InetAddress

object BannoSbtPluginAutoUpdating extends SbtPluginAutoUpdating {
  val currentPluginVersion = BuildInfo.version
  val currentSbtBinaryVersion = BuildInfo.sbtBinaryVersion
  val currentSbtScalaBinaryVersion = BuildInfo.scalaBinaryVersion

  val pluginsFile = SettingKey[File]("The plugins.sbt file (usually project/plugins.sbt)")

  val globalSettings = Seq(
    pluginsFile in Global := file("project") / "plugins.sbt",
    onLoad in Global := { state =>
      val _ = (onLoad in Global).value
      if (!(offline in Global).value &&
            nexusIfAccessible && 
            updatePluginIfNecessary("com.banno", "banno-sbt-plugin",
                                    currentSbtBinaryVersion, currentSbtScalaBinaryVersion,
                                    currentPluginVersion,
                                    (pluginsFile in Global).value))
        state.reload
      else
        state
    }
  )

  def nexusIfAccessible =
    try InetAddress.getByName("nexus.banno.com").isReachable(30000)
    catch {
      case t: Throwable => false
    }
}

trait SbtPluginAutoUpdating {

  def updatePluginIfNecessary(groupId: String, name: String,
                              currentSbtBinaryVersion: String,
                              currentSbtScalaBinaryVersion: String,
                              currentPluginVersion: String,
                              pluginsFile: File): Boolean = {

    (for {
      newestPluginVersionInNexus <- Nexus.latestReleasedVersionFor(groupId, s"${name}_${currentSbtScalaBinaryVersion}_${currentSbtBinaryVersion}")

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
