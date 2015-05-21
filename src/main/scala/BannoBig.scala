package com.banno
import sbt._
import Keys._

object BannoBig {

  val autocreate = SettingKey[Boolean]("big-autocreate")
  val noRecreate = SettingKey[Boolean]("big-no-recreate")

  val services = SettingKey[Seq[String]]("big-services")

  val big = InputKey[Unit]("big")

  val upServices = TaskKey[Unit]("big-up-services")
  val upServicesIfAutocreate = TaskKey[Unit]("big-up-services-if-autocreate")
  val killServices = TaskKey[Unit]("big-kill-services")
  val destroyServices = TaskKey[Unit]("big-destroy-services")

  def addService(service: String) = {
    services += service
  }

  def addServices(services: String*) = {
    BannoBig.services ++= services
  }

  val settings: Seq[Setting[_]] = Seq(
    autocreate := true,
    noRecreate := true,
    services := Nil,

    big := processBigWithArguments(Def.spaceDelimited("<arg>").parsed),

    upServices := processBigUpServices(services.value, noRecreate.value),
    upServicesIfAutocreate := { if (autocreate.value) processBigUpServices(services.value, noRecreate.value) },
    killServices := processBigKillServices(services.value),
    destroyServices := processBigDestroyServices(services.value),

    (test in Test) <<= (test in Test).dependsOn(upServicesIfAutocreate),
    (testOnly in Test) <<= (testOnly in Test).dependsOn(upServicesIfAutocreate),
    (run in Compile) <<= (run in Compile).dependsOn(upServicesIfAutocreate)
  )

  private[this] def processBigWithArguments(args: Seq[String]) =
    processBigCommand("big" :: args.toList)

  private[this] def processBigUpServices(services: Seq[String], noRecreate: Boolean) =
    if (services.nonEmpty) {
      processBigCommand(makeBigUpCommand(noRecreate) ++ services)
    }

  private[this] def processBigKillServices(services: Seq[String]) =
    if (services.nonEmpty) {
      processBigCommand(List("big", "kill") ++ services)
    }

  private[this] def processBigDestroyServices(services: Seq[String]) =
    if (services.nonEmpty) {
      processBigCommand(List("big", "destroy") ++ services)
    }

  private[this] def makeBigUpCommand(noRecreate: Boolean) = {
    val baseCommand = List("big", "up", "-d")
    if (noRecreate) {
      baseCommand :+ "--no-recreate"
    } else {
      baseCommand
    }
  }

  private[this] def processBigCommand(command: List[String]) = {
    val exitCode = Process(command).!
    if (exitCode != 0) sys.error(s"Problem running command: ${command.mkString(' '.toString)}")
  }
}
