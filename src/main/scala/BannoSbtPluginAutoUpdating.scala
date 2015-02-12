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

  val globalSettings =
    makeGlobalSettings("com.banno", "banno-sbt-plugin",
                       currentPluginVersion, currentSbtBinaryVersion, currentSbtScalaBinaryVersion)
}

trait SbtPluginAutoUpdating {
  lazy val pluginsFile = settingKey[File]("The plugins.sbt file (usually project/plugins.sbt)")
  lazy val autoUpdateBannoSbtPlugin = settingKey[Boolean]("Auto-update banno-sbt-plugin")
  lazy val updateBannoPlugin = taskKey[Unit]("Updates the banno-sbt-plugin to the newest available version")

  def makeGlobalSettings(groupId: String, name: String,
                         currentPluginVersion: String, currentSbtBinaryVersion: String, currentSbtScalaBinaryVersion: String) = Seq(
    pluginsFile in Global := file("project") / "plugins.sbt",
    autoUpdateBannoSbtPlugin in Global := userIsntJenkins && notOverriddenBySysProp,
    updateBannoPlugin in Global := {
      updatePluginIfNecessary(groupId, name,
                              currentSbtBinaryVersion, currentSbtScalaBinaryVersion,
                              currentPluginVersion,
                              (pluginsFile in Global).value)
    },
    onLoad in Global := { state =>
      val _ = (onLoad in Global).value
      if ((autoUpdateBannoSbtPlugin in Global).value &&
            !(offline in Global).value &&
            nexusIfAccessible && 
            updatePluginIfNecessary(groupId, name,
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

  def userIsntJenkins = {
    val username = System.getProperty("user.name")
    !Set("root", "jenkins").contains(username)
  }

  def notOverriddenBySysProp =
    System.getProperty("banno.sbt.no.autoupdate") == null


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
}
