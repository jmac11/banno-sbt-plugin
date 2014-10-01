package com.banno
import sbt._
import Keys._
import sbtbuildinfo.Plugin._

object BuildInfo {
  val settings: Seq[Setting[_]] = Seq(
    addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2"),
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.banno"
  ) ++ buildInfoSettings
}
