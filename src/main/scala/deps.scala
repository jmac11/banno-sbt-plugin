package com.banno
import sbt._
import Keys._

object BannoCommonDeps {
  val settings: Seq[Project.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.joda" % "joda-convert" % "1.1",
      "joda-time" % "joda-time" % "2.0"
    )
  )
}

object Akka {
  val version = SettingKey[String]("akka-version")

  def akkaModule(module: String, v: String) =
    (if (v.startsWith("1")) "se.scalablesolutions.akka" else "com.typesafe.akka") % ("akka-" + module) % v

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "2.0.1",
    libraryDependencies <++= (version) { v =>
      Seq(akkaModule("actor", v),
          akkaModule("remote", v),
          akkaModule("slf4j", v),
          akkaModule("testkit", v) % "test")
    }
  )
}

