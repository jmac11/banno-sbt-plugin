package com.banno
import sbt._
import Keys._

object BannoCompile {
  val settings = Seq(
    scalacOptions <++= (scalaVersion) map {
      case sv if sv.startsWith("2.9") =>
        Seq("-deprecation", "-explaintypes", "-unchecked")
      case _ =>
        Seq("-deprecation", "-feature", "-explaintypes", "-unchecked")
    },
    scalacOptions <++= (scalaVersion) map { sv =>
      if (sv.startsWith("2.9")) Seq("-Ydependent-method-types") else Nil
    },
    scalacOptions <++= (version) map { v =>
      if (v.endsWith("SNAPSHOT")) Nil else Seq("-optimize")
    }
  )
}
