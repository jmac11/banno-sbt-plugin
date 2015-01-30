package com.banno
import sbt._
import Keys._
import sbtbuildinfo.Plugin._
import java.util.Date

object BuildInfoSettings {
  val settings: Seq[Setting[_]] =
    buildInfoSettings ++
    Seq(
      buildInfoPackage := "com.banno",
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name,
                                         version,
                                         scalaVersion,
                                         sbtVersion,
                                         bannoDependencies,
                                         BuildInfoKey.action("buildTime") {
                                           (new Date()).toString
                                         },
                                         BuildInfoKey.action("gitSha") {
                                           Process("bash" :: "-c" :: "git rev-parse HEAD || echo None" :: Nil) !!
                                         })
    )
}
