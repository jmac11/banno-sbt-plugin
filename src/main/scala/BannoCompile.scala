package com.banno
import sbt._
import Keys._

object BannoCompile {
  val settings = Seq(
    scalacOptions <++= (scalaVersion) map {
      case sv if sv.startsWith("2.9") =>
        Seq("-deprecation", "-unchecked")
      case _ =>
        Seq("-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions",
            "-Ywarn-adapted-args", "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-inaccessible", "-unchecked")
    },
    scalacOptions <++= (scalaVersion) map { sv =>
      if (sv.startsWith("2.9")) Seq("-Ydependent-method-types") else Nil
    },
    scalacOptions <++= (version) map { v =>
      if (v.endsWith("SNAPSHOT")) Nil else Seq("-optimize")
    }
  )
}
