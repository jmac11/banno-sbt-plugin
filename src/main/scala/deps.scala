package com.banno
import sbt._
import Keys._

object Akka {
  val version = SettingKey[String]("akka-version")

  def akkaModule(module: String, v: String) =
    "com.typesafe.akka" %% ("akka-" + module) % v

  val settings: Seq[Setting[_]] = Seq(
    version := "2.3.7",
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

object BannoCommonDeps {
  val slf4jVersion = SettingKey[String]("slf4j-version")

  val settings: Seq[Setting[_]] = Seq(
    slf4jVersion := "1.7.5",
    libraryDependencies ++= Seq(
      "org.joda" % "joda-convert" % "1.6",
      "joda-time" % "joda-time" % "2.4",

      "org.slf4j" % "slf4j-api" % slf4jVersion.value,
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion.value,
      "org.slf4j" % "jcl-over-slf4j" % slf4jVersion.value,

      "javax.servlet" % "javax.servlet-api" % "3.0.1"
    )
  ) ++ LogbackDeps.settings
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

object Kafka {
  val version = SettingKey[String]("kafka-version")

  val setVersion: Setting[_] = {
    version := "0.8.2.1"
  }

  val clients: Seq[Setting[_]] = Seq(
    setVersion,
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % version.value,
      "org.apache.kafka" % "kafka-clients" % version.value % "test" classifier "test"
    )
  )

  val settings: Seq[Setting[_]] = Seq(
    setVersion,
    libraryDependencies ++= Seq(
      "org.apache.kafka" %% "kafka" % version.value
    )
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

object Metrics {
  val version = SettingKey[String]("metrics-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "3.1.0",
    libraryDependencies <++= (version, Akka.version) { (v, av) =>
      val msv = s"3.3.0_a${av.take(3)}" // https://github.com/erikvanoosten/metrics-scala#available-versions-abbreviated
      Seq(
        "io.dropwizard.metrics" % "metrics-core"     % "3.1.0-java7", // until https://github.com/dropwizard/metrics/issues/742 is resolved
        "io.dropwizard.metrics" % "metrics-graphite" % v,
        "io.dropwizard.metrics" % "metrics-logback"  % v,
        "nl.grons" %% "metrics-scala"    % msv
      )
    }
  )
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

object Scalaz {
  val version = SettingKey[String]("scalaz-version")
  val streamVersion = SettingKey[String]("scalaz-stream-version")
  val contribVersion = SettingKey[String]("scalaz-contrib-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "7.1.0",
    streamVersion := "0.5a",
    contribVersion := (if (scalaVersion.value.startsWith("2.10")) "0.1.5" else "0.2.0"),
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


object Spray {
  val version = SettingKey[String]("spray-version")

  def olderSpray(v: String) =
    v.startsWith("1.2") || v.startsWith("1.1")

  def sprayModule(module: String, v: String) =
    if (olderSpray(v)) // not cross versioned
      "io.spray" % ("spray-" + module) % v
    else
      "io.spray" %% ("spray-" + module) % v

  val setVersion =
    version := {
      Akka.version.value match {
        case av if av.startsWith("2.3") => "1.3.3"
        case av if av.startsWith("2.2") => "1.2.3"
        case av if av.startsWith("2.1") => "1.1.3"
      }
    }

  val removeWarnAdaptedArgs = scalacOptions ~= (_.filterNot(_ == "-Ywarn-adapted-args"))

  val caching: Seq[Setting[_]] = Seq(
    setVersion,
    libraryDependencies += sprayModule("caching", version.value)
  )

  val client: Seq[Setting[_]] = Seq(
    setVersion,
    removeWarnAdaptedArgs,
    libraryDependencies += sprayModule("client", version.value)
  )

  val routing: Setting[_] =
    libraryDependencies += {
      if (olderSpray(version.value)) {
        sprayModule("routing", version.value)
      } else {
        // From: https://github.com/milessabin/shapeless/wiki/Migration-guide:-shapeless-2.0.0-to-2.1.0#macro-paradise-plugin-required-for-scala-210x
        if (scalaVersion.value.trim == "2.10.5") {
          scalacOptions += "-Yfundep-materialization"
        }
        sprayModule("routing-shapeless2", version.value)
      }
    }

  val server: Seq[Setting[_]] = Seq(
    setVersion,
    removeWarnAdaptedArgs,
    libraryDependencies ++= Seq(sprayModule("can", version.value),
                                sprayModule("testkit", version.value) % "test"),
    routing
  )
}
