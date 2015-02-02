package com.banno
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import net.virtualvoid.sbt.graph.{Plugin => GraphPlugin}
import com.github.tkawachi.doctest.DoctestPlugin
import com.github.tkawachi.doctest.DoctestPlugin._

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
    Seq(publishArtifact in (Compile, packageSrc) := false,
        publishArtifact in (Compile, packageDoc) := false) ++
    Seq(checksums in update := Nil) ++
    Seq(javaOptions ++= Seq("-Djava.awt.headless=true", "-Xmx1024M", "-XX:MaxPermSize=512m")) ++
    GraphPlugin.graphSettings ++
    Revolver.settings ++
    DoctestPlugin.doctestSettings ++
    Seq(doctestTestFramework := DoctestTestFramework.Specs2, doctestWithDependencies := false) ++
    BannoCi.settings ++
    BannoNexus.settings ++
    BuildInfoSettings.settings ++
    BannoCommonDeps.settings ++
    BannoCompile.settings ++
    BannoRelease.settings ++
    BannoPrompt.settings ++
    BannoIvy.settings
}
