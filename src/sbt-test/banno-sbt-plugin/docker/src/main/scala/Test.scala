package com.banno

object Test extends App {
  val testEnvVar = sys.env("TEST_VAR")
  println(s"${BuildInfo.name} - Ok - ${args(0)} - ${args(1)} - ${testEnvVar}")
}
