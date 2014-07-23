package com.banno
import sbt._
import Keys._
import sbtdocker._
import sbtdocker.Plugin.DockerKeys._

object Docker {

  val packageUsingDocker = SettingKey[Boolean]("package-using-docker")

  val settings = sbtdocker.Plugin.dockerSettings ++ Seq(
    packageUsingDocker := true,
    docker <<= docker.dependsOn(Keys.`package`.in(Compile, packageBin)),
    dockerfile in docker <<= (artifactPath.in(Compile, packageBin), managedClasspath in Compile, mainClass.in(Compile, packageBin)) map {
      case (jarFile, managedClasspath, Some(mainClass)) =>
        val libs = "/app/libs"
        val jarTarget = "/app/" + jarFile.name
        new Dockerfile {
          from("dockerfile/java")
          managedClasspath.files.foreach { depFile =>
            val target = file(libs) / depFile.name
            copyToStageDir(depFile, target)
          }
          add(libs, libs)
          add(jarFile, jarTarget)
          val classpath = s"$libs/*:$jarTarget"
          cmd("java", "-cp", classpath, mainClass)
        }
    },
    imageName in docker := {
      ImageName(
        namespace = Some("registry.banno-internal.com"),
        repository = name.value,
        tag = Some(version.value))
    }
  )
}
