package com.banno
import sbt._
import Keys._
import sbtdocker._
import sbtdocker.Plugin.DockerKeys._

object Docker {
  val docker = Plugin.DockerKeys.docker

  val dockerPullLatest = TaskKey[Unit]("Pull latest docker image")
  val dockerPush = TaskKey[Unit]("Push docker image")
  val dockerPushLatestTag = TaskKey[Unit]("Push docker image as latest tag")

  val namespace = SettingKey[String]("Namespace for docker image")
  val baseImage = SettingKey[String]("Base docker image to use during build")

  val packageUsingDocker = SettingKey[Boolean]("Package using docker?")
  val appDir = SettingKey[File]("App directory within docker")
  val exposedPorts = SettingKey[Seq[Int]]("Exposed ports in docker")

  val additionalRunCommands = SettingKey[Seq[String]]("Additional Run Commands to run during docker build")
  val command = SettingKey[Seq[String]]("Docker Default Command (usually arguments given to the java process)")

  val regularPackage = (Keys.`package` in (Compile, packageBin))

  val settings = sbtdocker.Plugin.dockerSettings ++ Seq(
    packageUsingDocker := true,

    namespace in docker := "registry.banno-internal.com",
    baseImage in docker := "registry.banno-internal.com/java:latest",

    appDir in docker := file("/app"),

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
    exposedPorts in docker := Nil,

    (additionalRunCommands in docker) := Nil,

    command in docker := Nil,

    // necessary to touch directories
    docker <<= (streams, dockerPath in docker, buildOptions in docker, stageDirectory in docker, dockerfile in docker, imageName in docker, appDir in docker) map {
        (streams, dockerPath, buildOptions, stageDir, dockerfile, imageName, appDir) =>
      val log = streams.log
      log.debug("Using Dockerfile:")
      log.debug(dockerfile.mkString)

      log.info(s"Creating docker image with name: '$imageName'")
      DockerBuilder.prepareFiles(dockerfile, stageDir, log)
      touchDirectoriesTo1970(stageDir / appDir.getPath / "libs", stageDir / appDir.getPath / "banno-libs")
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

      val dockerAppDir = (appDir in docker).value
      val jar = dockerAppDir / jarFile.name
      val classpath =
        (
          jar +:
          internalDepsNameWithClassDir.map { case (name, _) => (dockerAppDir / "internal" / name) } :+
          dockerAppDir / "banno-libs" / "*" :+
          dockerAppDir / "libs" / "*"
        ).mkString(":")
      val entryPointLine =
        Seq(
          "bash",
          "-c",
          (Seq("java", "-cp", classpath) ++ javaArgs :+ main :+ "\"$@\"").mkString(" "),
          "--"
        )

      new mutable.Dockerfile {
        otherCp.foreach    { depFile => stageFile(depFile, dockerAppDir / "libs" / depFile.name) }
        bannoDepCp.foreach { depFile => stageFile(depFile, dockerAppDir / "banno-libs" / depFile.name) }
        internalDepsNameWithClassDir.foreach { case (name, classDir) => stageFile(classDir, dockerAppDir / "internal" / name) }
        stageFile(jarFile, jar)

        from((baseImage in docker).value)

        if ((additionalRunCommands in docker).value.nonEmpty)
          (additionalRunCommands in docker).value.foreach(runLine => run(runLine))

        workDir("/app")
        add(dockerAppDir / "libs", dockerAppDir / "libs")
        add(dockerAppDir / "banno-libs", dockerAppDir / "banno-libs")
        if (internalDepsNameWithClassDir.nonEmpty)
          add(dockerAppDir / "internal", dockerAppDir / "internal")
        add(jar, jar)

        if ((exposedPorts in docker).value.nonEmpty)
          expose((exposedPorts in docker).value: _*)

        entryPoint(entryPointLine: _*)

        if ((command in docker).value.nonEmpty)
          cmd((command in docker).value: _*)
      }
    },

    imageName in docker := {
      ImageName(
        namespace  = Some((namespace in docker).value),
        repository = name.value,
        tag        = Some(version.value)
      )
    },

    dockerPullLatest := execDockerPull((imageName in docker).value.copy(tag = Some("latest"))),

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

  private[this] def execDockerPull(dockerImageName: ImageName): Unit =
    ("docker" :: "pull" :: fullImageName(dockerImageName) :: Nil) !

  private[this] def execDockerPush(dockerImageName: ImageName): Unit = {
    val cmd = "docker" :: "push" :: fullImageName(dockerImageName) :: Nil
    val exitCode = (cmd !)
    if (exitCode != 0) sys.error(s"'${cmd}' failed")
  }

  private[this] def touchDirectoriesTo1970(dirs: File*): Unit =
    dirs.foreach { dir =>
      "touch" :: "-t" :: "197001010000" :: dir.getPath :: Nil !!
    }

  private[this] def fullImageName(dockerImageName: ImageName): String =
    s"${dockerImageName.namespace.get}/${dockerImageName.repository}:${dockerImageName.tag.get}"
}
