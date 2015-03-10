package com.banno
import sbt._
import Keys._
import sbtbuildinfo.Plugin._

object BuildInfoSettings {
  lazy val settingsRunTime = generateSettings(false)
  lazy val settings = generateSettings(true)
  def generateSettings(generateOnCompile: Boolean): Seq[Setting[_]] =
    buildInfoSettings ++
    Seq(
      buildInfoPackage := "com.banno",
      (if (generateOnCompile)
         sourceGenerators in Compile <+= buildInfo
      else
        sourceGenerators in Runtime <+= buildInfo),
      buildInfoKeys := Seq[BuildInfoKey](name,
                                         version,
                                         scalaVersion,
                                         sbtVersion,
                                         bannoDependencies,
                                         BuildInfoKey.action("gitSha") {
                                           Process("bash" :: "-c" :: "git rev-parse HEAD || echo None" :: Nil) !!
                                         })
    )
}
