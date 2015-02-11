package com.banno
import sbt._

object BannoSbtPlugin extends AutoPlugin {
  override lazy val buildSettings = BannoSettings.buildSettings 
  override lazy val projectSettings = BannoSettings.settings
  override lazy val globalSettings = BannoSbtPluginAutoUpdating.globalSettings
}
