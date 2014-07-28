package com.banno
import sbt._
import Keys._
import sbtdocker._
import sbtdocker.Plugin.DockerKeys._

object Docker {

  val dockerPush = TaskKey[Unit]("dockerPush")
  val dockerPushLatestTag = TaskKey[Unit]("dockerPushLatestTag")
  val packageUsingDocker = SettingKey[Boolean]("packageUsingDocker")

  val namespace = SettingKey[String]("dockerNamespace")
  val appDir = SettingKey[File]("dockerAppDir")
  val exposedPorts = SettingKey[Seq[Int]]("dockerPorts")

  val regularPackage = (Keys.`package` in (Compile, packageBin))

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
      s"-Dcom.sun.management.jmxremote.port=8686",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Xmx512m",
      "-XX:MaxPermSize=128M",
      "$JAVA_OPTS"
    ),
    exposedPorts := Seq(
      8686,  // JMX
      9090   // Default Health
    ),

    docker := {
      val imageId = (docker dependsOn regularPackage).value
      val dockerImageName = (imageName in docker).value
      dockerTag(imageId.id, fullImageName(updateTagToLatest(dockerImageName)))
      imageId
    },

    dockerfile in docker := {
      val jarFile = artifactPath.in(Compile, packageBin).value
      val managedCp = (managedClasspath in Compile).value
      val main = mainClass.in(Compile, packageBin).value.get
      val javaArgs = (javaOptions in docker).value

      val dockerAppDir = appDir.value
      val jar = dockerAppDir / jarFile.name
      val classpath = Seq(dockerAppDir / "libs" / "*", dockerAppDir / "banno-libs" / "*", jar).mkString(":")
      val command =
        Seq(
          "bash",
          "-c",
          (Seq("java", "-cp", classpath) ++ javaArgs :+ main).mkString(" ")

        )

      // we partition these so the more volatile Banno libs are separate
      val bannoGroupId = Keys.organization.value
      val (bannoDepCp, otherCp) = managedCp.files.partition(isBannoDependency(bannoGroupId))
        
      new mutable.Dockerfile {
        otherCp.foreach { depFile => stageFile(depFile, dockerAppDir / "libs" / depFile.name) }
        bannoDepCp.foreach { depFile => stageFile(depFile, dockerAppDir / "banno-libs" / depFile.name) }
        stageFile(jarFile, jar)

        from("dockerfile/java")
        add(dockerAppDir / "libs", dockerAppDir / "libs")
        add(dockerAppDir / "banno-libs", dockerAppDir / "banno-libs")
        add(jar, jar)
        expose(exposedPorts.value: _*)
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
    dockerPushLatestTag := execDockerPush(updateTagToLatest((imageName in docker).value))
  )

  private[this] def isBannoDependency(bannoGroupId: String)(jar: File): Boolean =
    jar.getPath.contains(s"/${bannoGroupId}/")

  private[this] def dockerTag(imageId: String, name: String): Unit = {
    val cmd = "docker" :: "tag" :: imageId :: name :: Nil
    (cmd !)
  }

  private[this] def updateTagToLatest(dockerImageName: ImageName): ImageName =
    dockerImageName.copy(tag = Some("latest"))

  private[this] def execDockerPush(dockerImageName: ImageName): Unit = {
    val cmd = "docker" :: "push" :: fullImageName(dockerImageName) :: Nil
    cmd !
  }

  private[this] def fullImageName(dockerImageName: ImageName): String =
    s"${dockerImageName.namespace.get}/${dockerImageName.repository}:${dockerImageName.tag.get}"
}

