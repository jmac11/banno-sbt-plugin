package com.banno
import sbt._
import Keys._

object BannoCi {
  val ciTask = TaskKey[Unit]("ci")

  val settings = Seq(
    ciTask <<= publish.dependsOn((test in Test).dependsOn(clean))
  )
}
