package com.banno
import sbt._
import Keys._
import sbtdocker._
import sbtdocker.Plugin.DockerKeys._

object Docker {

  val dockerPush = TaskKey[Unit]("dockerPush")
  val dockerPushLatestTag = TaskKey[Unit]("dockerPushLatestTag")
  val dockerTagAsLatest = TaskKey[Unit]("dockerTagAsLatest")
  val packageUsingDocker = SettingKey[Boolean]("packageUsingDocker")

  val namespace = SettingKey[String]("dockerNamespace")
  val appDir = SettingKey[File]("dockerAppDir")

  val jmxPort = SettingKey[Int]("dockerJmxPort")
  val healthPort = SettingKey[Int]("dockerHealthPort")
  val ports = SettingKey[Seq[Int]]("dockerPorts")

  val settings = sbtdocker.Plugin.dockerSettings ++ Seq(
    packageUsingDocker := true,

    namespace := "registry.banno-internal.com",

    appDir := file("/app"),

    javaOptions in docker := Seq(
      "-server",
      "-XX:+HeapDumpOnOutOfMemoryError",
      "-XX:+UseConcMarkSweepGC",
      "-XX:+CMSClassUnloadingEnabled",
      "-XX:+UseCompressedOops",
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      s"-Dcom.sun.management.jmxremote.port=${jmxPort.value}",
      "-Dcom.sun.management.jmxremote.ssl=false"
    ),
    jmxPort := 8686,
    healthPort := 9090,
    ports := Seq(jmxPort.value, healthPort.value),

    docker <<= docker.dependsOn(Keys.`package`.in(Compile, packageBin)),

    dockerfile in docker := {
      val jarFile = artifactPath.in(Compile, packageBin).value
      val managedCp = (managedClasspath in Compile).value
      val main = mainClass.in(Compile, packageBin).value.get
      val javaArgs = (javaOptions in docker).value

      val dockerAppDir = appDir.value
      val jar = dockerAppDir / jarFile.name
      val classpath = Seq(dockerAppDir / "libs" / "*", jar).mkString(":")
      val command = Seq("java", "-cp", classpath) ++ javaArgs :+ main

      new mutable.Dockerfile {
        managedCp.files.foreach { depFile =>
          val target =  dockerAppDir / "libs" / depFile.name
          stageFile(depFile, target)
        }
        stageFile(jarFile, jar)

        from("dockerfile/java")
        add(appDir.value, appDir.value)
        expose(ports.value: _*)
        entryPoint(command: _*)
      }
    },

    imageName in docker := {
      ImageName(
        namespace  = Some(namespace.value),
        repository = name.value,
        tag        = Some(version.value)
      )
    },

    dockerPush := execDockerPush((imageName in docker).value),
    dockerPushLatestTag := execDockerPush(updateTagToLatest((imageName in docker).value)),

    dockerTagAsLatest := {
      val dockerImageName = (imageName in docker).value
      val cmd = "docker" :: "tag" :: fullImageName(dockerImageName) :: fullImageName(updateTagToLatest(dockerImageName)) :: Nil
      cmd !!
    }

  )

  private[this] def updateTagToLatest(dockerImageName: ImageName): ImageName =
    dockerImageName.copy(tag = Some("LATEST"))

  private[this] def execDockerPush(dockerImageName: ImageName): Unit = {
    val cmd = "docker" :: "push" :: fullImageName(dockerImageName) :: Nil
    cmd !!
  }

  private[this] def fullImageName(dockerImageName: ImageName): String =
    s"${dockerImageName.namespace}/#{dockerImageName.repository}:#{dockerImageName.tag}"
}
