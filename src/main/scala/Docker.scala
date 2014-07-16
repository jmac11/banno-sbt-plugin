package com.banno
import sbt._
import Keys._
import sbtdocker._
import sbtdocker.Plugin.DockerKeys._
import sbtassembly._
import sbtassembly.Plugin
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._

object Docker {

  val packageUsingDocker = SettingKey[Boolean]("package-using-docker")

  val settings = sbtdocker.Plugin.dockerSettings ++ sbtassembly.Plugin.assemblySettings ++ Seq(
    packageUsingDocker := true,
    docker <<= (docker dependsOn assembly),
    doockerfile in docker := {
      val artifact = (outputPath in assembly).value
      val appDirPath = "/app"
      val artifactTargetPath = s"$appDirPath/${artifact.name}"
      new Dockerfile {
        from("dockerfile/java")
        add(artifact, artifactTargetPath)
        workDir(appDirPath)
        entryPoint("java", "-jar", artifactTargetPath)
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
