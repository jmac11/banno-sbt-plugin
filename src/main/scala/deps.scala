package com.banno
import sbt._
import Keys._

object BannoCommonDeps {
  val settings: Seq[Project.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.joda" % "joda-convert" % "1.1",
      "joda-time" % "joda-time" % "2.0",

      "org.slf4j" % "slf4j-api" % "1.7.5",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.5",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.5"
    )
  )
}

object Akka {
  val version = SettingKey[String]("akka-version")

  def akkaModule(module: String, v: String, sv: String) = sv match {
    case sv if sv.startsWith("2.9.") => "com.typesafe.akka" % ("akka-" + module) % v
    case _                           => "com.typesafe.akka" %% ("akka-" + module) % v
  }

  val settings: Seq[Project.Setting[_]] = Seq(
    version <<= scalaVersion.apply {
      case sv if sv.startsWith("2.9.") => "2.0.2"
      case _ => "2.1.4"
    },
    libraryDependencies <++= (version, scalaVersion) { (v, sv) =>
      Seq(akkaModule("actor", v, sv),
          akkaModule("remote", v, sv),
          akkaModule("slf4j", v, sv),
          akkaModule("testkit", v, sv) % "test")
    }
  )
}

object AsyncHttpClient {
  val version = SettingKey[String]("ning-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "1.7.19",
    libraryDependencies <+= (version)("com.ning" % "async-http-client" % _)
  )
}

object Spray {
  val version = SettingKey[String]("spray-version")

  def sprayMoule(module: String, v: String, sv: String) = sv match {
    case sv if sv.startsWith("2.9.") => "io.spray" % ("spray-" + module) % v
    case _                           => "io.spray" % ("spray-" + module) % v
  }

  val caching: Seq[Project.Setting[_]] = Seq(
    libraryDependencies <+= (version, scalaVersion) { (v, sv) => sprayMoule("caching", v, sv) }
  )

  val settings: Seq[Project.Setting[_]] = Seq(
    version <<= scalaVersion.apply {
      case sv if sv.startsWith("2.9.") => "1.0-RC1"
      case _                           => "1.1-RC1"
    },
    libraryDependencies <++= (version, scalaVersion) { (v, sv) =>
      Seq(sprayMoule("can", v, sv),
          sprayMoule("routing", v, sv),
          sprayMoule("client", v, sv),
          sprayMoule("testkit", v, sv) % "test")
    }
  )
}

object Metrics {
  val version = SettingKey[String]("metrics-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "2.2.0",
    libraryDependencies <++= (version) { v=>
      Seq(
        "com.yammer.metrics" % "metrics-core" % v,
        "com.yammer.metrics" % "metrics-graphite" % v,
        "com.yammer.metrics" % "metrics-logback" % v,
        "nl.grons"           %% "metrics-scala" % v
      )
    }
  )
}

object Scalaz {
  val version = SettingKey[String]("scalaz-version")
  val contribVersion = SettingKey[String]("scalaz-contrib-version")

  val settings: Seq[Project.Setting[_]] = Seq(
    version := "7.0.4",
    contribVersion := "0.1.5",
    libraryDependencies <++= (version, contribVersion) { (v, cv) =>
      Seq(
        "org.scalaz" %% "scalaz-core" % v,
        "org.typelevel" %% "scalaz-contrib-210" % cv
      )
    }
  )
}
