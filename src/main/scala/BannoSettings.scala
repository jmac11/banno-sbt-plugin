package com.banno
import sbt._
import Keys._

object BannoSettings {
  val settings =
    Seq(organization := "com.banno",
        scalaVersion := "2.9.1"
      ) ++
    BannoNexus.settings ++
    BannoCommonDeps.settings ++
    BannoRelease.settings
}
