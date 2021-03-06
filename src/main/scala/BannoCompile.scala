package com.banno
import sbt._
import Keys._

object BannoCompile {
  val settings = Seq(
    scalacOptions := Seq("-deprecation", "-feature", "-language:postfixOps", "-Xlint", "-Xlog-free-terms", "-Xlog-free-types",
                         "-language:implicitConversions", "-language:higherKinds", "-language:existentials", "-language:postfixOps",
                         "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-inaccessible", "-unchecked"),
    scalacOptions in Test += "-language:reflectiveCalls"
  )
}
