package com.banno
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import net.virtualvoid.sbt.graph.{Plugin => GraphPlugin}

object BannoSettings {
  val buildSettings = Seq(
    version in ThisBuild := "1-SNAPSHOT"
  )

  val settings =
    Seq(organization := "com.banno",
        scalaVersion := "2.10.4"
      ) ++
    Seq[Setting[_]](bannoDependencies := Seq.empty,
                    libraryDependencies <++= bannoDependencies) ++
    Seq(publishArtifact in (Compile, packageSrc) := false) ++
    Seq(checksums in update := Nil) ++
    Seq(javaOptions ++= Seq("-Djava.awt.headless=true", "-Xmx1024M", "-XX:MaxPermSize=512m")) ++
    GraphPlugin.graphSettings ++
    Revolver.settings ++
    BannoCi.settings ++
    BannoNexus.settings ++
    BuildInfoSettings.settings ++
    BannoCommonDeps.settings ++
    BannoCompile.settings ++
    BannoRelease.settings ++
    BannoPrompt.settings ++
    BannoIvy.settings
}
