package com.banno
import sbt._
import Keys._

object BannoSettings extends Plugin {
  override val settings =
    Seq(organization := "com.banno",
        scalaVersion := "2.9.1",
        version := "1.0-SNAPSHOT") ++
    BannoNexus.settings ++
    BannoCommonDeps.settings
}
