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
      "-Xmx512m",
      "-XX:MaxPermSize=128M",
      "$JAVA_OPTS"
    ),
    exposedPorts := Seq(
      8686,  // JMX
      9090   // Default Health
    ),

    // necessary to touch directories
    docker <<= (streams, dockerPath in docker, buildOptions in docker, stageDirectory in docker, dockerfile in docker, imageName in docker) map {
        (streams, dockerPath, buildOptions, stageDir, dockerfile, imageName) =>
      val log = streams.log
      log.debug("Using Dockerfile:")
      log.debug(dockerfile.mkString)

      log.info(s"Creating docker image with name: '$imageName'")
      DockerBuilder.prepareFiles(dockerfile, stageDir, log)
      touchDirectoriesTo1970(stageDir / "app" / "libs", stageDir / "app" / "banno-libs")
      DockerBuilder.buildImage(dockerPath, buildOptions, imageName, stageDir, log)
    },

    docker := {
      val imageId = (docker dependsOn regularPackage).value
      val dockerImageName = (imageName in docker).value
      dockerTag(imageId.id, fullImageName(updateTagToLatest(dockerImageName)))
      imageId
    },

    dockerfile in docker := {
      val jarFile = artifactPath.in(Compile, packageBin).value
      val main = mainClass.in(Compile, packageBin).value.get
      val javaArgs = (javaOptions in docker).value

      val interalDepClasspaths = (internalDependencyClasspath in Compile).value
      val cpJars = (managedClasspath in Compile).value

      // we partition these so the more volatile Banno libs are separate
      val bannoGroupId = Keys.organization.value
      val (bannoDepCp, otherCp) = cpJars.files.partition(isBannoDependency(bannoGroupId))

      // include internal deps
      val internalDepsNameWithClassDir = interalDepClasspaths.flatMap { acp =>
        acp.metadata.get(Keys.moduleID.key).map { depModId =>
          (depModId.name, acp.data)
        }
      }

      val dockerAppDir = appDir.value
      val jar = dockerAppDir / jarFile.name
      val classpath =
        (
          internalDepsNameWithClassDir.map { case (name, _) => (dockerAppDir / "internal" / name) } ++
          List(dockerAppDir / "libs" / "*", dockerAppDir / "banno-libs" / "*", jar)
        ).mkString(":")
      val command =
        Seq(
          "bash",
          "-c",
          (Seq("java", "-cp", classpath) ++ javaArgs :+ main).mkString(" ")

        )

      new mutable.Dockerfile {
        otherCp.foreach    { depFile => stageFile(depFile, dockerAppDir / "libs" / depFile.name) }
        bannoDepCp.foreach { depFile => stageFile(depFile, dockerAppDir / "banno-libs" / depFile.name) }
        internalDepsNameWithClassDir.foreach { case (name, classDir) => stageFile(classDir, dockerAppDir / "internal" / name) }
        stageFile(jarFile, jar)

        from("dockerfile/java")
        add(dockerAppDir / "libs", dockerAppDir / "libs")
        add(dockerAppDir / "banno-libs", dockerAppDir / "banno-libs")
        if (internalDepsNameWithClassDir.nonEmpty)
          add(dockerAppDir / "internal", dockerAppDir / "internal")
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

  private[this] def touchDirectoriesTo1970(dirs: File*): Unit =
    dirs.foreach { dir =>
      "touch" :: "-t" :: "197001010000" :: dir.getPath :: Nil !!
    }

  private[this] def fullImageName(dockerImageName: ImageName): String =
    s"${dockerImageName.namespace.get}/${dockerImageName.repository}:${dockerImageName.tag.get}"
}
