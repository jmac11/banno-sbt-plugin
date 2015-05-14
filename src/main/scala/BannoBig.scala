package com.banno
import sbt._
import Keys._

object BannoBig {

  val autocreate = SettingKey[Boolean]("big-autocreate")
  val noRecreate = SettingKey[Boolean]("big-no-recreate")

  val services = SettingKey[Seq[String]]("big-services")

  val doctor = TaskKey[Unit]("big-doctor")
  val up = TaskKey[Unit]("big-up")
  val upServices = TaskKey[Unit]("big-up-services")
  val upServicesIfAutocreate = TaskKey[Unit]("big-up-services-if-autocreate")
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
    autocreate := true,
    noRecreate := true,
    services := Nil,

    doctor := processBigDoctor(),
    up := processBigUp(noRecreate.value),
    upServices := processBigUpServices(services.value, noRecreate.value),
    upServicesIfAutocreate := { if (autocreate.value) processBigUpServices(services.value, noRecreate.value) },
    ps := processBigPs(),
    kill :=  processBigKill(),
    killServices := processBigKillServices(services.value),

    (test in Test) <<= (test in Test).dependsOn(upServicesIfAutocreate),
    (testOnly in Test) <<= (testOnly in Test).dependsOn(upServicesIfAutocreate),
    (run in Compile) <<= (run in Compile).dependsOn(upServicesIfAutocreate)
  )

  private[this] def processBigDoctor() =
    processBigCommand(List("big", "doctor"))

  private[this] def processBigUp(noRecreate: Boolean) =
    processBigCommand(makeBigUpCommand(noRecreate))

  private[this] def processBigUpServices(services: Seq[String], noRecreate: Boolean) =
    processBigCommand(makeBigUpCommand(noRecreate) ++ services)

  private[this] def processBigPs() =
    processBigCommand(List("big", "ps"))

  private[this] def processBigKill() =
    processBigCommand(List("big", "kill"))

  private[this] def processBigKillServices(services: Seq[String]) =
    processBigCommand(List("big", "kill") ++ services)

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
