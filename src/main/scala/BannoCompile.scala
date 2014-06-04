package com.banno
import sbt._
import Keys._

object BannoCompile {
  val settings = Seq(
    scalacOptions <++= (scalaVersion) map {
      case sv if sv.startsWith("2.9") =>
        Seq("-deprecation", "-unchecked")
      case _ =>
        Seq("-deprecation", "-feature", "-language:postfixOps",
            "-language:implicitConversions", "-language:higherKinds", "-language:existentials", "-language:postfixOps",
            "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-inaccessible", "-unchecked")
    },
    scalacOptions <++= (scalaVersion) map { sv =>
      if (sv.startsWith("2.9")) Seq("-Ydependent-method-types") else Nil
    },
    scalacOptions in Test <++= (scalaVersion) map {
      case sv if sv.startsWith("2.9") => Nil
      case _                          => Seq("-language:reflectiveCalls")
    }
  )
}
