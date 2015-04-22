package com.banno
import sbt._
import Keys._
import sbtdocker._
import sbtdocker.DockerKeys._

object Docker {
  val dockerPullLatest = TaskKey[Unit]("Pull latest docker image")
  val dockerPush = DockerKeys.dockerPush

  val namespace = SettingKey[String]("Namespace for docker image")
  val baseImage = SettingKey[String]("Base docker image to use during build")

  val appDir = SettingKey[File]("App directory within docker")
  val exposedVolumes = SettingKey[Seq[File]]("Directories within docker to map")
  val exposedPorts = SettingKey[Seq[Int]]("Exposed ports in docker")

  val entryPointPrelude = SettingKey[String]("Additional ENV variables that need ran (in /bin/bash) before java process starts")
  val additionalRunShellCommands =
    SettingKey[Seq[String]]("Additional Run Commands to run during docker build. Each entry represents a RUN command in the /bin/sh -c '...' form")
  val additionalRunExecCommands =
    SettingKey[Seq[Seq[String]]]("Additional Run Commands to run during docker build. Each entry represents a RUN command in the exec form")
  val command = SettingKey[Seq[String]]("Docker Default Command (usually arguments given to the java process)")

  val regularPackage = (Keys.`package` in (Compile, packageBin))

  val settings = DockerSettings.baseDockerSettings ++ Seq(
    namespace in docker := "registry.banno-internal.com",
    baseImage in docker := "registry.banno-internal.com/java:latest",

    appDir in docker := file("/app"),
    exposedVolumes in docker := Seq((appDir in docker).value / "logs"),

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

    (additionalRunShellCommands in docker) := Nil,
    (additionalRunExecCommands in docker) := Nil,

    command in docker := Nil,
    entryPointPrelude in docker := "",

    docker <<= docker.dependsOn(sbt.Keys.`package`.in(Compile, packageBin)),

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
      val entryPointLinePrelude = (entryPointPrelude in docker).value
      val entryPointLine =
        Seq( "bash", "-c") :+ (
          (if (entryPointLinePrelude.nonEmpty) Seq(entryPointLinePrelude) else Nil) ++
          Seq("java", "-cp", classpath) ++ javaArgs :+ main :+ "\"$@\""
        ).mkString(" ") :+
      "--"

      val dockerVolumes = (exposedVolumes in docker).value.map(_.toString)

      new mutable.Dockerfile {
        from((baseImage in docker).value)

        if ((additionalRunShellCommands in docker).value.nonEmpty)
          (additionalRunShellCommands in docker).value.foreach(runLine => runRaw(runLine))
        if ((additionalRunExecCommands in docker).value.nonEmpty)
          (additionalRunExecCommands in docker).value.foreach(execLine => run(execLine: _*))

        workDir(dockerAppDir.toString)

        add(otherCp, s"${dockerAppDir}/libs/")

        if ((exposedPorts in docker).value.nonEmpty)
          expose((exposedPorts in docker).value: _*)

        if (dockerVolumes.nonEmpty)
          volume(dockerVolumes: _*)

        entryPoint(entryPointLine: _*)

        if ((command in docker).value.nonEmpty)
          cmd((command in docker).value: _*)

        if (bannoDepCp.nonEmpty)
          add(bannoDepCp, s"${dockerAppDir}/banno-libs/")

        if (internalDepsNameWithClassDir.nonEmpty)
          internalDepsNameWithClassDir.foreach {
            case (name, classDir) => add(classDir, s"${dockerAppDir}/internal/${name}/")
          }          

        add(jarFile, jar)
      }
    },

    imageNames in docker := Seq(
      ImageName(
        namespace  = Some((namespace in docker).value),
        repository = name.value,
        tag        = Some(version.value)
      ),
      ImageName(
        namespace  = Some((namespace in docker).value),
        repository = name.value,
        tag        = Some("latest")
      )
    ),

    buildOptions in docker := BuildOptions(pullBaseImage = BuildOptions.Pull.Always),

    dockerPullLatest := execDockerPull(updateTagToLatest((imageNames in docker).value.head))
  )

  private[this] def isBannoDependency(bannoGroupId: String)(jar: sbt.File): Boolean =
    jar.getPath.contains(s"/${bannoGroupId}/")

  private[this] def updateTagToLatest(dockerImageName: ImageName): ImageName =
    dockerImageName.copy(tag = Some("latest"))

  private[this] def execDockerPull(dockerImageName: ImageName): Unit =
    ("docker" :: "pull" :: dockerImageName.toString :: Nil) !

}
