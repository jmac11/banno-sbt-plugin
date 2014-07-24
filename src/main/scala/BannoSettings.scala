package com.banno
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object BannoSettings {
  val settings =
    Seq(organization := "com.banno",
        version in ThisBuild := "1-SNAPSHOT",
        scalaVersion := "2.11.2"
      ) ++
    Seq[Setting[_]](bannoDependencies := Seq.empty,
                    libraryDependencies <++= bannoDependencies) ++
    Seq(publishArtifact in (Compile, packageSrc) := false,
        publishArtifact in (Compile, packageDoc) := false) ++
    Seq(checksums in update := Nil) ++
    Seq(javaOptions ++= Seq("-Djava.awt.headless=true", "-Xmx1024M", "-XX:MaxPermSize=512m")) ++
    Revolver.settings ++
    BannoCi.settings ++
    BannoNexus.settings ++
    BannoCommonDeps.settings ++
    BannoCompile.settings ++
    BannoRelease.settings ++
    BannoPrompt.settings ++
    BannoIvy.settings
}
