package com.banno
import sbt._
import Keys._
import sbtbuildinfo.Plugin._

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
                                         BuildInfoKey.action("gitSha") {
                                           Process("bash" :: "-c" :: "git rev-parse HEAD || echo None" :: Nil) !!
                                         })
    )
}
