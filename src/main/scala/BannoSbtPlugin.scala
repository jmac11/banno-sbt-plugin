package com.banno
import sbt._

object BannoSbtPlugin extends AutoPlugin {
  override lazy val projectSettings = BannoSettings.settings
}
