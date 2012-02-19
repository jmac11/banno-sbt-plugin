package com.banno
import sbt._
import Keys._

object Specs2 {
  val version = SettingKey[String]("specs2-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "1.8.1",
    libraryDependencies <+= (version) { v =>
      "org.specs2" %% "specs2" % v % "test"
    }
  )
}

