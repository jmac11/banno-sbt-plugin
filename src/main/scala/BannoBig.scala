package com.banno
import sbt._
import Keys._

object BannoBig {

  val enabled = SettingKey[Boolean]("big-enabled")
  val noRecreate = SettingKey[Boolean]("big-no-recreate")

  val services = SettingKey[Seq[String]]("big-services")

  val doctor = TaskKey[Unit]("big-doctor")
  val up = TaskKey[Unit]("big-up")
  val upServices = TaskKey[Unit]("big-up-services")
  val upServicesIfEnabled = TaskKey[Unit]("big-up-services-if-enabled")
  val ps = TaskKey[Unit]("big-ps")
  val kill = TaskKey[Unit]("big-kill")
  val killServices = TaskKey[Unit]("big-kill-services")

  def addService(service: String) = {
    services += service
  }

  def addServices(services: String*) = {
    BannoBig.services ++= services
  }

  val settings: Seq[Setting[_]] = Seq(
    enabled := true,
    noRecreate := true,
    services := Nil,

    doctor := processBigDoctor(),
    up := processBigUp(noRecreate.value),
    upServices := processBigUpServices(services.value, noRecreate.value),
    upServicesIfEnabled := processBigUpServices(services.value, noRecreate.value, enabled.value),
    ps := processBigPs(),
    kill :=  processBigKill(),
    killServices := processBigKillServices(services.value),

    (executeTests in Test) <<= (executeTests in Test).dependsOn(upServicesIfEnabled)
  )

  private[this] def processBigDoctor() =
    processBigCommand(List("big", "doctor"))

  private[this] def processBigUp(noRecreate: Boolean) =
    processBigCommand(makeBigUpCommand(noRecreate))

  private[this] def processBigUpService(service: String, noRecreate: Boolean) =
    processBigCommand(makeBigUpCommand(noRecreate) :+ service)

  private[this] def processBigUpServices(services: Seq[String], noRecreate: Boolean, enabled: Boolean = true) =
    if (enabled) {
      services.foreach(processBigUpService(_, noRecreate))
    }

  private[this] def processBigPs() =
    processBigCommand(List("big", "ps"))

  private[this] def processBigKill() =
    processBigCommand(List("big", "kill"))

  private[this] def processBigKillService(service: String) =
    processBigCommand(List("big", "kill", service))

  private[this] def processBigKillServices(services: Seq[String]) =
    services.foreach(processBigKillService(_))

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
    if (exitCode != 0) sys.error(s"Problem running command: $command.mkString(' '.toString)")
  }
}
