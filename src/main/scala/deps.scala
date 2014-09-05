package com.banno
import sbt._
import Keys._

object BannoCommonDeps {
  val settings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.joda" % "joda-convert" % "1.6",
      "joda-time" % "joda-time" % "2.4",

      "org.slf4j" % "slf4j-api" % "1.7.5",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.5",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.5",

      "javax.servlet" % "javax.servlet-api" % "3.0.1"
    )
  ) ++ LogbackDeps.settings
}

object ScalaModules {
  val xmlVersion = SettingKey[String]("scala-xml-version")

  def scalaModule(module: String, version: String) =
    "org.scala-lang.modules" %% ("scala-" + module) % version

  val xml: Seq[Setting[_]] = Seq(
    xmlVersion := "1.0.2",
    libraryDependencies ++= {
      if (!scalaVersion.value.startsWith("2.10"))
        Seq(
          scalaModule("xml", xmlVersion.value),
          scalaModule("parser-combinators", xmlVersion.value)
        )
      else
        Nil
    }

  )
}

object LogbackDeps {
  val version = SettingKey[String]("logback-version")

  def logbackModule(module: String, version: String) =
    "ch.qos.logback" % ("logback-" + module) % version

  val settings: Seq[Setting[_]] = Seq(
    version := "1.0.13",
    libraryDependencies ++= Seq(
      logbackModule("core", version.value),
      logbackModule("classic", version.value)
    )
  )
}

object Akka {
  val version = SettingKey[String]("akka-version")

  def akkaModule(module: String, v: String) =
    "com.typesafe.akka" %% ("akka-" + module) % v

  val settings: Seq[Setting[_]] = Seq(
    version := "2.3.6",
    libraryDependencies <++= (version) { (v) =>
      Seq(akkaModule("actor", v),
          akkaModule("remote", v),
          akkaModule("slf4j", v),
          akkaModule("testkit", v) % "test")
    }
  )
}

object AsyncHttpClient {
  val version = SettingKey[String]("ning-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "1.7.19",
    libraryDependencies <+= (version)("com.ning" % "async-http-client" % _)
  )
}

object Spray {
  val version = SettingKey[String]("spray-version")

  def sprayModule(module: String, v: String) =
    "io.spray" %% ("spray-" + module) % v

  val setVersion =
    version := {
      Akka.version.value match {
        case av if av.startsWith("2.3") => "1.3.1"
        case av if av.startsWith("2.2") => "1.2.1"
        case av if av.startsWith("2.1") => "1.1.1"
      }
    }

  val removeWarnAdaptedArgs = scalacOptions ~= (_.filterNot(_ == "-Ywarn-adapted-args"))

  val caching: Seq[Setting[_]] = Seq(
    setVersion,
    libraryDependencies <+= (version) { (v) => sprayModule("caching", v) }
  )

  val client: Seq[Setting[_]] = Seq(
    setVersion,
    removeWarnAdaptedArgs,
    libraryDependencies <+= (version) { (v) => sprayModule("client", v) }
  )

  val server: Seq[Setting[_]] = Seq(
    setVersion,
    removeWarnAdaptedArgs,
    libraryDependencies <++= (version) { (v) =>
      Seq(sprayModule("can", v),
          sprayModule("routing-shapeless2", v),
          sprayModule("testkit", v) % "test")
    }
  )
}

object Metrics {
  val version = SettingKey[String]("metrics-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "3.0.2",
    libraryDependencies <++= (version, Akka.version) { (v, av) =>
      val msv = s"3.2.1_a${av.take(3)}" // https://github.com/erikvanoosten/metrics-scala#available-versions-abbreviated
      Seq(
        "com.codahale.metrics" % "metrics-core"     % v,
        "com.codahale.metrics" % "metrics-graphite" % v,
        "com.codahale.metrics" % "metrics-logback"  % v,
        "nl.grons"            %% "metrics-scala"    % msv
      )
    }
  )
}

object Scalaz {
  val version = SettingKey[String]("scalaz-version")
  val streamVersion = SettingKey[String]("scalaz-stream-version")
  val contribVersion = SettingKey[String]("scalaz-contrib-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "7.0.5",
    streamVersion := "0.3",
    contribVersion := "0.1.5",
    libraryDependencies <++= (version, streamVersion, contribVersion) { (v, sv, cv) =>
      Seq(
        "org.scalaz" %% "scalaz-core" % v,
        "org.scalaz" %% "scalaz-concurrent" % v,
        "org.scalaz" %% "scalaz-effect" % v,
        "org.scalaz" %% "scalaz-scalacheck-binding" % v % "test",
        "org.scalaz.stream" %% "scalaz-stream" % sv,
        "org.typelevel" %% "scalaz-contrib-210" % cv
      )
    }
  )
}

object Dispatch {
  val version = SettingKey[String]("dispatch-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "0.11.2",
    libraryDependencies <++= (version) { (v) =>
      Seq(
        "net.databinder.dispatch" %% "dispatch-core" % v
      )
    }
  )
}
