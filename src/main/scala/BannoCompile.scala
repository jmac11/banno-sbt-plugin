package com.banno
import sbt._
import Keys._

object BannoCompile {
  val settings = Seq(
    scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps",
                         "-language:implicitConversions", "-language:higherKinds", "-language:existentials", "-language:postfixOps",
                         "-Ywarn-adapted-args", "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-inaccessible", "-unchecked"),
    scalacOptions in Test += "-language:reflectiveCalls"
  )
}
