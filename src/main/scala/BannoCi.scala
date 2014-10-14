package com.banno
import sbt._
import Keys._
import complete.DefaultParsers._
import Docker._
import sbtdocker._
import sbtdocker.Plugin._
import sbtdocker.Plugin.DockerKeys._

import scala.util.Try

object BannoCi {
  val ciTask = TaskKey[Unit]("ci-without-docker")

  val settings = Seq(
    commands += ci,
    ciTask <<= {
      publish.dependsOn((test in Test).dependsOn(clean))
    }
  )

  val ci: Command = Command("ci")(_ => (Space ~> "skip-tests").*) { (st, args) =>
    val extract = Project.extract(st)
    Project.runTask(ciTask.scopedKey, st)

    val useDocker = extract.getOpt(Docker.packageUsingDocker)
    if (useDocker.getOrElse(false)) {
      extract.runTask(Docker.dockerPullLatest, st)
      extract.runTask(docker, st)
      extract.runTask(Docker.dockerPush, st)
      extract.runTask(Docker.dockerPushLatestTag, st)
    }

    st
  }
}
