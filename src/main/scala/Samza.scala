package com.banno
import sbt._
import Keys._
import com.banno.Docker.{baseImage, entryPointPrelude}
import sbtdocker.Plugin.DockerKeys.docker

object Samza {
  val settings = Docker.settings ++ Seq(
    javaOptions in docker ~= { _.filterNot(opt => opt.contains("Xms") || opt.contains("Xmx")) },
    mainClass in Compile := Some("com.banno.samza.Main"),
    baseImage in docker := "banno/samza-mesos:0.21.1",
    //Mesos native lib binds to LIBPROCESS_IP so the Mesos master can communicate with the Mesos framework, so it needs to be an IP accessible by Mesos master (not 127.0.0.1)
    entryPointPrelude in docker := "LIBPROCESS_IP=`ifconfig eth0 | awk '/inet addr/ {gsub(\"addr:\", \"\", $2); print $2}'`"
  )
}
