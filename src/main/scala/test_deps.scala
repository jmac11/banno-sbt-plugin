package com.banno
import sbt._
import Keys._

object Specs2 {
  val version = SettingKey[String]("specs2-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "1.14",
    libraryDependencies <+= (version) { v =>
      "org.specs2" %% "specs2" % v % "test"
    }
  )
}

object Scalacheck {
  val version = SettingKey[String]("scalacheck-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "1.10.0",
    libraryDependencies <+= (version) { v =>
      "org.scalacheck" %% "scalacheck" % v % "test"
    }
  )
}


object ScalaTest {
  val version = SettingKey[String]("scalatest-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "1.9.1",
    libraryDependencies <+= (version) { v =>
      "org.scalatest" %% "scalatest" % v % "test"
    }
  )

}
